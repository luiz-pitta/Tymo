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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import io.development.tymo.adapters.ContactsAdapter;
import io.development.tymo.model_server.Response;
import io.development.tymo.model_server.User;
import io.development.tymo.network.NetworkUtil;
import io.development.tymo.utils.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.MODE_PRIVATE;


public class ContactViewHolder extends BaseViewHolder<User> implements View.OnClickListener, View.OnTouchListener {
    private TextView text1, text2;
    private ProgressBar progressIcon;
    private ImageView profilePhoto, actionIcon, moreVerticalIcon;
    private CompositeDisposable mSubscriptions;
    private SharedPreferences mSharedPreferences;
    private Context context;
    private RefreshLayoutPlansCallback callback;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String contactName;

    private String email_friend, name_friend;
    private boolean myContacts, blocked;
    private int favorite, deleteBlock = Constants.BLOCK;

    public ContactViewHolder(ViewGroup parent, final Context context, boolean myContacts, boolean blocked, RefreshLayoutPlansCallback callback) {
        super(parent, R.layout.list_item_contact);

        text1 = $(R.id.text1);
        text2 = $(R.id.text2);
        profilePhoto = $(R.id.profilePhoto);
        actionIcon = $(R.id.actionIcon);
        moreVerticalIcon = $(R.id.moreVerticalIcon);
        progressIcon = $(R.id.progressIcon);
        this.context = context;
        this.callback = callback;
        this.myContacts = myContacts;
        this.blocked = blocked;

        if (blocked) {
            $(R.id.listItemBox).setClickable(false);
        }

        moreVerticalIcon.setOnClickListener(this);
        moreVerticalIcon.setOnTouchListener(this);
        actionIcon.setOnClickListener(this);

        mSubscriptions = new CompositeDisposable();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);

        mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
    }


    @Override
    public void setData(User contact) {
        text1.setText(contact.getName());

        contactName = contact.getName();

        if (contact.getCountCommon() == 0) {
            text2.setVisibility(View.GONE);
        } else {
            if (contact.getCountCommon() > 1) {
                text2.setText(context.getResources().getString(R.string.friend_in_common, contact.getCountCommon()));
            } else {
                text2.setText(context.getResources().getString(R.string.friend_in_common_one));
            }
            text2.setVisibility(View.VISIBLE);
        }

        email_friend = contact.getEmail();
        name_friend = contact.getName();
        favorite = contact.getCountKnows();

        if (favorite > 0) {
            actionIcon.setImageResource(R.drawable.ic_star_activated);
            actionIcon.setColorFilter(ContextCompat.getColor(context, R.color.yellow_700));
        } else {
            actionIcon.setImageResource(R.drawable.ic_star_deactivated);
            actionIcon.setColorFilter(ContextCompat.getColor(context, R.color.grey_600));
        }

        if (!contact.getPhoto().matches("")) {
            Glide.clear(profilePhoto);
            Glide.with(context)
                    .load(contact.getPhoto())
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

        if (blocked) {
            actionIcon.setVisibility(View.GONE);
            moreVerticalIcon.setVisibility(View.VISIBLE);
        } else if (!myContacts) {
            text2.setVisibility(View.GONE);
            actionIcon.setVisibility(View.GONE);
            moreVerticalIcon.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        if (v == moreVerticalIcon) {

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "moreVerticalIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            CharSequence[] items;

            if (blocked) {
                items = new String[1];
                items[0] = context.getResources().getString(R.string.my_contacts_unblock, fullNameToShortName(contactName));
            } else {
                items = new String[3];
                items[0] = context.getResources().getString(R.string.my_contacts_view, fullNameToShortName(contactName));
                items[1] = context.getResources().getString(R.string.my_contacts_block, fullNameToShortName(contactName));
                items[2] = context.getResources().getString(R.string.my_contacts_delete, fullNameToShortName(contactName));
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (item == 0 && !blocked) {
                        Intent intent = new Intent(context, ContactsActivity.class);
                        intent.putExtra("email_contacts", email_friend);
                        intent.putExtra("contact_full_name", name_friend);
                        context.startActivity(intent);
                    } else
                        createDialogDelete(item);
                }
            });
            builder.show();
        } else if (v == actionIcon) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "actionIcon" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            String email = mSharedPreferences.getString(Constants.EMAIL, "");
            User user = new User();
            user.setEmail(email);
            user.setDateTimeNow(Calendar.getInstance().getTimeInMillis());
            user.setPrivacy(favorite);

            sendFavoriteRequest(email_friend, user);
        }
    }

    private void createDialogDelete(int item) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.dialog_message, null);

        TextView text1 = (TextView) customView.findViewById(R.id.text1);
        TextView text2 = (TextView) customView.findViewById(R.id.text2);
        TextView buttonText1 = (TextView) customView.findViewById(R.id.buttonText1);
        TextView buttonText2 = (TextView) customView.findViewById(R.id.buttonText2);
        EditText editText = (EditText) customView.findViewById(R.id.editText);

        editText.setVisibility(View.GONE);
        text2.setVisibility(View.GONE);

        if (item == 1) {
            text1.setText(context.getResources().getString(R.string.contact_confirmation_question_block, contactName));
        } else if (item == 2) {
            text1.setText(context.getResources().getString(R.string.contact_confirmation_question_delete, contactName));
        } else {
            text1.setText(context.getResources().getString(R.string.contact_confirmation_question_unblock, contactName));
        }

        buttonText1.setText(context.getResources().getString(R.string.no));
        buttonText2.setText(context.getResources().getString(R.string.yes));


        Dialog dialog = new Dialog(context, R.style.NewDialog);

        dialog.setContentView(customView);
        dialog.setCanceledOnTouchOutside(true);

        buttonText1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.grey_500));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText1.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.grey_300));
                }

                return false;
            }
        });

        buttonText2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_300));
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonText2.setTextColor(ContextCompat.getColor(dialog.getContext(), R.color.deep_purple_100));
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
                Bundle bundle = new Bundle();

                if (item == 1) {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "BLOCK" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    deleteBlock = Constants.BLOCK;
                    user.setPrivacy(Constants.BLOCK);
                    sendBlockRequest(email_friend, user);
                } else if (item == 2) {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "DELETE" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    deleteBlock = Constants.DELETE;
                    sendDeleteRequest(email_friend, user);
                } else {
                    bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "UNBLOCK" + "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                    deleteBlock = Constants.UNBLOCK;
                    user.setPrivacy(Constants.UNBLOCK);
                    sendUnBlockRequest(email_friend, user);
                }

                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20, getClass().getName().length()));
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private String fullNameToShortName(String fullName) {
        String[] fullNameSplited = fullName.split(" ");

        if (fullNameSplited.length > 1) {
            return fullNameSplited[0] + " " + fullNameSplited[fullNameSplited.length - 1];
        } else {
            return fullNameSplited[0];
        }
    }

    public void setProgress(boolean progress) {
        if (progress) {
            if (!blocked)
                actionIcon.setVisibility(View.GONE);
            progressIcon.setVisibility(View.VISIBLE);
        } else {
            if (!blocked)
                actionIcon.setVisibility(View.VISIBLE);
            progressIcon.setVisibility(View.GONE);
        }
    }

    private void sendFavoriteRequest(String email, User user) {
        setProgress(true);
        mSubscriptions.add(NetworkUtil.getRetrofit().registerFavoriteRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseFavoriteRequest, this::handleError));
    }

    private void sendBlockRequest(String email, User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().registerBlockRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseBlockDeleteRequest, this::handleError));
    }

    private void sendDeleteRequest(String email, User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().registerDeleteRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseBlockDeleteRequest, this::handleError));
    }

    private void sendUnBlockRequest(String email, User user) {

        mSubscriptions.add(NetworkUtil.getRetrofit().registerBlockRequest(email, user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponseUnblockRequest, this::handleError));
    }

    private void handleResponseUnblockRequest(Response response) {
        ContactsAdapter adapter = getOwnerAdapter();
        adapter.remove(getAdapterPosition());
        if (callback != null)
            callback.refreshLayout();
        Toast.makeText(context, context.getResources().getString(R.string.user_unblocked), Toast.LENGTH_LONG).show();
    }

    private void handleResponseBlockDeleteRequest(Response response) {
        ContactsAdapter adapter = getOwnerAdapter();
        adapter.remove(getAdapterPosition());
        if (callback != null)
            callback.refreshLayout();
        if (deleteBlock == Constants.BLOCK)
            Toast.makeText(context, context.getResources().getString(R.string.user_blocked_one), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context, context.getResources().getString(R.string.contact_deleted), Toast.LENGTH_LONG).show();
    }

    private void handleResponseFavoriteRequest(Response response) {
        ContactsAdapter adapter = getOwnerAdapter();

        if (favorite == 0) {
            actionIcon.setImageResource(R.drawable.ic_star_activated);
            actionIcon.setColorFilter(ContextCompat.getColor(context, R.color.yellow_700));
            favorite = 1;
            adapter.getItem(getAdapterPosition()).setCountKnows(1);
            Toast.makeText(context, context.getResources().getString(R.string.contact_favorited, adapter.getItem(getAdapterPosition()).getName()), Toast.LENGTH_LONG).show();
        } else {
            actionIcon.setImageResource(R.drawable.ic_star_deactivated);
            actionIcon.setColorFilter(ContextCompat.getColor(context, R.color.grey_600));
            favorite = 0;
            adapter.getItem(getAdapterPosition()).setCountKnows(0);
            Toast.makeText(context, context.getResources().getString(R.string.contact_disfavorited, adapter.getItem(getAdapterPosition()).getName()), Toast.LENGTH_LONG).show();
        }
        setProgress(false);
    }

    private void handleError(Throwable error) {
        //setProgress(false);
        Toast.makeText(context, context.getResources().getString(R.string.error_network), Toast.LENGTH_LONG).show();
    }

    public interface RefreshLayoutPlansCallback {

        void refreshLayout();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == moreVerticalIcon) {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                moreVerticalIcon.setColorFilter(ContextCompat.getColor(context, R.color.grey_600));
            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                moreVerticalIcon.setColorFilter(ContextCompat.getColor(context, R.color.grey_400));
            }
        }

        return false;
    }

}
