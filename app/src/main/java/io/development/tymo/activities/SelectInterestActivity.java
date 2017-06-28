package io.development.tymo.activities;

import android.content.Intent;
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
import io.development.tymo.adapters.SelectionTagAdapter;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;

public class SelectInterestActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView applyButton, cleanButton;
    private RecyclerView mMultiChoiceRecyclerView;
    private ArrayList<String> tagQueryList, tagList, tagListSelected;
    private SearchView searchView;
    private SelectionTagAdapter selectionTagAdapter;
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
            if(selectionTagAdapter != null && selectionTagAdapter.getItemCount() > 60)
                setProgress(true);
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

        mSwipeRefreshLayout.setDistanceToTriggerSync(225);

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

        mMultiChoiceRecyclerView = (RecyclerView) findViewById(R.id.recyclerSelectView);
        mMultiChoiceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMultiChoiceRecyclerView.setNestedScrollingEnabled(false);

        setUpMultiChoiceRecyclerView();

        tagListSelected = new ArrayList<>();

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
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refreshItems" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
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
                selectionTagAdapter.deselectAll();
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
        mSubscriptions.add(NetworkUtil.getRetrofit().getInterest()
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

        tagList = new ArrayList<>();
        tagList.addAll(tagQueryList);
        selectionTagAdapter = new SelectionTagAdapter(tagQueryList, this) ;
        mMultiChoiceRecyclerView.setAdapter(selectionTagAdapter);
        selectionTagAdapter.setSingleClickMode(true);

        selectionTagAdapter.setMultiChoiceSelectionListener(new MultiChoiceAdapter.Listener() {
            @Override
            public void OnItemSelected(int selectedPosition, int itemSelectedCount, int allItemCount) {
                if(!tagListSelected.contains(tagQueryList.get(selectedPosition)))
                    tagListSelected.add(tagQueryList.get(selectedPosition));
            }

            @Override
            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
                int position = getPositionSelected(tagQueryList.get(deselectedPosition));
                if(position >= 0)
                    tagListSelected.remove(getPositionSelected(tagQueryList.get(deselectedPosition)));
            }

            @Override
            public void OnSelectAll(int itemSelectedCount, int allItemCount) {

            }

            @Override
            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {

            }
        });

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(this,R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, this));

        mMultiChoiceRecyclerView.addItemDecoration(itemDecoration);

        List<String> stock_list = getIntent().getStringArrayListExtra("tags_list");
        for(i = 0; i < tagList.size() && stock_list.size() > 0; i++){
            if(stock_list.contains(tagList.get(i)))
                selectionTagAdapter.select(i);

        }

        if(selectionTagAdapter.getItemCount() > 60) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setProgress(false);
                }
            }, 1500);
        }else
            setProgress(false);
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(this, getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if(view == applyButton){
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "applyButton" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent();
            intent.putStringArrayListExtra("tags_objs", tagListSelected);
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

    private List<String> filter(List<String> models, String query) {
        if(models == null)
            return new ArrayList<>();

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
                selectionTagAdapter.select(i);

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
        mSubscriptions.dispose();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}