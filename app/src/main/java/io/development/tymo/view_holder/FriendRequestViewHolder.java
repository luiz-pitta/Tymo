package io.development.tymo.view_holder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.io.IOException;
import java.util.ArrayList;

import io.development.tymo.R;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.ActivityAdapter;
import io.development.tymo.adapters.FriendResquestAdapter;
import io.development.tymo.model_server.FriendRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.models.FeedCubeModel;
import io.development.tymo.models.FriendRequestModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.ServerMessage;
import io.development.tymo.utils.Utilities;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;


public class FriendRequestViewHolder extends BaseViewHolder<FriendRequestModel> implements View.OnClickListener {
    private RelativeLayout resquestInvite;
    private TextView text1, text2, text3, text4, ignoreButton, acceptButton;
    private ImageView profilePhoto, moreVerticalIcon;
    private ProgressBar ignoreAcceptProgressBar;
    private Context context;

    private FirebaseAnalytics mFirebaseAnalytics;

    private RelativeLayout mainBox;

    private String email_friend;

    private CompositeSubscription mSubscriptions;
    private SharedPreferences mSharedPreferences;


    public FriendRequestViewHolder(ViewGroup parent, final Context context) {
        super(parent, R.layout.list_item_invitation);

        resquestInvite = $(R.id.resquestInvite);
        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        mainBox = $(R.id.mainBox);
        moreVerticalIcon = $(R.id.moreVerticalIcon);
        ignoreButton = $(R.id.ignoreButton);
        acceptButton = $(R.id.acceptButton);
        profilePhoto = $(R.id.profilePhoto);
        ignoreAcceptProgressBar = $(R.id.ignoreAcceptProgressBar);
        this.context = context;

        moreVerticalIcon.setVisibility(View.GONE);
        $(R.id.pieceBox).setVisibility(View.GONE);
        //resquestInvite.getLayoutParams().height = (int) Utilities.convertDpToPixel(120, context);

        acceptButton.setOnClickListener(this);
        ignoreButton.setOnClickListener(this);
        mainBox.setOnClickListener(this);

        mSubscriptions = new CompositeSubscription();
        mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public void setProgress(boolean progress) {
        if (progress) {
            ignoreButton.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);
            ignoreAcceptProgressBar.setVisibility(View.VISIBLE);
        } else {
            ignoreButton.setVisibility(View.VISIBLE);
            acceptButton.setVisibility(View.VISIBLE);
            ignoreAcceptProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        FriendRequest friendRequest = new FriendRequest();
        String email = mSharedPreferences.getString(Constants.EMAIL, "");
        friendRequest.setEmail(email);
        friendRequest.setEmailFriend(email_friend);
        if (v == acceptButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "acceptButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            text4.setText(context.getResources().getString(R.string.request_to_add_accepted, fullNameToShortName((String) text1.getText())));
            friendRequest.setStatus(1);
            updateFriendRequest(friendRequest);
        } else if (v == ignoreButton) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "ignoreButton");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            text4.setText(context.getResources().getString(R.string.request_to_add_ignore));
            friendRequest.setStatus(0);
            updateFriendRequest(friendRequest);
        } else if (v == mainBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "mainBox");
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent = new Intent(context, FriendProfileActivity.class);
            intent.putExtra("friend_email", email_friend);
            intent.putExtra("name", (String) text1.getText());
            context.startActivity(intent);
        }
    }

    private String fullNameToShortName(String fullName) {
        String[] fullNameSplited = fullName.split(" ");
        return fullNameSplited[0];
    }

    private void updateFriendRequest(FriendRequest friendRequest) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateFriendRequest(friendRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);
        FriendResquestAdapter friendResquestAdapter = getOwnerAdapter();
        friendResquestAdapter.getItem(getAdapterPosition()).setRequestAccepted(true);
        acceptButton.setVisibility(View.GONE);
        ignoreButton.setVisibility(View.GONE);
        text4.setVisibility(View.VISIBLE);
        //Toast.makeText(context, ServerMessage.getServerMessage(context, response.getMessage()), Toast.LENGTH_LONG).show();
        //RELATIONSHIP_UPDATED_SUCCESSFULLY, WITHOUT_NOTIFICATION e REQUEST_TO_ADD_ACCEPTED
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(context, context.getResources().getString(R.string.network_error), Toast.LENGTH_LONG).show();
    }


    @Override
    public void setData(FriendRequestModel friend) {
        if(!friend.isRequestAccepted()) {
            acceptButton.setVisibility(View.VISIBLE);
            acceptButton.setText(context.getResources().getString(R.string.add));
            ignoreButton.setVisibility(View.VISIBLE);
            text4.setVisibility(View.GONE);
        }else{
            acceptButton.setVisibility(View.GONE);
            ignoreButton.setVisibility(View.GONE);
            text4.setVisibility(View.VISIBLE);
        }

        if (!friend.getText1().matches("")) {
            if (friend.getText3().matches("accept")) {
                text1.setText(friend.getText1());
                text4.setVisibility(View.VISIBLE);
                text4.setText(context.getResources().getString(R.string.invitation_request_accepted, fullNameToShortName((String) text1.getText())));
                acceptButton.setVisibility(View.GONE);
                ignoreButton.setVisibility(View.GONE);
            } else {
                text1.setText(friend.getText1());
                acceptButton.setVisibility(View.VISIBLE);
                ignoreButton.setVisibility(View.VISIBLE);
            }
            text1.setVisibility(View.VISIBLE);
        } else
            text1.setVisibility(View.GONE);

        if (!friend.getText2().matches("")) {
            text2.setText(friend.getText2());
            text2.setVisibility(View.VISIBLE);
        } else
            text2.setVisibility(View.GONE);

        if (!friend.getText3().matches("")) {
            if (!friend.getText3().matches("accept")) {
                text3.setText(friend.getText3());
                text3.setVisibility(View.VISIBLE);
            } else
                text3.setVisibility(View.GONE);
        } else
            text3.setVisibility(View.GONE);

        email_friend = friend.getEmail();

        if (!friend.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(context)
                    .load(friend.getPhoto())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

    }


}
