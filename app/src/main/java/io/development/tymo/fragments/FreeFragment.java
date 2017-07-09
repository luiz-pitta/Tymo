package io.development.tymo.fragments;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.adapters.PlansAdapter;
import io.development.tymo.models.WeekModel;
import io.development.tymo.utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class FreeFragment extends Fragment {

    private PlansAdapter weekAdapter;
    private EasyRecyclerView recyclerView;
    private List<WeekModel> data = new ArrayList<>();
    private int screen;
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance() {
        FreeFragment fragment = new FreeFragment();
        return fragment;
    }

    public FreeFragment() {
        // Required empty public constructor
    }

    public void setScreen(int screen){
        this.screen = screen;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_plans, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (EasyRecyclerView) view.findViewById(R.id.listPlans);
        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int) Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(false);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        List<WeekModel> list;

        PlansFragment fragment = (PlansFragment)getActivity().getFragmentManager().findFragmentByTag("Plans_main");

        if(fragment == null) {
            FriendProfileActivity parent = (FriendProfileActivity)getActivity();
            list = parent.getListPlans();
            screen = Utilities.TYPE_FRIEND;
            weekAdapter = new PlansAdapter(view.getContext(), screen, (FriendProfileActivity)getActivity(), parent.getUserFriend(), true);
            recyclerView.setEmptyView(R.layout.empty_profile_privated);
        }
        else {
            list = fragment.getListPlans();
            screen = Utilities.TYPE_PLANS;
            weekAdapter = new PlansAdapter(view.getContext(), screen, fragment, null, true);
            recyclerView.setEmptyView(R.layout.empty_commitments_of_the_day);
        }

        recyclerView.getRecyclerView().setNestedScrollingEnabled(false);

        recyclerView.setAdapterWithProgress(weekAdapter);
        recyclerView.showProgress();

        if(list != null && list.size() == 0)
            recyclerView.showEmpty();

        weekAdapter.clear();
        weekAdapter.addAll(list);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    public void showProgressFree(){
        if(recyclerView != null) {
            weekAdapter.clear();
            recyclerView.showProgress();
        }
    }

    public void setDataAdapter(List<WeekModel> list){
        weekAdapter.clear();
        weekAdapter.addAll(list);
        recyclerView.scrollToPosition(0);

        if(list.size() == 0)
            recyclerView.showEmpty();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


}
