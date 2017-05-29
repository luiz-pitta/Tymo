package io.development.tymo.fragments;

import android.app.Fragment;
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
import io.development.tymo.activities.ShowGuestsActivity;
import io.development.tymo.adapters.ShowGuestsAdapter;
import io.development.tymo.model_server.User;
import io.development.tymo.utils.Utilities;


public class FitPeopleFragment extends Fragment {

    private EasyRecyclerView mRecyclerView;
    private ShowGuestsAdapter adapter;
    private long idAct;
    private boolean isAdm, isFlag;
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<User> listPeople = new ArrayList<>();

    public FitPeopleFragment() {
        // Required empty public constructor
    }

    public void setIdAct(long id){
        idAct = id;
    }

    public void setAdm(boolean adm){
        isAdm = adm;
    }

    public void setFlag(boolean flag){
        isFlag = flag;
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

        adapter = new ShowGuestsAdapter(getActivity(), idAct, isAdm, isFlag);

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
    }

    public void setAdapterItens(List<User> list){
        listPeople.clear();
        listPeople.addAll(list);
        if(adapter != null) {
            adapter.clear();
            adapter.addAll(listPeople);
        }
    }

    public void showProgress(boolean search){
        if(mRecyclerView!=null) {
            if(!search)
                mRecyclerView.showProgress();
            else if(adapter.getCount() > 60)
                mRecyclerView.showProgress();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && adapter != null) {
            ArrayList<User> list = new ArrayList<>();
            ShowGuestsActivity showGuestsActivity = (ShowGuestsActivity) getActivity();
            ArrayList<User> listInvited = showGuestsActivity.getListInvitedUser();
            for (int i = 0; i < listInvited.size(); i++) {
                if (listInvited.get(i).getInvitation() == 1)
                    list.add(listInvited.get(i));
            }
            listPeople.clear();
            listPeople.addAll(list);
            adapter.clear();
            adapter.addAll(listPeople);
            adapter.notifyDataSetChanged();
            showGuestsActivity.setListConfirmedUser(list);

        }
    }

}