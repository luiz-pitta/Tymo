package io.development.tymo.fragments;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.CompareActivity;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.adapters.CompareAdapter;
import io.development.tymo.models.CompareModel;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompareFreeFragment extends Fragment{

    private RecyclerView recyclerView;
    private CompareAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    List<CompareModel> data = new ArrayList<>();

    public static Fragment newInstance(String text) {
        CompareFreeFragment fragment = new CompareFreeFragment();
        return fragment;
    }

    public CompareFreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_compare, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new CompareAdapter(data, getActivity(), true, (CompareActivity)getActivity());
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        recyclerView = (RecyclerView) view.findViewById(R.id.listPlans);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(true);

        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setNestedScrollingEnabled(false);

        CompareActivity activity = (CompareActivity)getActivity();
        List<CompareModel> list = activity.getListCompare();

        for (int i = 0; i < list.size(); i++){
            data.add(list.get(i));
            adapter.notifyItemInserted(i);
        }

        adapter.setDateList(activity.getTodayDate());

        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void setDataAdapter(List<CompareModel> list){
        adapter.clear();
        for (int i = 0; i < list.size(); i++){
            data.add(list.get(i));
            adapter.notifyItemInserted(i);
        }
    }

    public void updateDelete(int position){
        if(adapter!=null) {
            data.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

