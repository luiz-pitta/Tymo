package io.development.tymo.view_holder;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.util.Calendar;

import io.development.tymo.R;
import io.development.tymo.activities.FlagActivity;
import io.development.tymo.activities.ShowActivity;
import io.development.tymo.adapters.InviteAdapter;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.ActivityWrapper;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.model_server.FlagWrapper;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.models.InviteModel;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


public class InviteViewHolder extends BaseViewHolder<InviteModel> implements View.OnClickListener {
    private TextView text1, text2, text3, text4, ignoreButton, acceptButton;
    private ImageView pieceIcon, cubeLowerBoxIcon, cubeUpperBoxIcon, itemIcon, moreVerticalIcon;
    private RelativeLayout pieceBox, itemImage;
    private ProgressBar ignoreAcceptProgressBar;
    private Context context;

    private RelativeLayout resquestInvite;

    private FirebaseAnalytics mFirebaseAnalytics;

    private long id_act_flag;
    private Object object;
    private int type;

    private CompositeDisposable mSubscriptions;
    private SharedPreferences mSharedPreferences;


    public InviteViewHolder(ViewGroup parent, final Context context) {
        super(parent, R.layout.list_item_invitation);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        moreVerticalIcon = $(R.id.moreVerticalIcon);
        ignoreButton = $(R.id.ignoreButton);
        acceptButton = $(R.id.acceptButton);
        pieceIcon = $(R.id.pieceIcon);
        resquestInvite = $(R.id.resquestInvite);
        itemImage = $(R.id.itemImage);
        cubeLowerBoxIcon = $(R.id.cubeLowerBoxIcon);
        cubeUpperBoxIcon = $(R.id.cubeUpperBoxIcon);
        pieceBox = $(R.id.pieceBox);
        itemIcon = $(R.id.itemIcon);
        ignoreAcceptProgressBar = $(R.id.ignoreAcceptProgressBar);
        this.context = context;

        moreVerticalIcon.setVisibility(View.GONE);
        itemIcon.setVisibility(View.GONE);
        $(R.id.profilePhotoBox).setVisibility(View.GONE);

        acceptButton.setOnClickListener(this);
        ignoreButton.setOnClickListener(this);
        resquestInvite.setOnClickListener(this);

        mSubscriptions = new CompositeDisposable();
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
        if (v == resquestInvite) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "resquestInvite" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent intent;
            if (type == Constants.ACT) {
                intent = new Intent(context, ShowActivity.class);
                intent.putExtra("act_show", new ActivityWrapper((ActivityServer) object));
            } else {
                intent = new Intent(context, FlagActivity.class);
                intent.putExtra("type_flag", 1);
                intent.putExtra("flag_show", new FlagWrapper((FlagServer) object));
            }
            context.startActivity(intent);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "inviteRequest" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            InviteRequest inviteRequest = new InviteRequest();
            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            inviteRequest.setEmail(email);
            inviteRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            inviteRequest.setIdAct(id_act_flag);
            inviteRequest.setType(type);
            if (v == acceptButton) {
                text4.setText(context.getString(R.string.response_invitation_accepted));
                inviteRequest.setStatus(Constants.YES);
            } else if (v == ignoreButton) {
                text4.setText(context.getString(R.string.response_invitation_ignored));
                inviteRequest.setStatus(Constants.NO);
            }

            updateInviteRequest(inviteRequest);
        }
    }

    private void updateInviteRequest(InviteRequest inviteRequest) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateInviteRequest(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse, this::handleError));
    }

    private void handleResponse(Response response) {
        setProgress(false);
        InviteAdapter inviteAdapter = getOwnerAdapter();
        inviteAdapter.getItem(getAdapterPosition()).setInviteAccepted(true);
        inviteAdapter.notifyItemChanged(getAdapterPosition());
        acceptButton.setVisibility(View.GONE);
        ignoreButton.setVisibility(View.GONE);
        text4.setVisibility(View.VISIBLE);
        //Toast.makeText(context, ServerMessage.getServerMessage(context, response.getMessage()), Toast.LENGTH_LONG).show();
        //RELATIONSHIP_UPDATED_SUCCESSFULLY e WITHOUT_NOTIFICATION
    }

    private void handleError(Throwable error) {
        setProgress(false);
        if (!Utilities.isDeviceOnline(context))
            Toast.makeText(context, context.getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, context.getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
    }


    @Override
    public void setData(InviteModel invite) {
        if (!invite.isInviteAccepted()) {
            acceptButton.setVisibility(View.VISIBLE);
            ignoreButton.setVisibility(View.VISIBLE);
            text4.setVisibility(View.GONE);
        } else {
            acceptButton.setVisibility(View.GONE);
            ignoreButton.setVisibility(View.GONE);
            text4.setVisibility(View.VISIBLE);
        }

        if (invite.getText3().matches("accept")) {
            text1.setText(invite.getText1());
            acceptButton.setVisibility(View.GONE);
            ignoreButton.setVisibility(View.GONE);
        } else {
            text1.setText(invite.getText1());
            acceptButton.setVisibility(View.VISIBLE);
            ignoreButton.setVisibility(View.VISIBLE);
        }

        text1.setVisibility(View.VISIBLE);

        if (!invite.getText2().matches("")) {
            text2.setText(invite.getText2());
            text2.setVisibility(View.VISIBLE);
        } else
            text2.setVisibility(View.GONE);

        if (!invite.getText3().matches("")) {
            if (!invite.getText3().matches("accept")) {
                text3.setText(invite.getText3());
                text3.setVisibility(View.VISIBLE);
            } else
                text3.setVisibility(View.GONE);
        } else
            text3.setVisibility(View.GONE);


        object = invite.getActivity();
        if (invite.getActivity() instanceof ActivityServer) {

            Glide.clear(pieceIcon);
            Glide.with(context)
                    .load(invite.getIcon())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(pieceIcon);

            cubeUpperBoxIcon.setColorFilter(invite.getColorUpper());
            cubeLowerBoxIcon.setColorFilter(invite.getColorLower());
            pieceBox.setVisibility(View.VISIBLE);
            itemIcon.setVisibility(View.GONE);
            id_act_flag = ((ActivityServer) invite.getActivity()).getId();
            text1.setTextColor(ContextCompat.getColor(context, R.color.black));
            type = Constants.ACT;
        } else {
            id_act_flag = ((FlagServer) invite.getActivity()).getId();
            pieceBox.setVisibility(View.GONE);
            itemIcon.setVisibility(View.VISIBLE);
            itemIcon.setImageResource(R.drawable.ic_flag_available);
            text1.setTextColor(ContextCompat.getColor(context, R.color.flag_available));
            type = Constants.FLAG;
        }

        if (text4.getVisibility() == View.VISIBLE) {
            acceptButton.setVisibility(View.GONE);
            ignoreButton.setVisibility(View.GONE);
        }
    }


}
