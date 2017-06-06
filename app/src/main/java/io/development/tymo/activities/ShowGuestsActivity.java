package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import io.development.tymo.R;
import io.development.tymo.adapters.ViewPagerAdapter;
import io.development.tymo.fragments.FitPeopleFragment;
import io.development.tymo.fragments.InvitedPeopleFragment;
import io.development.tymo.model_server.ListUserWrapper;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ShowGuestsActivity extends AppCompatActivity {

    private ImageView mBackButton;
    private TextView m_title;

    private Handler handler = new Handler();
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SearchView searchView;
    private CompositeSubscription mSubscriptions;
    private ViewPagerAdapter adapter;
    private boolean isAdm, isFlag;
    private long idAct;
    private ArrayList<User> listInvitedUser = new ArrayList<>(), listQueryInvitedUser = new ArrayList<>();
    private ArrayList<User> listConfirmedUser = new ArrayList<>(), listQueryConfirmedUser = new ArrayList<>();

    private FirebaseAnalytics mFirebaseAnalytics;

    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            InvitedPeopleFragment invitedPeopleFragment = (InvitedPeopleFragment) adapter.getItem(0);
            FitPeopleFragment fitPeopleFragment = (FitPeopleFragment) adapter.getItem(1);

            invitedPeopleFragment.showProgress(true);
            fitPeopleFragment.showProgress(true);

            executeFilter(query);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_guests);

        mSubscriptions = new CompositeSubscription();

        findViewById(R.id.icon1).setVisibility(View.GONE);
        findViewById(R.id.icon2).setVisibility(View.INVISIBLE);

        searchView = (SearchView) findViewById(R.id.searchSelectionView);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        mBackButton = (ImageView) findViewById(R.id.actionBackIcon);
        m_title = (TextView) findViewById(R.id.text);

        m_title.setText(getResources().getString(R.string.guests));

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mBackButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                Intent intent = new Intent();
                //intent.putStringArrayListExtra("tags_objs", tagListSelected);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        tabLayout.setupWithViewPager(viewPager);

        //search bar
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
        //search bar end

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        //setUpMultiChoiceRecyclerView(email);
        ListUserWrapper listUserWrapper = (ListUserWrapper)getIntent().getSerializableExtra("guest_list_user");
        listQueryInvitedUser = listUserWrapper.getListUser();
        listUserWrapper = (ListUserWrapper)getIntent().getSerializableExtra("confirmed_list_user");
        listQueryConfirmedUser = listUserWrapper.getListUser();

        isAdm = getIntent().getBooleanExtra("is_adm", false);
        idAct = getIntent().getLongExtra("id_act", -1);
        isFlag = getIntent().getBooleanExtra("is_flag", false);

        listConfirmedUser.addAll(listQueryConfirmedUser);
        listInvitedUser.addAll(listQueryInvitedUser);

        setupViewPager(viewPager);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public String getQuery() {
        return searchView.getQuery().toString();
    }

    public void executeFilter(String query) {
        // Load items
        handler.post(new Runnable() {
            @Override
            public void run() {

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "executeFilter" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                for(int i=0;i<listInvitedUser.size();i++){
                    for(int j=0;i<listQueryInvitedUser.size();i++){
                        if(listInvitedUser.get(i).getEmail().matches(listQueryInvitedUser.get(j).getEmail()))
                        {
                            listInvitedUser.remove(i);
                            listInvitedUser.add(i, listQueryInvitedUser.get(j));
                        }
                    }
                }

                for(int i=0;i<listConfirmedUser.size();i++){
                    for(int j=0;i<listQueryConfirmedUser.size();i++){
                        if(listConfirmedUser.get(i).getEmail().matches(listQueryConfirmedUser.get(j).getEmail()))
                        {
                            listConfirmedUser.remove(i);
                            listConfirmedUser.add(i, listQueryInvitedUser.get(j));
                        }
                    }
                }

                ArrayList<User> filteredModelList1 = filter(listInvitedUser, query);
                ArrayList<User> filteredModelList2 = filter(listConfirmedUser, query);

                InvitedPeopleFragment invitedPeopleFragment = (InvitedPeopleFragment) adapter.getItem(0);
                listQueryInvitedUser.clear();
                listQueryInvitedUser.addAll(filteredModelList1);
                invitedPeopleFragment.setAdapterItens(listQueryInvitedUser);
                adapter.setPageTitle(0, getResources().getString(R.string.guests_invited, listQueryInvitedUser.size()));

                FitPeopleFragment fitPeopleFragment = (FitPeopleFragment) adapter.getItem(1);
                listQueryConfirmedUser.clear();
                listQueryConfirmedUser.addAll(filteredModelList2);
                fitPeopleFragment.setAdapterItens(listQueryConfirmedUser);
                adapter.setPageTitle(1, getResources().getString(R.string.guests_fit, listQueryConfirmedUser.size()));

                adapter.notifyDataSetChanged();
            }
        });
        // Load complete
    }


    public ArrayList<User> getListInvitedUser(){
        return listQueryInvitedUser;
    }

    public ArrayList<User> getListConfirmedUser(){
        return listQueryConfirmedUser;
    }

    public void setListInvitedUser(ArrayList<User> list){
        listQueryInvitedUser = list;
    }

    public void setListConfirmedUser(ArrayList<User> list){
        listQueryConfirmedUser = list;
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getFragmentManager());

        adapter.addFragment(new InvitedPeopleFragment(), getResources().getString(R.string.guests_invited, listInvitedUser.size()));
        adapter.addFragment(new FitPeopleFragment(), getResources().getString(R.string.guests_fit, listConfirmedUser.size()));
        viewPager.setAdapter(adapter);

        InvitedPeopleFragment invitedPeopleFragment = (InvitedPeopleFragment) adapter.getItem(0);
        invitedPeopleFragment.setAdapterItens(listQueryInvitedUser);
        invitedPeopleFragment.setIdAct(idAct);
        invitedPeopleFragment.setAdm(isAdm);
        invitedPeopleFragment.setFlag(isFlag);

        FitPeopleFragment fitPeopleFragment = (FitPeopleFragment) adapter.getItem(1);
        fitPeopleFragment.setAdapterItens(listQueryConfirmedUser);
        fitPeopleFragment.setIdAct(idAct);
        fitPeopleFragment.setAdm(isAdm);
        fitPeopleFragment.setFlag(isFlag);
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void getGuests(String email) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getUsers(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(ArrayList<User> users) {

        setProgress(false);
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private ArrayList<User> filter(ArrayList<User> models, String query) {
        String lowerCaseQuery = query.toLowerCase();

        ArrayList<User> filteredModelList = new ArrayList<>();
        for (User model : models) {
            String text = model.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}
