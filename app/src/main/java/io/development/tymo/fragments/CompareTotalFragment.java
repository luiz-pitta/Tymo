package io.development.tymo.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import io.development.tymo.R;
import io.development.tymo.activities.CompareActivity;
import io.development.tymo.adapters.CompareAdapter;
import io.development.tymo.models.CompareModel;
import io.development.tymo.utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class CompareTotalFragment extends Fragment{

    private RecyclerView recyclerView;
    private CompareAdapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    List<CompareModel> data = new ArrayList<>();

    public static Fragment newInstance(String text) {
        CompareTotalFragment fragment = new CompareTotalFragment();
        return fragment;
    }

    public CompareTotalFragment() {
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

        recyclerView = (RecyclerView) view.findViewById(R.id.listPlans);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(true);

        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapter = new CompareAdapter(data, getActivity(), false, (CompareActivity)getActivity());

        recyclerView.setAdapter(adapter);

        CompareActivity activity = (CompareActivity)getActivity();
        List<CompareModel> list = activity.getListCompare();

        for (int i = 0; i < list.size(); i++){
            data.add(list.get(i));
            adapter.notifyItemInserted(i);
        }

        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);
    }

    public void setDataAdapter(List<CompareModel> list){
        adapter.clear();
        for (int i = 0; i < list.size(); i++){
            data.add(list.get(i));
            adapter.notifyItemInserted(i);
        }
    }

    public void updateDelete(int position){
        data.remove(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

