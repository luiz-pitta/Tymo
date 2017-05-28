package io.development.tymo.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.cunoraz.tagview.OnTagDeleteListener;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.activities.SelectTagsActivity;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.utils.Utilities;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhatEditFragment extends Fragment implements View.OnClickListener {

    private Tag tag;
    private TagView tagGroup;
    private EditText tittleEditText, descriptionEditText, whatsAppEditText;
    private RelativeLayout addTagBox;

    private FirebaseAnalytics mFirebaseAnalytics;

    private OnTagDeleteListener mOnTagDeleteListener = new OnTagDeleteListener() {

        @Override
        public void onTagDeleted(final TagView view, final Tag tag, final int position) {
            view.remove(position);
        }
    };
    //Listners End

    public static Fragment newInstance(String text) {
        WhatEditFragment fragment = new WhatEditFragment();
        return fragment;
    }

    public WhatEditFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_act_what_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tagGroup = (TagView) view.findViewById(R.id.tagGroup);
        descriptionEditText = (EditText) view.findViewById(R.id.description);
        tittleEditText = (EditText) view.findViewById(R.id.title);
        whatsAppEditText = (EditText) view.findViewById(R.id.whatsAppGroupLink);
        addTagBox = (RelativeLayout) view.findViewById(R.id.addTagBox);

        addTagBox.setOnClickListener(this);
        tagGroup.setOnTagDeleteListener(mOnTagDeleteListener);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1), null /* class override */);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public List<String> getTextFromView() {
        List<String> texts = new ArrayList<>();
        texts.add(tittleEditText.getText().toString());
        texts.add(descriptionEditText.getText().toString());
        texts.add(whatsAppEditText.getText().toString());
        return texts;
    }

    @Override
    public void onClick(View view){
        if(view == addTagBox){

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "addTagBox" + "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "=>=" + getClass().getName().substring(20,getClass().getName().length() - 1));
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            int i;
            ArrayList<String> list = new ArrayList<>();
            List<Tag> list_tags = tagGroup.getTags();
            for(i = 0; i < list_tags.size(); i++){
                list.add(list_tags.get(i).text);
            }
            Intent intent = new Intent(getActivity(), SelectTagsActivity.class);
            intent.putStringArrayListExtra("tags_list", list);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                List<String> list = intent.getStringArrayListExtra("tags_objs");

                boolean tag_face = isTagPresent(getResources().getString(R.string.settings_import_facebook_tag));
                boolean tag_google = isTagPresent(getResources().getString(R.string.settings_import_google_tag));
                tagGroup.removeAll();

                if(tag_face){
                    Tag tag;
                    tag = new Tag(getResources().getString(R.string.settings_import_facebook_tag));
                    tag.radius = Utilities.convertDpToPixel(10.0f, getActivity());
                    tag.layoutColor = ContextCompat.getColor(getActivity(), R.color.deep_purple_400);
                    tag.isDeletable = false;
                    tagGroup.addTag(tag);
                }
                if(tag_google){
                    Tag tag;
                    tag = new Tag(getResources().getString(R.string.settings_import_google_tag));
                    tag.radius = Utilities.convertDpToPixel(10.0f, getActivity());
                    tag.layoutColor = ContextCompat.getColor(getActivity(), R.color.deep_purple_400);
                    tag.isDeletable = false;
                    tagGroup.addTag(tag);
                }

                Collections.sort(list, new Comparator<String>() {
                    @Override
                    public int compare(String c1, String c2) {
                        if (c1.compareTo(c2) > 0)
                            return 1;
                        else if (c1.compareTo(c2) < 0)
                            return -1;
                        else
                            return 0;
                    }
                });

                for (int i=0;i<list.size();i++){
                    Tag tag;
                    tag = new Tag(list.get(i));
                    tag.radius = Utilities.convertDpToPixel(10.0f, getActivity());
                    tag.layoutColor = ContextCompat.getColor(getActivity(), R.color.deep_purple_400);
                    tag.isDeletable = true;
                    tagGroup.addTag(tag);
                }



            }
        }
    }

    public List<Tag> getTags() {
        return tagGroup.getTags();
    }

    public boolean isTagPresent(String tag) {
        List<Tag> tags = tagGroup.getTags();
        for(int i=0;i<tags.size();i++){
            Tag t = tags.get(i);
            if(t.text.matches(tag))
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setLayout(ActivityServer activityServer, ArrayList<TagServer> tags){
        tittleEditText.setText(activityServer.getTitle());

        whatsAppEditText.setText(activityServer.getWhatsappGroupLink());

        if(activityServer.getDescription() != null)
            descriptionEditText.setText(activityServer.getDescription());
        else
            descriptionEditText.setText("");

        loadTags(tags);
    }

    private void loadTags(ArrayList<TagServer> tags){

        Collections.sort(tags, new Comparator<TagServer>() {
            @Override
            public int compare(TagServer c1, TagServer c2) {
                String name1 = c1.getTitle();
                String name2 = c2.getTitle();

                if (name1.compareTo(name2) > 0)
                    return 1;
                else if (name1.compareTo(name2) < 0)
                    return -1;
                else
                    return 0;
            }
        });

        for(int i=0;i<tags.size();i++){
            String text = tags.get(i).getTitle();
            tag = new Tag(text);
            tag.radius = Utilities.convertDpToPixel(10.0f, getActivity());
            tag.layoutColor = ContextCompat.getColor(getActivity(), R.color.deep_purple_400);
            if(text.matches(getResources().getString(R.string.settings_import_facebook_tag)) ||
                    text.matches(getResources().getString(R.string.settings_import_google_tag)))
                tag.isDeletable = false;
            else
                tag.isDeletable = true;
            tagGroup.addTag(tag);
        }
    }
}
