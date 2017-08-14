package io.development.tymo.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.borjabravo.readmoretextview.ReadMoreTextView;
import com.cunoraz.tagview.Tag;
import com.cunoraz.tagview.TagView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.model_server.TagServer;
import io.development.tymo.utils.SecureStringPropertyConverter;
import io.development.tymo.utils.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class WhatShowFragment extends Fragment {

    private TagView tagGroup;
    private Tag tag;
    private TextView tittleText, whatsAppGroupLink, descriptionShort;
    private ReadMoreTextView descriptionReadMore;
    private LinearLayout whatsAppGroupLinkBox;
    private SecureStringPropertyConverter converter = new SecureStringPropertyConverter();
    private FirebaseAnalytics mFirebaseAnalytics;

    public static Fragment newInstance(String text) {
        WhatShowFragment fragment = new WhatShowFragment();
        return fragment;
    }

    public WhatShowFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_act_what, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tagGroup = (TagView) view.findViewById(R.id.tagGroup);
        tittleText = (TextView) view.findViewById(R.id.title);
        whatsAppGroupLink = (TextView) view.findViewById(R.id.whatsAppGroupLink);
        descriptionReadMore = (ReadMoreTextView) view.findViewById(R.id.descriptionReadMore);
        descriptionShort = (TextView) view.findViewById(R.id.descriptionShort);
        whatsAppGroupLinkBox = (LinearLayout) view.findViewById(R.id.whatsAppGroupLinkBox);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        mFirebaseAnalytics.setCurrentScreen(getActivity(), "=>=" + getClass().getName().substring(20,getClass().getName().length()), null /* class override */);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setLayout(ActivityServer activityServer, ArrayList<TagServer> tags){
        if(tittleText!=null) {
            tittleText.setText(activityServer.getTitle());

            if (activityServer.getWhatsappGroupLink() == null || activityServer.getWhatsappGroupLink().matches(""))
                whatsAppGroupLinkBox.setVisibility(View.GONE);
            else
                whatsAppGroupLink.setText(converter.toEntityAttribute(activityServer.getWhatsappGroupLink()));

            if (activityServer.getDescription() != null && activityServer.getDescription().length() <= 240) {
                descriptionShort.setVisibility(View.VISIBLE);
                descriptionReadMore.setVisibility(View.GONE);

                if (!activityServer.getDescription().matches(""))
                    descriptionShort.setText(activityServer.getDescription());
                else
                    descriptionShort.setVisibility(View.GONE);
            } else {
                if (activityServer.getDescription() != null && !activityServer.getDescription().matches("")) {
                    String description = activityServer.getDescription() + " " + "\n\n";
                    descriptionReadMore.setText(description);
                } else
                    descriptionReadMore.setVisibility(View.GONE);
            }

            loadTags(tags);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void loadTags(ArrayList<TagServer> tags){
        tagGroup.removeAll();

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
            tag.isDeletable = false;
            tagGroup.addTag(tag);
        }
    }
}
