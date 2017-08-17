package io.development.tymo.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.MediaPlayer;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.ActivityAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import jp.wasabeef.recyclerview.animators.OvershootInRightAnimator;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener{

    private ItemTouchHelper.SimpleCallback simpleItemTouchCallback;
    private LinearLayout feed_empty_view;
    private ImageView emptyIcon;
    private TextView emptyText1, emptyText2;
    private EasyRecyclerView recyclerView;
    private ActivityAdapter adapter;
    private Paint p = new Paint();

    private int period = 3;
    private boolean lastPositionFit = false;

    private MediaPlayer mp;

    private boolean erase = true;
    private int scrolled = 0;
    int d_notify, m_notify, y_notify;

    private CompositeDisposable mSubscriptions;
    private FirebaseAnalytics mFirebaseAnalytics;

    private List<Object> listFeed = new ArrayList<>();

    private Handler handler = new Handler();

    public static Fragment newInstance(String text) {
        FeedListFragment fragment = new FeedListFragment();
        return fragment;
    }

    public FeedListFragment() {
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

        return inflater.inflate(R.layout.fragment_feed_zoom_less, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSubscriptions = new CompositeDisposable();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        recyclerView = (EasyRecyclerView) view.findViewById(R.id.list);
        feed_empty_view = (LinearLayout) view.findViewById(R.id.feed_empty_view);
        emptyIcon = (ImageView) view.findViewById(R.id.emptyIcon);
        emptyText1 = (TextView) view.findViewById(R.id.emptyText1);
        emptyText2 = (TextView) view.findViewById(R.id.emptyText2);

        recyclerView.setItemAnimator(new OvershootInRightAnimator());

        adapter = new ActivityAdapter(getActivity());

        adapter.setNoMore(R.layout.footer_feed_zoom_less);

        adapter.setOnItemClickListener(new RecyclerArrayAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Object object = adapter.getItem(position);
                FlagServer flagServer;
                ActivityServer activityServer;
                Intent myIntent;

                if(object instanceof FlagServer){
                    flagServer = (FlagServer) object;
                    myIntent = new Intent(getActivity(), FlagActivity.class);
                    myIntent.putExtra("type_flag", 1);
                    myIntent.putExtra("flag_show", new FlagWrapper(flagServer));
                }else{
                    activityServer = (ActivityServer) object;
                    myIntent = new Intent(getActivity(), ShowActivity.class);
                    myIntent.putExtra("act_show", new ActivityWrapper(activityServer));
                }

                startActivity(myIntent);
            }
        });

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrolled += dy;
            }
        });

        recyclerView.getSwipeToRefresh().setDistanceToTriggerSync(850);


        recyclerView.setRefreshListener(this);
        recyclerView.setAdapterWithProgress(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setRefreshingColor(ContextCompat.getColor(getActivity(),R.color.deep_purple_400));

        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);

        initSwipe();
    }

    public int getCurrentPosition(){
        float dy = Utilities.convertPixelsToDp(scrolled, getActivity());
        return (int)(dy/165);
    }

    public void setCurrentPosition(int position) {
        recyclerView.scrollToPosition(position);
        scrolled = (int)Utilities.convertDpToPixel(position*165, getActivity());
    }

    public void setPeriod(int period){
        this.period = period;
    }

    public int getPeriod(){
        return this.period;
    }

    public void setEmptyLayout(boolean empty){
        if(empty) {
            feed_empty_view.setVisibility(View.VISIBLE);
            if(period == 3) {
                emptyIcon.setColorFilter(getResources().getColor(R.color.white));
                emptyText1.setTextColor(getResources().getColor(R.color.white));
                emptyText2.setTextColor(getResources().getColor(R.color.white));
            }
            else{
                emptyIcon.setColorFilter(getResources().getColor(R.color.white));
                emptyText1.setTextColor(getResources().getColor(R.color.white));
                emptyText2.setTextColor(getResources().getColor(R.color.white));
            }
        }
        else {
            feed_empty_view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void setAdapterItens(List<Object> list){
        listFeed.clear();
        listFeed.addAll(list);
        adapter.clear();
        adapter.addAll(listFeed);
        setEmptyLayout(adapter.getCount() == 0);
    }

    @Override
    public void onRefresh() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                recyclerView.showProgress();
                setEmptyLayout(false);
                FeedFragment fragment = (FeedFragment)getActivity().getFragmentManager().findFragmentByTag("Feed_main");

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "refresh_feed" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                fragment.setFeedRefresh();
            }
        }, 1000);
    }

    public void setProgress(boolean progress) {
        if(recyclerView != null) {
            if (progress) {
                recyclerView.showProgress();
                setEmptyLayout(false);
            }
            else {
                recyclerView.showRecycler();
                setEmptyLayout(adapter.getCount() == 0);
            }
        }
    }

    private void updateInviteRequest(InviteRequest inviteRequest) {

        mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleDeleteIgnoreConfirm,this::handleError));
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
        //ACTIVITY_DELETED_SUCCESSFULLY, RELATIONSHIP_UPDATED_SUCCESSFULLY e WITHOUT_NOTIFICATION
    }

    private void handleError(Throwable error) {
        if(Utilities.isDeviceOnline(getActivity()))
            Toast.makeText(getActivity(), getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getActivity(), getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }

    private void initSwipe(){
        simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return super.getSwipeDirs(recyclerView, viewHolder);
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int swipeFlags = ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT;

                int swipe = (int)Utilities.convertPixelsToDp(viewHolder.itemView.getHeight(), getActivity());

                if(swipe == 100)
                    swipeFlags = 0;

                return makeMovementFlags(0, swipeFlags);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                lastPositionFit = false;
                Object item = adapter.getItem(position);
                Snackbar snackbar;

                adapter.remove(position);
                setEmptyLayout(adapter.getCount() == 0);

                if (direction == ItemTouchHelper.LEFT){

                    mp = MediaPlayer.create(getActivity(), R.raw.feed_ignore);
                    mp.start();

                    snackbar =  Snackbar.make(recyclerView,getResources().getString(R.string.feed_invitation_activity_ignored), Snackbar.LENGTH_LONG)
                        .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white))
                        .setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                adapter.insert(item, position);
                                erase = false;
                                if(position == 0)
                                    recyclerView.scrollToPosition(0);
                            }
                        });

                    snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);

                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.feed_invitation_activity_ignored) + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                            FeedFragment fragment = (FeedFragment)getActivity().getFragmentManager().findFragmentByTag("Feed_main");
                            if(fragment != null)
                                fragment.setAdapterItensCard(adapter.getAllData());
                        }
                    });

                    snackbar.show();

                } else if (direction == ItemTouchHelper.RIGHT){

                    int participates = 0;
                    if(item instanceof ActivityServer)
                        participates = ((ActivityServer)item).getParticipates();
                    else if(item instanceof FlagServer)
                        participates = ((FlagServer)item).getParticipates();

                    if (participates == 1){
                        mp = MediaPlayer.create(getActivity(), R.raw.feed_fit);
                        mp.start();

                        lastPositionFit = true;
                        snackbar = Snackbar.make(recyclerView, getResources().getString(R.string.feed_invitation_activity_swiped), Snackbar.LENGTH_LONG)
                                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white))
                                .setAction(getResources().getString(R.string.undo_unhide), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        adapter.insert(item, position);
                                        erase = false;
                                        if (position == 0)
                                            recyclerView.scrollToPosition(0);
                                    }
                                });
                    }else {
                        mp = MediaPlayer.create(getActivity(), R.raw.feed_fit);
                        mp.start();

                        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(200);

                        snackbar = Snackbar.make(recyclerView, getResources().getString(R.string.feed_invitation_activity_fit), Snackbar.LENGTH_LONG)
                                .setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white))
                                .setAction(getResources().getString(R.string.undo), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        adapter.insert(item, position);
                                        erase = false;
                                        if (position == 0)
                                            recyclerView.scrollToPosition(0);
                                    }
                                });
                    }

                    snackbar.show();


                    snackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            super.onDismissed(transientBottomBar, event);
                            InviteRequest inviteRequest = new InviteRequest();
                            ActivityServer activityServer;
                            FlagServer flagServer;

                            int participates = 0;
                            if(item instanceof ActivityServer) {
                                d_notify = ((ActivityServer) item).getDayStart();
                                m_notify = ((ActivityServer) item).getMonthStart();
                                y_notify = ((ActivityServer) item).getYearStart();
                                participates = ((ActivityServer) item).getParticipates();
                            }
                            else if(item instanceof FlagServer) {
                                d_notify = ((FlagServer) item).getDayStart();
                                m_notify = ((FlagServer) item).getMonthStart();
                                y_notify = ((FlagServer) item).getYearStart();
                                participates = ((FlagServer) item).getParticipates();
                            }

                            if (participates == 1)
                                erase = false;

                            if(erase) {

                                Bundle bundle = new Bundle();
                                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, getResources().getString(R.string.feed_invitation_activity_fit) + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                                SharedPreferences mSharedPreferences = getActivity().getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
                                String email = mSharedPreferences.getString(Constants.EMAIL, "");

                                inviteRequest.setEmail(email);
                                inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                                inviteRequest.setStatus(Constants.YES);

                                if(item instanceof ActivityServer){
                                    inviteRequest.setType(Constants.ACT);
                                    activityServer = (ActivityServer) item;
                                    inviteRequest.setIdAct(activityServer.getId());
                                }else if(item instanceof FlagServer){
                                    inviteRequest.setType(Constants.FLAG);
                                    flagServer = (FlagServer) item;
                                    inviteRequest.setIdAct(flagServer.getId());
                                }

                                updateInviteRequest(inviteRequest);
                            }

                            FeedFragment fragment = (FeedFragment)getActivity().getFragmentManager().findFragmentByTag("Feed_main");
                            if(fragment != null)
                                fragment.setAdapterItensCard(adapter.getAllData());

                            erase = true;
                        }
                    });
                }
            }


            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                float itemViewWidth = (float) itemView.getWidth();
                float alpha = 1.0f - Math.abs(dX) / itemViewWidth;
                float alphaIcon;
                float baseAlpha = (1.0f - alpha);

                if(baseAlpha > 0.4f)
                    alphaIcon = 255.0f;
                else
                    alphaIcon = 327.0f*baseAlpha + 124.0f;

                itemView.setAlpha(alpha);

                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX < 0){
                        c.rotate(2.5f, 0.5f, 0.5f);
                        int viewportCornerRadius = 10;

                        RectF frame = new RectF(
                                (float) itemView.getRight() - 2*width-Utilities.convertDpToPixel(28.5f,getActivity()),
                                (float) itemView.getTop() + width+Utilities.convertDpToPixel(2.5f,getActivity()),
                                (float) itemView.getRight() - width+Utilities.convertDpToPixel(28.5f,getActivity()),
                                (float) itemView.getBottom() - width-Utilities.convertDpToPixel(15.5f,getActivity()));

                        Path path = new Path();
                        Paint stroke = new Paint();
                        stroke.setAntiAlias(true);
                        stroke.setStrokeWidth(Utilities.convertDpToPixel(4,getActivity()));
                        stroke.setColor(ContextCompat.getColor(getActivity(),R.color.red_600));
                        stroke.setStyle(Paint.Style.STROKE);
                        stroke.setAlpha((int)alphaIcon);
                        path.addRoundRect(frame, (float) viewportCornerRadius, (float) viewportCornerRadius, Path.Direction.CW);
                        c.drawPath(path, stroke);

                        p.setAlpha((int)alphaIcon);
                        p.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(),R.color.red_600), PorterDuff.Mode.SRC_IN));

                        RectF icon_dest = new RectF(
                                (float) itemView.getRight() - 2*width,
                                (float) itemView.getTop() + width,
                                (float) itemView.getRight() - width,
                                (float) itemView.getBottom() - width);

                        String text = getResources().getString(R.string.ignore);
                        Rect r = new Rect();
                        p.getTextBounds(text, 0, text.length(), r);
                        p.setTextSize(Utilities.spToPixels(getActivity(), 18));
                        p.setTypeface(Typeface.DEFAULT_BOLD);

                        p.setTextAlign(Paint.Align.CENTER);
                        c.drawText(text.toUpperCase(),icon_dest.centerX()  , icon_dest.centerY(), p);

                    } else if(dX > 0){

                        boolean fit = false;

                        int position = viewHolder.getAdapterPosition();
                        if(position >= 0) {
                            Object item = adapter.getItem(position);

                            int participates = 0;
                            if (item instanceof ActivityServer)
                                participates = ((ActivityServer) item).getParticipates();
                            else if (item instanceof FlagServer)
                                participates = ((FlagServer) item).getParticipates();

                            if (participates == 1)
                                fit = true;
                        }else if(lastPositionFit)
                            fit = true;

                        c.rotate(-2.5f, 0.5f, 0.5f);
                        int viewportCornerRadius = 10;
                        RectF frame;

                        if(!fit) {
                            frame = new RectF(
                                    (float) itemView.getLeft() + width - Utilities.convertDpToPixel(28.5f, getActivity()),
                                    (float) itemView.getTop() + width + Utilities.convertDpToPixel(2.5f, getActivity()),
                                    (float) itemView.getLeft() + 2 * width + Utilities.convertDpToPixel(28.5f, getActivity()),
                                    (float) itemView.getBottom() - width - Utilities.convertDpToPixel(15.5f, getActivity()));
                        }else {
                            frame = new RectF(
                                    (float) itemView.getLeft() + width - Utilities.convertDpToPixel(37.5f, getActivity()),
                                    (float) itemView.getTop() + width + Utilities.convertDpToPixel(2.5f, getActivity()),
                                    (float) itemView.getLeft() + 2 * width + Utilities.convertDpToPixel(37.5f, getActivity()),
                                    (float) itemView.getBottom() - width - Utilities.convertDpToPixel(15.5f, getActivity()));
                        }

                        Path path = new Path();
                        String text;
                        Paint stroke = new Paint();
                        stroke.setAntiAlias(true);
                        stroke.setStrokeWidth(Utilities.convertDpToPixel(4,getActivity()));
                        if(!fit) {
                            stroke.setColor(ContextCompat.getColor(getActivity(), R.color.green_600));
                            text = getResources().getString(R.string.fit);
                        }
                        else {
                            stroke.setColor(ContextCompat.getColor(getActivity(), R.color.blue_600));
                            text = getResources().getString(R.string.feed_fit_already);
                        }
                        stroke.setStyle(Paint.Style.STROKE);
                        stroke.setAlpha((int)alphaIcon);
                        path.addRoundRect(frame, (float) viewportCornerRadius, (float) viewportCornerRadius, Path.Direction.CW);
                        c.drawPath(path, stroke);

                        p.setAlpha((int)alphaIcon);
                        if(!fit)
                            p.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(),R.color.green_600), PorterDuff.Mode.SRC_IN));
                        else
                            p.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getActivity(),R.color.blue_600), PorterDuff.Mode.SRC_IN));

                        RectF icon_dest = new RectF(
                                (float) itemView.getLeft() + width,
                                (float) itemView.getTop() + width,
                                (float) itemView.getLeft() + 2*width,
                                (float) itemView.getBottom() - width);


                        Rect r = new Rect();
                        p.getTextBounds(text, 0, text.length(), r);
                        p.setTextSize(Utilities.spToPixels(getActivity(), 18));
                        p.setTypeface(Typeface.DEFAULT_BOLD);

                        p.setTextAlign(Paint.Align.CENTER);
                        c.drawText(text.toUpperCase(),icon_dest.centerX()  , icon_dest.centerY(), p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView.getRecyclerView());
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }

    public EasyRecyclerView getRecyclerView(){
        return recyclerView;
    }
}

