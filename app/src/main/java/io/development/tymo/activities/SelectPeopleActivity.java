package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.decoration.DividerDecoration;


import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.SelectionPeopleAdapter;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;


public class SelectPeopleActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView applyButton, cleanButton;
    private RecyclerView mMultiChoiceRecyclerView;
    private ArrayList<User> personQueryList, personList, personListSelected;
    private SearchView searchView;
    private SelectionPeopleAdapter selectionPeopleAdapter;
    private CompositeDisposable mSubscriptions;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();

    private FirebaseAnalytics mFirebaseAnalytics;

    private SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String query) {
            //if(selectionPeopleAdapter.getItemCount() > 50 || query.equals(""))
            //    setProgress(true);
            executeFilter(query);
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items_select);

        mSubscriptions = new CompositeDisposable();

        findViewById(R.id.exampleBox).setVisibility(GONE);
        findViewById(R.id.horizontalBottomLine2).setVisibility(GONE);

        searchView = (SearchView) findViewById(R.id.searchSelectionView);
        applyButton = (TextView) findViewById(R.id.applyButton);
        cleanButton = (TextView) findViewById(R.id.cleanButton);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);


        //search bar
        //int magId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        //ImageView magImage = (ImageView) searchView.findViewById(magId);
        //magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        //magImage.setVisibility(GONE);


        mSwipeRefreshLayout.setDistanceToTriggerSync(400);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this,R.color.deep_purple_400));

        applyButton.setOnClickListener(this);
        cleanButton.setOnClickListener(this);

        //search bar

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
        //search bar end

        mMultiChoiceRecyclerView = (RecyclerView) findViewById(R.id.recyclerSelectView);
        mMultiChoiceRecyclerView.setNestedScrollingEnabled(false);
        mMultiChoiceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        setUpMultiChoiceRecyclerView(email);

        personListSelected = new ArrayList<>();

        cleanButton.setText(getResources().getString(R.string.cancel));
        applyButton.setText(getResources().getString(R.string.ok));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                setUpMultiChoiceRecyclerView(email);
            }
        }, 500);

        // Load complete
    }

    public void executeFilter(String query) {
        // Load items
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<User> filteredModelList = filter(personList, query);
                if(selectionPeopleAdapter !=null) {
                    selectionPeopleAdapter.swap(filteredModelList);
                    selectionPeopleAdapter.deselectAll();
                    setSelectionQuery();
                    mMultiChoiceRecyclerView.scrollToPosition(0);
                }
                setProgress(false);
            }
        });
        // Load complete
    }

    public void setProgress(boolean progress) {
        if(progress)
            findViewById(R.id.progressBox).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.progressBox).setVisibility(View.GONE);
    }

    private void setUpMultiChoiceRecyclerView(String email) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getUsers(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(ArrayList<User> users) {

        int i, j;
        personQueryList = new ArrayList<>();
        List<String> stock_list = getIntent().getStringArrayListExtra("guest_list");
        boolean erase_from_list = getIntent().getBooleanExtra("erase_from_list", false);
        UserWrapper userWrapper = (UserWrapper)getIntent().getSerializableExtra("user_friend_exclude");

        if(userWrapper == null)
            personQueryList.addAll(users);
        else{
            User userFriend = userWrapper.getUser();
            for (i = 0; i < users.size(); i++) {
                if (!users.get(i).getEmail().equals(userFriend.getEmail()))
                    personQueryList.add(users.get(i));
            }
        }

        if(erase_from_list && stock_list != null){
            for (i = 0; i < personQueryList.size() && stock_list.size() > 0; i++) {
                User friend = personQueryList.get(i);
                if (stock_list.contains(friend.getEmail())) {
                    personQueryList.remove(i);
                    i--;
                }
            }
        }

        personList = new ArrayList<>();
        personList.addAll(personQueryList);
        selectionPeopleAdapter = new SelectionPeopleAdapter(personQueryList, getApplication()) ;
        mMultiChoiceRecyclerView.setAdapter(selectionPeopleAdapter);
        selectionPeopleAdapter.setSingleClickMode(true);

        selectionPeopleAdapter.setMultiChoiceSelectionListener(new MultiChoiceAdapter.Listener() {
            @Override
            public void OnItemSelected(int selectedPosition, int itemSelectedCount, int allItemCount) {
                int position = getPositionSelected(personQueryList.get(selectedPosition).getEmail());
                if(position == -1)
                    personListSelected.add(personQueryList.get(selectedPosition));
            }

            @Override
            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
                int position = getPositionSelected(personQueryList.get(deselectedPosition).getEmail());
                if(position >= 0)
                    personListSelected.remove(position);
            }

            @Override
            public void OnSelectAll(int itemSelectedCount, int allItemCount) {

            }

            @Override
            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {

            }
        });

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));
        itemDecoration.setDrawLastItem(false);

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);
        mMultiChoiceRecyclerView.setHasFixedSize(true);

        if(stock_list != null) {
            for (i = 0; i < personList.size() && stock_list.size() > 0; i++) {
                User personModel = personList.get(i);
                if (stock_list.contains(personModel.getEmail()))
                    selectionPeopleAdapter.select(i);

            }
        }

        if(selectionPeopleAdapter.getItemCount() > 60) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setProgress(false);
                }
            }, 1500);
        }else
            setProgress(false);

        String query = searchView.getQuery().toString();
        if(!query.equals(""))
            executeFilter(query);
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        if(!Utilities.isDeviceOnline(this))
            Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        //else
        //    Toast.makeText(this, getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == applyButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "applyButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent();
            PersonModelWrapper wrapper = new PersonModelWrapper(personListSelected);
            intent.putExtra("guest_objs", wrapper);
            setResult(RESULT_OK, intent);
            finish();
        }else if(view == cleanButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private ArrayList<User> filter(List<User> models, String query) {
        if(models == null)
            return new ArrayList<>();

        ArrayList<User> filteredModelList = new ArrayList<>();
        for (User model : models) {
            String text = model.getName().toLowerCase();
            if (Utilities.isListContainsQuery(text, query))
                filteredModelList.add(model);
        }
        return filteredModelList;
    }

    private void setSelectionQuery(){
        int i;
        int pos;
        for(i=0;i<personQueryList.size();i++){
            pos = getPositionSelected(personQueryList.get(i).getEmail());
            if(pos >= 0)
                selectionPeopleAdapter.select(i);

        }

    }

    private int getPositionSelected(String name){
        int i;
        for(i=0;i<personListSelected.size();i++){
            String text = personListSelected.get(i).getEmail();
            if(text.equals(name))
                return i;
        }
        return -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.dispose();
    }

}
