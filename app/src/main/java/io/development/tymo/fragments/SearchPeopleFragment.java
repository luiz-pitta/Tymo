package io.development.tymo.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.adapters.SearchMultipleAdapter;
import io.development.tymo.R;
import io.development.tymo.utils.Utilities;


public class SearchPeopleFragment extends Fragment {

    private EasyRecyclerView mRecyclerView;
    private SearchMultipleAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Object> listPeople = new ArrayList<>();

    public SearchPeopleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        mRecyclerView = (EasyRecyclerView) view.findViewById(R.id.listSearch);
        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(true);

        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setProgressView(R.layout.progress_loading_list);

        adapter = new SearchMultipleAdapter(getActivity());

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                if(adapter.getCount() == 0)
                    mRecyclerView.showEmpty();
            }
        });

        mRecyclerView.setAdapterWithProgress(adapter);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.clear();
        adapter.addAll(listPeople);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
        if(listPeople.size() == 0)
            mRecyclerView.showProgress();
    }

    public void setAdapterItens(List<Object> list){
        listPeople.clear();
        listPeople.addAll(list);
        if(adapter != null) {
            adapter.clear();
            adapter.addAll(listPeople);
        }
    }

    public void showProgress(){
        if(mRecyclerView!=null)
            mRecyclerView.showProgress();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            adapter.clear();
            adapter.addAll(listPeople);
        }
    }

}