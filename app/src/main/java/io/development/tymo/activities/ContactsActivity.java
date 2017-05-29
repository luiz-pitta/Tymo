package io.development.tymo.activities;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.ContactsAdapter;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.development.tymo.view_holder.ContactViewHolder;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, ContactViewHolder.RefreshLayoutPlansCallback {

    private EasyRecyclerView recyclerView;
    private ContactsAdapter adapter;
    private String email, full_name;
    private SearchView searchView;

    private ImageView mBackButton;
    private TextView m_title, contactsQty;

    private int m_contacts_qty;

    private Handler handler = new Handler();

    private List<User> listContactQuery, listContact;

    private CompositeSubscription mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            if(adapter.getCount() > 60)
                recyclerView.showProgress();
            executeFilter(query);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mSubscriptions = new CompositeSubscription();

        findViewById(R.id.dateBox).setVisibility(View.GONE);
        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        contactsQty = (TextView) findViewById(R.id.contactsQty);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);
        recyclerView = (EasyRecyclerView) findViewById(R.id.recycler_view);
        searchView = (SearchView) findViewById(R.id.searchSelectionView);

        mBackButton.setOnClickListener(this);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(mOnQueryTextListener);

        email = getIntent().getStringExtra("email_contacts");
        full_name = getIntent().getStringExtra("contact_full_name");

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String my_email = mSharedPreferences.getString(Constants.EMAIL, "");

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this, R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setRefreshListener(this);
        recyclerView.setRefreshingColor(ContextCompat.getColor(this,R.color.deep_purple_400));

        if (email.matches(my_email)) {
            m_title.setText(getResources().getString(R.string.my_contacts));
            adapter = new ContactsAdapter(this, true, false, this);
            recyclerView.setEmptyView(R.layout.empty_contacts);
        } else {
            m_title.setText(getResources().getString(R.string.contacts_of, fullNameToShortName(full_name)));
            adapter = new ContactsAdapter(this, false, false, this);
            recyclerView.setEmptyView(R.layout.empty_contacts_friend);
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.PEOPLE_ACCEPT);

        User user = new User();
        user.setEmail(my_email);

        if (email.matches(my_email)) {
            m_title.setText(getResources().getString(R.string.my_contacts));
            adapter = new ContactsAdapter(this, true, false, this);
            getContacts(email);
        } else {
            m_title.setText(getResources().getString(R.string.contacts_of, fullNameToShortName(full_name)));
            adapter = new ContactsAdapter(this, false, false, this);
            getContactsFriend(email, user);
        }

        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String my_email = mSharedPreferences.getString(Constants.EMAIL, "");

                if (!adapter.getItem(position).getEmail().matches(my_email)) {
                    Intent intent = new Intent(ContactsActivity.this, FriendProfileActivity.class);
                    intent.putExtra("friend_email", adapter.getItem(position).getEmail());
                    intent.putExtra("name", adapter.getItem(position).getName());
                    startActivity(intent);
                }
            }
        });

        recyclerView.setAdapterWithProgress(adapter);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        findViewById(R.id.horizontalBottomLine).setVisibility(View.GONE);
        findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
        findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
    }

    private String fullNameToShortName(String fullName){
        String[] fullNameSplited = fullName.split(" ");

        if (fullNameSplited.length > 1){
            return fullNameSplited[0] + " " + fullNameSplited[fullNameSplited.length-1];
        }
        else {
            return fullNameSplited[0];
        }
    }

    public void executeFilter(String query) {
        // Load items
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<User> filteredModelList = filter(listContact, query);
                adapter.clear();
                adapter.addAll(filteredModelList);

                m_contacts_qty = adapter.getCount();

                if(m_contacts_qty == 0){
                    findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
                    findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
                    recyclerView.showEmpty();
                }
                else if(m_contacts_qty == 1){
                    findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
                    findViewById(R.id.contactsQtyBox).setVisibility(View.VISIBLE);
                    contactsQty.setText(R.string.contacts_qty_one);
                }
                else{
                    findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
                    findViewById(R.id.contactsQtyBox).setVisibility(View.VISIBLE);
                    contactsQty.setText(getResources().getString(R.string.contacts_qty, m_contacts_qty));
                }

                recyclerView.scrollToPosition(0);
            }
        });
        // Load complete
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.clear();

                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String my_email = mSharedPreferences.getString(Constants.EMAIL, "");

                User user = new User();
                user.setEmail(my_email);

                if (email.matches(my_email)) {
                    m_title.setText(getResources().getString(R.string.my_contacts));
                    getContacts(email);
                } else {
                    m_title.setText(getResources().getString(R.string.contacts_of, fullNameToShortName(full_name)));
                    getContactsFriend(email, user);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "onRefresh" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                recyclerView.showProgress();
            }
        }, 1000);
    }

    private void getContacts(String email) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getUsers(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void getContactsFriend(String email, User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getFriendsFriend(email, usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFriend, this::handleError));
    }

    private void handleResponseFriend(Response response) {

        User user = response.getUser();

        if(user.getCountKnows() == 0 && user.getPrivacy() == 1) {
            recyclerView.setEmptyView(R.layout.empty_plans_private);
            findViewById(R.id.horizontalBottomLine).setVisibility(View.GONE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
            findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
            findViewById(R.id.searchSelection).setVisibility(View.GONE);
            recyclerView.showEmpty();
        }else {
            findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
            findViewById(R.id.contactsQtyBox).setVisibility(View.VISIBLE);
            findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);
            listContact = new ArrayList<>();
            listContactQuery = new ArrayList<>();
            listContact.addAll(response.getPeople());
            listContactQuery.addAll(response.getPeople());
            adapter.clear();
            adapter.addAll(response.getPeople());

            m_contacts_qty = response.getPeople().size();

            if(m_contacts_qty == 0){
                findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
                findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
            }
            else if(m_contacts_qty == 1){
                contactsQty.setText(R.string.contacts_qty_one);
            }
            else{
                contactsQty.setText(getResources().getString(R.string.contacts_qty, m_contacts_qty));
            }
        }
    }

    private void handleResponse(ArrayList<User> users) {

        listContact = new ArrayList<>();
        listContactQuery = new ArrayList<>();
        listContact.addAll(users);
        listContactQuery.addAll(users);
        adapter.clear();
        adapter.addAll(users);

        m_contacts_qty = users.size();

        findViewById(R.id.horizontalBottomLine).setVisibility(View.VISIBLE);
        findViewById(R.id.horizontalBottomLine2).setVisibility(View.VISIBLE);
        findViewById(R.id.contactsQtyBox).setVisibility(View.VISIBLE);
        findViewById(R.id.searchSelection).setVisibility(View.VISIBLE);

        if(m_contacts_qty == 0){
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
            findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
        }
        else if(m_contacts_qty == 1){
            contactsQty.setText(R.string.contacts_qty_one);
        }
        else{
            contactsQty.setText(getResources().getString(R.string.contacts_qty, m_contacts_qty));
        }

    }

    private void handleError(Throwable error) {
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private List<User> filter(List<User> models, String query) {
        String lowerCaseQuery = query.toLowerCase();

        List<User> filteredModelList = new ArrayList<>();

        if(models != null) {
            for (User model : models) {
                String text = model.getName().toLowerCase();
                if (text.contains(lowerCaseQuery)) {
                    filteredModelList.add(model);
                }
            }
        }

        return filteredModelList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }


    @Override
    public void onClick(View v) {
        if (v == mBackButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            onBackPressed();
        }
    }

    @Override
    public void refreshLayout() {

        listContact.clear();
        listContactQuery.clear();
        listContact.addAll(adapter.getAllData());
        listContactQuery.addAll(adapter.getAllData());

        m_contacts_qty = adapter.getCount();

        if(m_contacts_qty == 0){
            recyclerView.setEmptyView(null);
            findViewById(R.id.horizontalBottomLine).setVisibility(View.GONE);
            findViewById(R.id.horizontalBottomLine2).setVisibility(View.GONE);
            findViewById(R.id.contactsQtyBox).setVisibility(View.GONE);
            findViewById(R.id.searchSelection).setVisibility(View.GONE);
            recyclerView.showEmpty();
        }
        else if(m_contacts_qty == 1){
            contactsQty.setText(R.string.contacts_qty_one);
        }
        else{
            contactsQty.setText(getResources().getString(R.string.contacts_qty, m_contacts_qty));
        }
    }

}
