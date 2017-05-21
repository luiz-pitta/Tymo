package io.development.tymo.adapters;

import android.content.Context;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;

import io.development.tymo.model_server.User;
import io.development.tymo.models.ContactModel;
import io.development.tymo.models.InviteModel;
import io.development.tymo.view_holder.ContactViewHolder;
import io.development.tymo.view_holder.InviteViewHolder;
import io.development.tymo.view_holder.PersonSearchViewHolder;


public class ContactsAdapter extends RecyclerArrayAdapter<User> {

    private Context context;
    private boolean myContacts, blocked;
    private ContactViewHolder.RefreshLayoutPlansCallback callback;

    public ContactsAdapter(Context context, boolean myContacts, boolean blocked, ContactViewHolder.RefreshLayoutPlansCallback callback) {
        super(context);
        this.context = context;
        this.myContacts = myContacts;
        this.blocked = blocked;
        this.callback = callback;
    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(parent, context, myContacts, blocked, callback);
    }

}
