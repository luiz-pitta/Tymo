package io.development.tymo.view_holder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import java.util.Calendar;

import io.development.tymo.R;
import io.development.tymo.activities.ContactsActivity;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.adapters.SearchMultipleAdapter;
import io.development.tymo.model_server.FriendRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


public class PersonSearchViewHolder extends BaseViewHolder<User> implements View.OnClickListener, View.OnTouchListener {
    private ImageView profilePhoto;
    private ImageView actionIcon, moreVerticalIcon;
    private TextView text1, text2, text3, text4;
    private Context mContext;
    private RelativeLayout itemBox;
    private String email;
    private ProgressBar progressIcon;
    private boolean text2Exists;

    private FirebaseAnalytics mFirebaseAnalytics;

    int actionButtonType; //1 = NÃO é meu amigo ; 2 = é meu amigo ; 3 = eu solicitei ; 4 = me solicitaram

    private int deleteBlock = Constants.BLOCK;

    private String contactName;

    private SharedPreferences mSharedPreferences;
    private String email_friend, name_friend;

    private int favorite, know;

    private CompositeDisposable mSubscriptions;
    private boolean accept = false;

    public PersonSearchViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.list_item_search);
        profilePhoto = $(R.id.profilePhoto);
        actionIcon = $(R.id.actionIcon);
        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        text3 = $(R.id.text3);
        text4 = $(R.id.text4);
        itemBox = $(R.id.ItemBox);
        moreVerticalIcon = $(R.id.moreVerticalIcon);
        progressIcon = $(R.id.progressIcon);
        this.mContext = context;

        actionIcon.setOnTouchListener(this);

        $(R.id.pieceBox).setVisibility(View.GONE);
        $(R.id.text4).setVisibility(View.GONE);

        itemBox.setOnClickListener(this);
        actionIcon.setOnClickListener(this);
        moreVerticalIcon.setOnClickListener(this);
        moreVerticalIcon.setOnTouchListener(this);

        mSubscriptions = new CompositeDisposable();

        mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    @Override
    public void setData(User user) {
        actionIcon.setOnClickListener(this);

        contactName = user.getName();

        if (!user.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(mContext)
                    .load(user.getPhoto())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(profilePhoto) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            profilePhoto.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        } else
            profilePhoto.setImageResource(R.drawable.ic_profile_photo_empty);

        favorite = user.getCountFavorite();
        email_friend = user.getEmail();
        name_friend = user.getName();
        know = user.getCountKnows();

        if (!user.getLivesIn().matches("")) {
            text2.setText(user.getLivesIn());
            text2.setVisibility(View.VISIBLE);
            text2Exists = true;
        } else {
            text2.setVisibility(View.GONE);
            text2Exists = false;
        }

        if (user.getCountKnows() > 0) {
            text4.setVisibility(View.GONE);
            actionButtonType = 2;
            actionIcon.setImageResource(R.drawable.ic_person_check);
            actionIcon.setTag(R.drawable.ic_person_check);
            actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_check);
            actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_300));

        } else if (user.getCountAskAdd() > 0) {
            if (user.getQtySuccessfullyLogins() == 0) {
                actionButtonType = 3;
                text2.setVisibility(View.GONE);
                text4.setVisibility(View.VISIBLE);
                text4.setText(mContext.getResources().getString(R.string.response_request_sent));
                actionIcon.setImageResource(R.drawable.ic_person_cancel);
                actionIcon.setTag(R.drawable.ic_person_cancel);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
            } else {
                actionButtonType = 4;
                text2.setVisibility(View.GONE);
                text4.setVisibility(View.VISIBLE);
                text4.setText(mContext.getResources().getString(R.string.response_waiting_your_answer));
                actionIcon.setImageResource(R.drawable.ic_person_waiting);
                actionIcon.setTag(R.drawable.ic_person_waiting);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
            }
        } else {
            actionButtonType = 1;
            text4.setVisibility(View.GONE);
            actionIcon.setImageResource(R.drawable.ic_person_add);
            actionIcon.setTag(R.drawable.ic_person_add);
            actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
            actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
        }


        text1.setText(user.getName());

        if (user.getCountCommon() == 0)
            text3.setVisibility(View.GONE);
        else {
            if (user.getCountCommon() > 1) {
                text3.setText(mContext.getResources().getString(R.string.friend_in_common, user.getCountCommon()));
            } else {
                text3.setText(mContext.getResources().getString(R.string.friend_in_common_one));
            }
            text3.setVisibility(View.VISIBLE);
        }

        email = user.getEmail();

    }

    @Override
    public void onClick(View view) {
        if (view == moreVerticalIcon) {

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "moreVerticalIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            CharSequence[] items;

            if (know > 0) {
                items = new String[3];
                items[0] = mContext.getResources().getString(R.string.my_contacts_view, fullNameToShortName(contactName));
                items[1] = mContext.getResources().getString(R.string.my_contacts_block, fullNameToShortName(contactName));
                items[2] = mContext.getResources().getString(R.string.my_contacts_delete, fullNameToShortName(contactName));
            } else {
                items = new String[2];
                items[0] = mContext.getResources().getString(R.string.my_contacts_view, fullNameToShortName(contactName));
                items[1] = mContext.getResources().getString(R.string.my_contacts_block, fullNameToShortName(contactName));
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0) {
                        Intent intent = new Intent(mContext, ContactsActivity.class);
                        intent.putExtra("email_contacts", email_friend);
                        intent.putExtra("contact_full_name", name_friend);
                        mContext.startActivity(intent);
                    } else
                        createDialogDelete(item);
                }
            });
            builder.show();
        } else if (view == actionIcon) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actionIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            String email_user = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString(Constants.EMAIL, "");
            User user = new User();
            user.setEmail(email_user);
            user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());

            CharSequence[] items;

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            // não é meu amigo, mas quero adicionar
            if (actionButtonType == 1) {
                sendFriendRequest(email, user);
                text2.setVisibility(View.GONE);
                text4.setVisibility(View.VISIBLE);
                text4.setText(mContext.getResources().getString(R.string.response_request_sent));
            }
            // eu solicitei, mas quero cancelar
            else if (actionButtonType == 3) {
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setEmail(email_user);
                friendRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                friendRequest.setEmailFriend(email);
                cancelFriendRequest(friendRequest);
                if(text2Exists){
                    text2.setVisibility(View.VISIBLE);
                }
                text4.setVisibility(View.GONE);
            }
            // me solicitaram e quero responder
            else if (actionButtonType == 4) {
                items = null;

                items = mContext.getResources().getStringArray(R.array.array_pending_request_accept_ignore);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        createDialogUpdate(item);
                    }
                });
                builder.show();
            }
        } else if (view == itemBox) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "itemBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            Intent myIntent = new Intent(mContext, FriendProfileActivity.class);
            myIntent.putExtra("name", text1.getText().toString());
            myIntent.putExtra("friend_email", email);
            mContext.startActivity(myIntent);
        }
    }

    private String fullNameToShortName(String fullName){
        String[] fullNameSplited = fullName.split(" ");

        if (fullNameSplited.length > 1){
            return fullNameSplited[0] + " " + fullNameSplited[fullNameSplited.length-1];
        }
        else {
            return fullNameSplited[0];
        }
    }

    public void setProgress(boolean progress) {
        if (progress) {
            actionIcon.setVisibility(View.GONE);
            progressIcon.setVisibility(View.VISIBLE);
        } else {
            actionIcon.setVisibility(View.VISIBLE);
            progressIcon.setVisibility(View.GONE);
        }
    }

    private void createDialogDelete(int item) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);


        if (item == 1) {
            text1.setText(mContext.getResources().getString(R.string.contact_confirmation_question_block, contactName));
        }
        else{
            text1.setText(mContext.getResources().getString(R.string.contact_confirmation_question_delete, contactName));
        }

        buttonText1.setText(mContext.getResources().getString(R.string.no));
        buttonText2.setText(mContext.getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(mContext, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mSharedPreferences.getString(Constants.EMAIL, "");
                User user = new User();
                user.setEmail(email);
                user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                if (item == 1) {
                    deleteBlock = Constants.BLOCK;
                    user.setPrivacy(Constants.BLOCK);
                    sendBlockRequest(email_friend, user);
                } else {
                    deleteBlock = Constants.DELETE;
                    sendDeleteRequest(email_friend, user);
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sendBlockDeleteRequest" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendBlockRequest(String email, User user) {
        mSubscriptions.add(NetworkUtil.getRetrofit().registerBlockRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseBlockDeleteRequest, this::handleError));
    }

    private void handleResponseBlockDeleteRequest(Response response) {
        SearchMultipleAdapter adapter = getOwnerAdapter();

        if (deleteBlock == Constants.BLOCK) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.user_blocked_one), Toast.LENGTH_LONG).show();
            adapter.remove(getAdapterPosition());
        }
        else {
            ((User)adapter.getItem(getAdapterPosition())).setCountKnows(0);
            adapter.notifyItemChanged(getAdapterPosition());
            Toast.makeText(mContext, mContext.getResources().getString(R.string.contact_deleted), Toast.LENGTH_LONG).show();
            /*LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.toast, null);
            TextView toastText = (TextView) layout.findViewById(R.id.toastText);
            toastText.setText(mContext.getResources().getString(R.string.contact_deleted));
            Toast toast = new Toast(mContext);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();*/
        }
    }

    private void createDialogUpdate(int item) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);

        if (item == 0) {
            text1.setText(mContext.getResources().getString(R.string.contact_confirmation_question_add, contactName));
        }
        else if(item == 1) {
            text1.setText(mContext.getResources().getString(R.string.contact_confirmation_question_ignore_request, contactName));
        }

        buttonText1.setText(mContext.getResources().getString(R.string.no));
        buttonText2.setText(mContext.getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(mContext, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_left_radius));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setBackground(null);
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setBackground(ContextCompat.getDrawable(dialog.getContext(), R.drawable.btn_dialog_message_bottom_right_radius));
                }

                return false;
            }
        });

        buttonText1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        buttonText2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_user = mContext.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE).getString(Constants.EMAIL, "");
                User user = new User();
                user.setEmail(email_user);
                user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setEmail(email_user);
                friendRequest.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
                friendRequest.setEmailFriend(email);

                if (item == 0) {
                    text4.setVisibility(View.GONE);
                    if(text2Exists){
                        text2.setVisibility(View.VISIBLE);
                    }
                    friendRequest.setStatus(1);
                    updateFriendRequest(friendRequest);
                    know = 1;
                    accept = true;
                } else if (item == 1) {
                    text4.setVisibility(View.GONE);
                    if(text2Exists){
                        text2.setVisibility(View.VISIBLE);
                    }
                    friendRequest.setStatus(0);
                    updateFriendRequest(friendRequest);
                    know = 0;
                    accept = false;
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "updateFriendRequest" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void sendDeleteRequest(String email, User user) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerDeleteRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseDeleteRequest, this::handleError));
    }

    private void handleResponseDeleteRequest(Response response) {
        know = 0;
        actionButtonType = 1;
        SearchMultipleAdapter searchMultipleAdapter = getOwnerAdapter();
        ((User)searchMultipleAdapter.getItem(getAdapterPosition())).setCountKnows(0);
        searchMultipleAdapter.notifyItemChanged(getAdapterPosition());
        actionIcon.setImageResource(R.drawable.ic_person_add);
        actionIcon.setTag(R.drawable.ic_person_add);
        actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
        actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
        setProgressFriendRequest(false);
        Toast.makeText(mContext, mContext.getResources().getString(R.string.contact_deleted), Toast.LENGTH_LONG).show();
    }

    private void setProgressFriendRequest(boolean progress) {
        if (progress) {
            progressIcon.setVisibility(View.VISIBLE);
            actionIcon.setVisibility(View.GONE);
        } else {
            progressIcon.setVisibility(View.GONE);
            actionIcon.setVisibility(View.VISIBLE);
        }
    }

    private void cancelFriendRequest(FriendRequest friendRequest) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().cancelFriendRequest(friendRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFriendRequest, this::handleError));
    }

    private void updateFriendRequest(FriendRequest friendRequest) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().updateFriendRequest(friendRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleFriendRequest, this::handleError));
    }

    private void handleFriendRequest(Response response) {
        //Toast.makeText(mContext, ServerMessage.getServerMessage(mContext, response.getMessage()), Toast.LENGTH_LONG).show();
        //RELATIONSHIP_UPDATED_SUCCESSFULLY, WITHOUT_NOTIFICATION e REQUEST_TO_ADD_ACCEPTED
        setProgressFriendRequest(false);
        SearchMultipleAdapter searchMultipleAdapter = getOwnerAdapter();


        if (actionButtonType == 3) {
            if(text2Exists){
                text2.setVisibility(View.VISIBLE);
            }
            know = 0;
            ((User)searchMultipleAdapter.getItem(getAdapterPosition())).setCountKnows(0);
            actionButtonType = 1;
            actionIcon.setImageResource(R.drawable.ic_person_add);
            actionIcon.setTag(R.drawable.ic_person_add);
            actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
            actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
        } else if (actionButtonType == 4) {
            if (accept) {
                if(text2Exists){
                    text2.setVisibility(View.VISIBLE);
                }
                know = 1;
                actionButtonType = 2;
                ((User)searchMultipleAdapter.getItem(getAdapterPosition())).setCountKnows2(1);
                actionIcon.setImageResource(R.drawable.ic_person_check);
                actionIcon.setTag(R.drawable.ic_person_check);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_check);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_300));
            } else {
                if(text2Exists){
                    text2.setVisibility(View.VISIBLE);
                }
                know = 0;
                actionButtonType = 1;
                ((User)searchMultipleAdapter.getItem(getAdapterPosition())).setCountKnows(0);
                actionIcon.setImageResource(R.drawable.ic_person_add);
                actionIcon.setTag(R.drawable.ic_person_add);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
            }
        }
        searchMultipleAdapter.notifyItemChanged(getAdapterPosition());
    }

    private void sendFriendRequest(String email, User user) {
        setProgressFriendRequest(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerFriendRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFriendRequest, this::handleError));
    }

    private void handleResponseFriendRequest(Response response) {
        actionButtonType = 3;
        SearchMultipleAdapter searchMultipleAdapter = getOwnerAdapter();
        ((User)searchMultipleAdapter.getItem(getAdapterPosition())).setCountAskAdd(1);
        searchMultipleAdapter.notifyItemChanged(getAdapterPosition());
        actionIcon.setImageResource(R.drawable.ic_person_cancel);
        actionIcon.setTag(R.drawable.ic_person_cancel);
        actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
        actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
        setProgressFriendRequest(false);
        actionIcon.setOnClickListener(this);
        //Toast.makeText(mContext, ServerMessage.getServerMessage(mContext, response.getMessage()), Toast.LENGTH_LONG).show();
        //SUCCESSFULLY e WITHOUT_NOTIFICATION
    }

    private void handleError(Throwable error) {
        //setProgressFriendRequest(false);
        actionIcon.setOnClickListener(this);
        if(!Utilities.isDeviceOnline(mContext))
            Toast.makeText(mContext, mContext.getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        //else
        //    Toast.makeText(mContext, mContext.getResources().getString(R.string.error_internal_app), Toast.LENGTH_LONG).show();
        /*LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast, null);
        TextView toastText = (TextView) layout.findViewById(R.id.toastText);
        toastText.setText(mContext.getResources().getString(R.string.network_error));
        Toast toast = new Toast(mContext);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();*/
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == moreVerticalIcon) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                moreVerticalIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                moreVerticalIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.grey_400));
            }
        }
        else if (view == actionIcon && actionButtonType != 2) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
                actionIcon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_contact_icon_add));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_200));
                actionIcon.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bg_contact_icon_add_pressed));
            }
        }

        return false;
    }
}
