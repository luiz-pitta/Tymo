package io.development.tymo.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceRecyclerView;
import com.davidecirillo.multichoicerecyclerview.listeners.MultiChoiceSelectionListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.adapters.SelectionPeopleAdapter;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.model_server.UserWrapper;
import io.development.tymo.models.PersonModelWrapper;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

//TODO mudar para pegar o email de intent e não do shared preferences
public class SelectPeopleActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView applyButton, cleanButton;
    private MultiChoiceRecyclerView mMultiChoiceRecyclerView;
    private ArrayList<User> personQueryList, personList, personListSelected;
    private SearchView searchView;
    private SelectionPeopleAdapter selectionPeopleAdapter;
    private CompositeSubscription mSubscriptions;
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
            setProgress(true);
            executeFilter(query);
            return true;
        }
    };
    private MultiChoiceSelectionListener mMultiChoiceSelectionListener = new MultiChoiceSelectionListener() {
        @Override
        public void OnItemSelected(final int selectedPosition, int itemSelectedCount, int allItemCount) {
            personListSelected.add(personQueryList.get(selectedPosition));
        }

        @Override
        public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
            personListSelected.remove(getPositionSelected(personQueryList.get(deselectedPosition).getEmail()));
        }

        @Override
        public void OnSelectAll(int itemSelectedCount, int allItemCount) {

        }

        @Override
        public void OnDeselectAll(int itemSelectedCount, int allItemCount) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_base);

        mSubscriptions = new CompositeSubscription();

        searchView = (SearchView) findViewById(R.id.searchSelectionView);
        applyButton = (TextView) findViewById(R.id.applyButton);
        cleanButton = (TextView) findViewById(R.id.cleanButton);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);

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

        mMultiChoiceRecyclerView = (MultiChoiceRecyclerView) findViewById(R.id.recyclerSelectView);
        mMultiChoiceRecyclerView.setNestedScrollingEnabled(false);

        SharedPreferences mSharedPreferences = getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        String email = mSharedPreferences.getString(Constants.EMAIL, "");

        setUpMultiChoiceRecyclerView(email);

        personListSelected = new ArrayList<>();

        cleanButton.setText(getResources().getString(R.string.cancel));
        applyButton.setText(getResources().getString(R.string.ok));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAnalytics.setCurrentScreen(this, getClass().getSimpleName(), null /* class override */);

    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems");
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
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
                selectionPeopleAdapter.swap(filteredModelList);
                mMultiChoiceRecyclerView.deselectAll();
                setSelectionQuery();
                mMultiChoiceRecyclerView.scrollToPosition(0);
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
                if (!users.get(i).getEmail().matches(userFriend.getEmail()))
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

        mMultiChoiceRecyclerView.setRecyclerColumnNumber(1);

        personList = new ArrayList<>();
        personList.addAll(personQueryList);
        selectionPeopleAdapter = new SelectionPeopleAdapter(personQueryList, getApplication()) ;
        mMultiChoiceRecyclerView.setAdapter(selectionPeopleAdapter);
        mMultiChoiceRecyclerView.setSingleClickMode(true);

        mMultiChoiceRecyclerView.setMultiChoiceSelectionListener(mMultiChoiceSelectionListener);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);


        if(stock_list != null)
            for (i = 0; i < personList.size() && stock_list.size() > 0; i++)
            {
                User personModel = personList.get(i);
                if (stock_list.contains(personModel.getEmail())) {
                    mMultiChoiceRecyclerView.select(i);
                    personListSelected.add(personModel);
                }
            }
        setProgress(false);
    }

    private void handleError(Throwable error) {
        setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == applyButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "applyButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent();
            PersonModelWrapper wrapper = new PersonModelWrapper(personListSelected);
            intent.putExtra("guest_objs", wrapper);
            setResult(RESULT_OK, intent);
            finish();
        }else if(view == cleanButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    private List<User> filter(List<User> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<User> filteredModelList = new ArrayList<>();
        for (User model : models) {
            final String text = model.getName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void setSelectionQuery(){
        int i;
        int pos;
        for(i=0;i<personQueryList.size();i++){
            pos = getPositionSelected(personQueryList.get(i).getEmail());
            if(pos >= 0)
                mMultiChoiceRecyclerView.select(i);

        }

    }

    private int getPositionSelected(String name){
        int i;
        for(i=0;i<personListSelected.size();i++){
            String text = personListSelected.get(i).getEmail();
            if(text.matches(name))
                return i;
        }
        return -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptions.unsubscribe();
    }

}
