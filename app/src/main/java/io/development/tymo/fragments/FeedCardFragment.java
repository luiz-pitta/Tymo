package io.development.tymo.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.TymoApplication;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.MainActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.FeedZoomMoreAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.RecyclerItemClickListener;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedCardFragment extends Fragment {

    private RecyclerViewPager mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayout feed_empty_view;
    private Handler handler = new Handler();

    private boolean erase = true;

    private FeedZoomMoreAdapter adapter;
    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    int d_notify, m_notify, y_notify;

    private List<Object> listFeed = new ArrayList<>();

    public static Fragment newInstance(String text) {
        FeedCardFragment fragment = new FeedCardFragment();
        return fragment;
    }

    public FeedCardFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_zoom_more, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = (RecyclerViewPager) view.findViewById(R.id.viewpager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        feed_empty_view = (LinearLayout) view.findViewById(R.id.feed_empty_view);

        mSwipeRefreshLayout.setDistanceToTriggerSync(400);

        mSubscriptions = new CompositeDisposable();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        LinearLayoutManager layout = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL,
                false);
        mRecyclerView.setLayoutManager(layout);

        mRecyclerView.setTriggerOffset(0.35f);
        mRecyclerView.setSinglePageFling(true);
        mRecyclerView.setAdapter(adapter = new FeedZoomMoreAdapter(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);

        mRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {
                adapter.setCurrentItemId(newPosition);

                Object item = adapter.getCurrentItem();

                int participates = 0;
                if(item instanceof ActivityServer)
                    participates = ((ActivityServer)item).getParticipates();
                else if(item instanceof FlagServer)
                    participates = ((FlagServer)item).getParticipates();

                FeedFragment fragment = (FeedFragment)getActivity().getFragmentManager().findFragmentByTag("Feed_main");

                fragment.setFeedIgnoreCheckButton(participates == 1);

                if(adapter.getPeoplePositionSize(newPosition) == 0) {
                    Object object = adapter.getCurrentItem();
                    if (object instanceof ActivityServer) {
                        ActivityServer activityServer = new ActivityServer();
                        activityServer.setId(newPosition);
                        activityServer.setCreator("");
                        setActivityInformation(((ActivityServer) object).getId(), activityServer);
                    } else {
                        FlagServer flagServer = new FlagServer();
                        flagServer.setId(newPosition);
                        setFlagInformation(((FlagServer) object).getId(), flagServer);
                    }
                }
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding = (mRecyclerView.getWidth() - width) / 2;

                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    float rate = 0;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                    } else
                        v.setScaleY(0.9f);

                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                refreshItems();
            }
        });

        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(),R.color.deep_purple_400));

        MainActivity activity = (MainActivity)getActivity();
        FeedFragment fragment = (FeedFragment)activity.getFragmentNavigator().getFragment(0);
        setAdapterItens(fragment.getListFeed());

        double lat = TymoApplication.getInstance().getLatLng().get(0);
        double lng = TymoApplication.getInstance().getLatLng().get(1);

        mRecyclerView.scrollToPosition(fragment.getCurrentPosition());

        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        setLatLng(lat, lng);
    }

    public void setLatLng(double lat, double lng) {
        if (adapter!=null)
            adapter.setLatLng(lat, lng);
    }

    public void setCurrentPosition(int position) {
        if(mRecyclerView!=null)
            mRecyclerView.scrollToPosition(position);
    }

    public int getCurrentPosition(){
        if(adapter !=null)
            return adapter.getCurrentItemId();
        else
            return 0;
    }

    public List<Object> getListFeed(){
        return adapter.getAllData();
    }


    private void updateInviteRequest(InviteRequest inviteRequest) {

        mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm,this::handleError));
    }

    private void ignoreActivityRequest(InviteRequest inviteRequest) {

        mSubscriptions.add(NetworkUtil.getRetrofit().ignoreActivity(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm,this::handleError));
    }

    private void setFlagInformation(long id, FlagServer flagServer) {
        flagServer.setCreator("");
        mSubscriptions.add(NetworkUtil.getRetrofit().getFlag2(id, flagServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void setActivityInformation(long id, ActivityServer activityServer) {

        mSubscriptions.add(NetworkUtil.getRetrofit().getActivity2(id, activityServer)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    private void handleResponse(Response response) {
        if(adapter.getItemCount() > 0 || adapter.getItemCount() > Integer.valueOf(response.getMessage()))
            adapter.addPeople(response.getPeople(), Integer.valueOf(response.getMessage()));
    }

    private void handleDeleteIgnoreConfirm(Response response) {
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);
        int month = c.get(Calendar.MONTH) + 1;
        int year = c.get(Calendar.YEAR);

        Calendar c2 = Calendar.getInstance();
        c2.add(Calendar.DATE, 1);
        int day2 = c2.get(Calendar.DAY_OF_MONTH);
        int month2 = c2.get(Calendar.MONTH) + 1;
        int year2 = c2.get(Calendar.YEAR);

        if((d_notify == day && m_notify == month && y_notify == year) || (d_notify == day2 && m_notify == month2 && y_notify == year2)) {
            d_notify = -1;
            m_notify = -1;
            y_notify = -1;
            Intent intent = new Intent("notification_update");
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        }
        //Toast.makeText(getActivity(), ServerMessage.getServerMessage(getActivity(), response.getMessage()), Toast.LENGTH_LONG).show();
    }

    private void handleError(Throwable error) {
        if(!Utilities.isDeviceOnline(getActivity()))
            Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    public void insertActivityBack(Object activity, int position){
        adapter.addItem(activity, position);
        mRecyclerView.scrollToPosition(position);
    }

    public void confirmActivity(){
        Object item = adapter.getCurrentItem();
        Snackbar snackbar;
        int currentPosition = adapter.getCurrentItemId();

        if(item instanceof ActivityServer){
            ActivityServer activityServer = (ActivityServer)item;
            d_notify = activityServer.getDayStart();
            m_notify = activityServer.getMonthStart();
            y_notify = activityServer.getYearStart();
        }else if(item instanceof FlagServer){
            FlagServer flagServer = (FlagServer)item;
            d_notify = flagServer.getDayStart();
            m_notify = flagServer.getMonthStart();
            y_notify = flagServer.getYearStart();
        }

        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);

        if(item != null) {
            adapter.removeCurrentItem();
            if (adapter.getCurrentItemId() > 0)
                adapter.setCurrentItemId(adapter.getCurrentItemId() - 1);

            ArrayList<Object> listActivities = new ArrayList<>();
            FeedFragment feedFragment = (FeedFragment) getActivity().getFragmentManager().findFragmentByTag("Feed_main");

            if (item instanceof ActivityServer) {
                ActivityServer activityServer = (ActivityServer) item;
                listActivities.add(activityServer);
            } else if (item instanceof FlagServer) {
                FlagServer flagServer = (FlagServer) item;
                listActivities.add(flagServer);
            }

            if (listActivities.size() > 1)
                feedFragment.createDialogRepeatImport(listActivities, currentPosition, 1);
            else {
                snackbar = Snackbar.make(mRecyclerView, R.string.feed_invitation_activity_fit, Snackbar.LENGTH_LONG)
                        .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white))
                        .setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter.addItem(item, currentPosition);
                                mRecyclerView.scrollToPosition(currentPosition);
                                erase = false;
                            }
                        });

                snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);

                        InviteRequest inviteRequest = new InviteRequest();
                        ActivityServer activityServer;
                        FlagServer flagServer;

                        if (erase) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.feed_invitation_activity_fit) + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                            String email = mSharedPreferences.getString(Constants.EMAIL, "");

                            inviteRequest.setEmail(email);
                            inviteRequest.setStatus(Constants.YES);
                            inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

                            if (item instanceof ActivityServer) {
                                inviteRequest.setType(Constants.ACT);
                                activityServer = (ActivityServer) item;
                                inviteRequest.setIdAct(activityServer.getId());
                            } else if(item instanceof FlagServer){
                                inviteRequest.setType(Constants.FLAG);
                                flagServer = (FlagServer) item;
                                inviteRequest.setIdAct(flagServer.getId());
                            }

                            updateInviteRequest(inviteRequest);

                            FeedListFragment feedListFragment = (FeedListFragment) getFragmentManager().findFragmentByTag("list");
                            if (feedListFragment != null)
                                feedListFragment.setAdapterItens(adapter.getAllData());
                        }

                        erase = true;

                        setEmptyLayout(adapter.getItemCount() == 0);
                    }
                });

                snackbar.show();
            }
        }
    }

    public void rejectActivity(){
        Object item = adapter.getCurrentItem();
        Snackbar snackbar;
        int currentPosition = adapter.getCurrentItemId();

        if(item instanceof ActivityServer){
            ActivityServer activityServer = (ActivityServer)item;
            d_notify = activityServer.getDayStart();
            m_notify = activityServer.getMonthStart();
            y_notify = activityServer.getYearStart();
        }else if(item instanceof FlagServer){
            FlagServer flagServer = (FlagServer)item;
            d_notify = flagServer.getDayStart();
            m_notify = flagServer.getMonthStart();
            y_notify = flagServer.getYearStart();
        }

        if(item != null) {
            adapter.removeCurrentItem();
            if (adapter.getCurrentItemId() > 0)
                adapter.setCurrentItemId(adapter.getCurrentItemId() - 1);

            snackbar = Snackbar.make(mRecyclerView, R.string.feed_invitation_activity_ignored, Snackbar.LENGTH_LONG)
                    .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white))
                    .setAction(R.string.undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            adapter.addItem(item, currentPosition);

                            mRecyclerView.scrollToPosition(currentPosition);
                            erase = false;
                        }
                    });

            snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);

                    if (erase) {
                        ActivityServer activityServer = null;
                        FlagServer flagServer = null;
                        InviteRequest inviteRequest = new InviteRequest();

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.feed_invitation_activity_ignored) + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                        SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                        String email = mSharedPreferences.getString(Constants.EMAIL, "");

                        inviteRequest.setEmail(email);
                        inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

                        if (item instanceof ActivityServer) {
                            inviteRequest.setType(Constants.ACT);
                            activityServer = (ActivityServer) item;
                            inviteRequest.setIdAct(activityServer.getId());
                        } else if (item instanceof FlagServer) {
                            inviteRequest.setType(Constants.FLAG);
                            flagServer = (FlagServer) item;
                            inviteRequest.setIdAct(flagServer.getId());
                        }

                        ignoreActivityRequest(inviteRequest);

                        FeedListFragment feedListFragment = (FeedListFragment) getFragmentManager().findFragmentByTag("list");
                        if (feedListFragment != null)
                            feedListFragment.setAdapterItens(adapter.getAllData());
                    }

                    erase = true;

                    setEmptyLayout(adapter.getItemCount() == 0);
                }
            });

            snackbar.show();
        }
    }

    public void setAdapterItens(List<Object> list){
        listFeed.clear();
        adapter.clear();
        listFeed.addAll(list);
        for(int i=0;i<listFeed.size();i++)
            adapter.addItem(listFeed.get(i));

        adapter.setCurrentItemId(0);
        Object object = adapter.getCurrentItem();
        if(object != null) {
            if (object instanceof ActivityServer) {
                ActivityServer activityServer = new ActivityServer();
                activityServer.setId(adapter.getCurrentItemId());
                activityServer.setCreator("");
                setActivityInformation(((ActivityServer) object).getId(), activityServer);
            } else {
                FlagServer flagServer = new FlagServer();
                flagServer.setId(adapter.getCurrentItemId());
                setFlagInformation(((FlagServer) object).getId(), flagServer);
            }
        }

        setEmptyLayout(adapter.getItemCount() == 0);

        mRecyclerView.smoothScrollToPosition(0);
    }

    public void setEmptyLayout(boolean empty){
        if(empty)
            feed_empty_view.setVisibility(View.VISIBLE);
        else
            feed_empty_view.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    void refreshItems() {
        // Load items
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setEmptyLayout(true);
                adapter.clear();
                FeedFragment fragment = (FeedFragment)getActivity().getFragmentManager().findFragmentByTag("Feed_main");

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refresh_feed" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                adapter.setCurrentItemId(0);

                fragment.setFeedRefresh();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 300);

        // Load complete
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }
}

