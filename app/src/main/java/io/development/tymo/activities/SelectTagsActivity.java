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
import io.development.tymo.adapters.SelectionTagAdapter;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

public class SelectTagsActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView applyButton, cleanButton;
    private MultiChoiceRecyclerView mMultiChoiceRecyclerView;
    private ArrayList<String> tagQueryList, tagList, tagListSelected;
    private SearchView searchView;
    private SelectionTagAdapter selectionTagAdapter;
    private CompositeSubscription mSubscriptions;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Handler handler = new Handler();

    private FirebaseAnalytics mFirebaseAnalytics;

    private MultiChoiceSelectionListener mMultiChoiceSelectionListener = new MultiChoiceSelectionListener() {
        @Override
        public void OnItemSelected(final int selectedPosition, int itemSelectedCount, int allItemCount) {
            tagListSelected.add(tagQueryList.get(selectedPosition));
        }

        @Override
        public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
            tagListSelected.remove(getPositionSelected(tagQueryList.get(deselectedPosition)));
        }

        @Override
        public void OnSelectAll(int itemSelectedCount, int allItemCount) {

        }

        @Override
        public void OnDeselectAll(int itemSelectedCount, int allItemCount) {

        }
    };
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
        int magId = getResources().getIdentifier("android:id/search_mag_icon", null, null);
        ImageView magImage = (ImageView) searchView.findViewById(magId);
        magImage.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        magImage.setVisibility(GONE);

        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(mOnQueryTextListener);
        //search bar end

        mMultiChoiceRecyclerView = (MultiChoiceRecyclerView) findViewById(R.id.recyclerSelectView);

        mMultiChoiceRecyclerView.setNestedScrollingEnabled(false);

        setUpMultiChoiceRecyclerView();

        tagListSelected = new ArrayList<>();

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
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + getClass().getSimpleName());
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                mSwipeRefreshLayout.setRefreshing(false);
                setUpMultiChoiceRecyclerView();
            }
        }, 500);

        // Load complete
    }

    public void executeFilter(String query) {
        // Load items
        handler.post(new Runnable() {
            @Override
            public void run() {
                List<String> filteredModelList = filter(tagList, query);
                selectionTagAdapter.swap(filteredModelList);
                mMultiChoiceRecyclerView.deselectAll();
                mMultiChoiceRecyclerView.scrollToPosition(0);
                setSelectionQuery();
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

    private void setUpMultiChoiceRecyclerView() {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().getTags()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(ArrayList<TagServer> tags) {

        int i;
        tagQueryList = new ArrayList<>();
        for(i = 0; i < tags.size(); i++){
            tagQueryList.add(tags.get(i).getTitle());
        }

        mMultiChoiceRecyclerView.setRecyclerColumnNumber(1);

        tagList = new ArrayList<>();
        tagList.addAll(tagQueryList);
        selectionTagAdapter = new SelectionTagAdapter(tagQueryList, this) ;
        mMultiChoiceRecyclerView.setAdapter(selectionTagAdapter);
        mMultiChoiceRecyclerView.setSingleClickMode(true);

        mMultiChoiceRecyclerView.setMultiChoiceSelectionListener(mMultiChoiceSelectionListener);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);

        List<String> stock_list = getIntent().getStringArrayListExtra("tags_list");
        for(i = 0; i < tagList.size() && stock_list.size() > 0; i++){
            if(stock_list.contains(tagList.get(i))) {
                mMultiChoiceRecyclerView.select(i);
                tagListSelected.add(tagList.get(i));
            }
        }
        setProgress(false);
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == applyButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "applyButton" + getClass().getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent();
            intent.putStringArrayListExtra("tags_objs", tagListSelected);
            setResult(RESULT_OK, intent);
            finish();
        }else if(view == cleanButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "cleanButton" + getClass().getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private List<String> filter(List<String> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<String> filteredModelList = new ArrayList<>();
        for (String model : models) {
            final String text = model.toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void setSelectionQuery(){
        int i;
        int pos;
        for(i=0;i<tagQueryList.size();i++){
            pos = getPositionSelected(tagQueryList.get(i));
            if(pos >= 0)
                mMultiChoiceRecyclerView.select(i);

        }

    }

    private int getPositionSelected(String name){
        int i;
        for(i=0;i<tagListSelected.size();i++){
            String text = tagListSelected.get(i);
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
