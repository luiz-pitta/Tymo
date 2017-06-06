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
import android.view.LayoutInflater;
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

import io.development.tymo.R;
import io.development.tymo.activities.ContactsActivity;
import io.development.tymo.activities.FriendProfileActivity;
import io.development.tymo.adapters.ShowGuestsAdapter;
import io.development.tymo.model_server.FriendRequest;
import io.development.tymo.model_server.InviteRequest;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.content.Context.MODE_PRIVATE;


public class ShowGuestViewHolder extends BaseViewHolder<User> implements View.OnClickListener {
    private ImageView profilePhoto;
    private ImageView actionIcon, moreVerticalIcon;
    private TextView text1, text2, text3, text4;
    private Context mContext;
    private RelativeLayout itemBox;
    private String email;
    private ProgressBar progressIcon;

    private FirebaseAnalytics mFirebaseAnalytics;

    private int actionButtonType; //1 = NÃO é meu amigo ; 2 = é meu amigo ; 3 = eu solicitei ; 4 = me solicitaram; 5 = eu bloqueiei

    private int deleteBlock = Constants.BLOCK;

    private String contactName;

    private SharedPreferences mSharedPreferences;
    private String email_friend, name_friend;

    private int know;
    private int iBlocked;
    private long idAct;

    private CompositeSubscription mSubscriptions;
    private boolean accept = false;
    private boolean admOrCreator = false, isFlag = false;
    private boolean isPersonAdm = false;
    private boolean isPersonCreator = false;

    public ShowGuestViewHolder(ViewGroup parent, Context context, long id, boolean adm, boolean flag) {
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
        this.admOrCreator = adm;
        this.idAct = id;
        this.isFlag = flag;

        $(R.id.pieceBox).setVisibility(View.GONE);
        $(R.id.text4).setVisibility(View.GONE);

        text4.setAllCaps(true);

        itemBox.setOnClickListener(this);
        actionIcon.setOnClickListener(this);
        moreVerticalIcon.setOnClickListener(this);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        mSubscriptions = new CompositeSubscription();

        mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
    }

    private void setOnClick(boolean active) {
        if (active) {
            actionIcon.setOnClickListener(this);
            itemBox.setOnClickListener(this);
            actionIcon.setOnClickListener(this);
            moreVerticalIcon.setOnClickListener(this);
        } else {
            actionIcon.setOnClickListener(null);
            itemBox.setOnClickListener(null);
            actionIcon.setOnClickListener(null);
            moreVerticalIcon.setOnClickListener(null);
        }
    }

    @Override
    public void setData(User user) {
        actionIcon.setOnClickListener(this);
        isPersonAdm = false;
        isPersonCreator = false;

        contactName = user.getName();
        iBlocked = user.getIBlocked();

        if (user.isCreator()) {
            text4.setVisibility(View.VISIBLE);
            text4.setText(mContext.getResources().getString(R.string.creator));
            isPersonCreator = true;
        } else if (user.isAdm()) {
            text4.setVisibility(View.VISIBLE);
            text4.setText(mContext.getResources().getString(R.string.administrator));
            isPersonAdm = true;
        } else
            text4.setVisibility(View.GONE);

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

        email_friend = user.getEmail();
        name_friend = user.getName();
        know = user.getCountKnows();

        text2.setVisibility(View.GONE);

        if (user.getIBlocked() > 0) {
            //text4.setVisibility(View.GONE);
            actionButtonType = 5;
            actionIcon.setImageResource(R.drawable.ic_person_blocked);
            actionIcon.setTag(R.drawable.ic_person_blocked);
            actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_block);
            actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.red_300));
        } else if (user.getCountKnows() > 0) {
            //text4.setVisibility(View.GONE);
            actionButtonType = 2;
            actionIcon.setImageResource(R.drawable.ic_person_check);
            actionIcon.setTag(R.drawable.ic_person_check);
            actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_check);
            actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_300));

        } else if (user.getCountAskAdd() > 0) {
            if (user.getQtySuccessfullyLogins() == 0) {
                actionButtonType = 3;
                //text4.setVisibility(View.VISIBLE);
                //text4.setText(mContext.getResources().getString(R.string.request_sent));
                actionIcon.setImageResource(R.drawable.ic_person_cancel);
                actionIcon.setTag(R.drawable.ic_person_cancel);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
            } else {
                actionButtonType = 4;
                //text4.setVisibility(View.VISIBLE);
                //text4.setText(mContext.getResources().getString(R.string.waiting_your_answer));
                actionIcon.setImageResource(R.drawable.ic_person_waiting);
                actionIcon.setTag(R.drawable.ic_person_waiting);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
            }
        } else {
            actionButtonType = 1;
            //text4.setVisibility(View.GONE);
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

        if (email.matches(mSharedPreferences.getString(Constants.EMAIL, "")) || user.getHeBlocked() > 0) {
            setOnClick(false);
            actionIcon.setVisibility(View.GONE);
            moreVerticalIcon.setVisibility(View.GONE);
        } else if (user.getIBlocked() > 0) {
            setOnClick(true);
            actionIcon.setVisibility(View.VISIBLE);
            moreVerticalIcon.setVisibility(View.VISIBLE);
            itemBox.setOnClickListener(null);
        } else {
            setOnClick(true);
            actionIcon.setVisibility(View.VISIBLE);
            moreVerticalIcon.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onClick(View view) {
        if (view == moreVerticalIcon) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "moreVerticalIcon" + "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            CharSequence[] items;

            if (iBlocked > 0) {
                items = new String[1];
                items[0] = mContext.getResources().getString(R.string.my_contacts_unblock, contactName);
            } else if (admOrCreator && !isPersonCreator && !isFlag) {
                if (!isPersonAdm) {
                    items = new String[2];
                    items[0] = mContext.getResources().getString(R.string.administrator_make);
                    items[1] = mContext.getResources().getString(R.string.my_contacts_view, contactName);
                } else {
                    items = new String[1];
                    items[0] = mContext.getResources().getString(R.string.my_contacts_view, contactName);
                }
            } else {
                items = new String[1];
                items[0] = mContext.getResources().getString(R.string.my_contacts_view, contactName);
            }


            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (iBlocked > 0)
                        createDialogDelete(item);
                    else if (admOrCreator && !isPersonCreator && !isFlag) {
                        if (item == 0 && !isPersonAdm) {
                            InviteRequest inviteRequest = new InviteRequest();
                            inviteRequest.setEmail(email);
                            inviteRequest.setType(Constants.ADM);
                            inviteRequest.setIdAct(idAct);
                            sendAdmRequest(inviteRequest);
                        } else {
                            Intent intent = new Intent(mContext, ContactsActivity.class);
                            intent.putExtra("email_contacts", email_friend);
                            intent.putExtra("contact_full_name", name_friend);
                            mContext.startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(mContext, ContactsActivity.class);
                        intent.putExtra("email_contacts", email_friend);
                        intent.putExtra("contact_full_name", name_friend);
                        mContext.startActivity(intent);
                    }
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

            CharSequence[] items;

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

            // não é meu amigo, mas quero adicionar
            if (actionButtonType == 1) {
                sendFriendRequest(email, user);
                text2.setVisibility(View.GONE);
                //text4.setVisibility(View.VISIBLE);
                //text4.setText(mContext.getResources().getString(R.string.request_sent));
            }
            // eu solicitei, mas quero cancelar
            else if (actionButtonType == 3) {
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setEmail(email_user);
                friendRequest.setEmailFriend(email);
                cancelFriendRequest(friendRequest);
                //text4.setVisibility(View.GONE);
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

    public void setProgress(boolean progress) {
        if (progress)
            progressIcon.setVisibility(View.VISIBLE);
        else
            progressIcon.setVisibility(View.GONE);
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

        text1.setText(mContext.getResources().getString(R.string.contact_confirmation_question_unblock, fullNameToShortName(contactName)));
        buttonText1.setText(mContext.getResources().getString(R.string.no));
        buttonText2.setText(mContext.getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(mContext, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

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
                deleteBlock = Constants.UNBLOCK;
                user.setPrivacy(Constants.UNBLOCK);
                sendUnBlockRequest(email_friend, user);

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "sendUnBlockRequest"+ "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        dialog.show();
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

    private void sendUnBlockRequest(String email, User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerBlockRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUnblockRequest, this::handleError));
    }

    private void sendAdmRequest(InviteRequest inviteRequest) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().setAdm(inviteRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseSetAdm, this::handleError));
    }

    private void handleResponseSetAdm(Response response) {
        ShowGuestsAdapter showGuestsAdapter = getOwnerAdapter();
        if (isPersonAdm)
            showGuestsAdapter.getItem(getAdapterPosition()).setAdm(false);
        else
            showGuestsAdapter.getItem(getAdapterPosition()).setAdm(true);
        showGuestsAdapter.notifyItemChanged(getAdapterPosition());
        setProgress(false);
        Toast.makeText(mContext, mContext.getResources().getString(R.string.administrator_new, contactName), Toast.LENGTH_LONG).show();
    }

    private void handleResponseUnblockRequest(Response response) {
        ShowGuestsAdapter showGuestsAdapter = getOwnerAdapter();
        showGuestsAdapter.getItem(getAdapterPosition()).setIBlocked(0);
        showGuestsAdapter.notifyItemChanged(getAdapterPosition());
        setProgress(false);
        Toast.makeText(mContext, mContext.getResources().getString(R.string.user_unblocked), Toast.LENGTH_LONG).show();
    }

    private void sendBlockRequest(String email, User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerBlockRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseBlockDeleteRequest, this::handleError));
    }

    private void handleResponseBlockDeleteRequest(Response response) {
        ShowGuestsAdapter showGuestsAdapter = getOwnerAdapter();


        if (deleteBlock == Constants.BLOCK) {
            showGuestsAdapter.getItem(getAdapterPosition()).setIBlocked(1);
            showGuestsAdapter.notifyItemChanged(getAdapterPosition());
            Toast.makeText(mContext, mContext.getResources().getString(R.string.user_blocked_one), Toast.LENGTH_LONG).show();
        } else {
            showGuestsAdapter.getItem(getAdapterPosition()).setCountKnows(0);
            showGuestsAdapter.notifyItemChanged(getAdapterPosition());
            Toast.makeText(mContext, mContext.getResources().getString(R.string.contact_deleted), Toast.LENGTH_LONG).show();
            /*LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.toast, null);
            TextView toastText = (TextView) layout.findViewById(R.id.toastText);
            toastText.setText(mContext.getResources().getString(R.string.contact_deleted));
            Toast toast = new Toast(mContext);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();*/
        }
        setProgress(false);
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
                FriendRequest friendRequest = new FriendRequest();
                friendRequest.setEmail(email_user);
                friendRequest.setEmailFriend(email);

                if (item == 0) {
                    //text4.setVisibility(View.GONE);
                    friendRequest.setStatus(1);
                    updateFriendRequest(friendRequest);
                    know = 1;
                    accept = true;
                } else if (item == 1) {
                    //text4.setVisibility(View.GONE);
                    friendRequest.setStatus(0);
                    updateFriendRequest(friendRequest);
                    know = 0;
                    accept = false;
                }

                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "updateFriendRequest"+ "=>=" + getClass().getName().substring(20,getClass().getName().length()));
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
        ShowGuestsAdapter showGuestsAdapter = getOwnerAdapter();
        showGuestsAdapter.getItem(getAdapterPosition()).setCountKnows(0);
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

        ShowGuestsAdapter showGuestsAdapter = getOwnerAdapter();
        setProgressFriendRequest(false);


        if (actionButtonType == 3) {
            know = 0;
            actionButtonType = 1;
            showGuestsAdapter.getItem(getAdapterPosition()).setCountKnows(0);
            actionIcon.setImageResource(R.drawable.ic_person_add);
            actionIcon.setTag(R.drawable.ic_person_add);
            actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
            actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
        } else if (actionButtonType == 4) {
            if (accept) {
                know = 1;
                actionButtonType = 2;
                showGuestsAdapter.getItem(getAdapterPosition()).setCountKnows2(1);
                actionIcon.setImageResource(R.drawable.ic_person_check);
                actionIcon.setTag(R.drawable.ic_person_check);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_check);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.green_300));
            } else {
                know = 0;
                actionButtonType = 1;
                showGuestsAdapter.getItem(getAdapterPosition()).setCountKnows(0);
                actionIcon.setImageResource(R.drawable.ic_person_add);
                actionIcon.setTag(R.drawable.ic_person_add);
                actionIcon.setBackgroundResource(R.drawable.bg_contact_icon_add);
                actionIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.deep_purple_400));
            }
        }
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
        ShowGuestsAdapter showGuestsAdapter = getOwnerAdapter();
        showGuestsAdapter.getItem(getAdapterPosition()).setCountAskAdd(1);
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
        Toast.makeText(mContext, mContext.getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
        /*LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.toast, null);
        TextView toastText = (TextView) layout.findViewById(R.id.toastText);
        toastText.setText(mContext.getResources().getString(R.string.network_error));
        Toast toast = new Toast(mContext);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();*/
    }
}
