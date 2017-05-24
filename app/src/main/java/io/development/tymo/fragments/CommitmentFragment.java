package io.development.tymo.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;

import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.adapters.PlansAdapter;
import io.development.tymo.models.WeekModel;
import io.development.tymo.utils.CreatePopUpDialogFragment;
import io.development.tymo.utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommitmentFragment extends Fragment{

    private EasyRecyclerView recyclerView;
    private PlansAdapter weekAdapter;
    private static int screen;
    private FirebaseAnalytics mFirebaseAnalytics;
    private CreatePopUpDialogFragment.RefreshLayoutPlansCallback callback;

    public static Fragment newInstance(int s) {
        CommitmentFragment fragment = new CommitmentFragment();
        screen = s;
        return fragment;
    }

    public CommitmentFragment() {
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

        return inflater.inflate(R.layout.fragment_plans, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = (EasyRecyclerView) view.findViewById(R.id.listPlans);

        DividerDecoration itemDecoration = new DividerDecoration(ContextCompat.getColor(getActivity(),R.color.horizontal_line), (int)Utilities.convertDpToPixel(1, getActivity()));
        itemDecoration.setDrawLastItem(false);

        PlansFragment fragment = (PlansFragment)getActivity().getFragmentManager().findFragmentByTag("Plans_main");

        if(fragment == null)
            recyclerView.setEmptyView(R.layout.empty_plans_private);
        else
            recyclerView.setEmptyView(R.layout.empty_timer);

        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if(screen == Utilities.TYPE_FRIEND)
            callback = (FriendProfileActivity)getActivity();
        else
            callback = (PlansFragment)getActivity().getFragmentManager().findFragmentByTag("Plans_main");

        weekAdapter = new PlansAdapter(view.getContext(), screen, callback, null, false);

        recyclerView.setAdapterWithProgress(weekAdapter);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);
    }

    public void showProgressCommitment(){
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

        PlansFragment fragment = (PlansFragment)getActivity().getFragmentManager().findFragmentByTag("Plans_main");

        if(fragment == null) {
            FriendProfileActivity parent = (FriendProfileActivity)getActivity();
            parent.getScrollView().smoothScrollTo(0,0);
        }
        else
            fragment.getScrollView().smoothScrollTo(0,0);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}

