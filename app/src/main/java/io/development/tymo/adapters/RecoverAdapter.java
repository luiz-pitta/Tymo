package io.development.tymo.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import io.development.tymo.R;
import io.development.tymo.model_server.ActivityServer;
import io.development.tymo.utils.Utilities;

public class RecoverAdapter extends RecyclerView.Adapter<RecoverAdapter.SimpleViewHolder> {
    private static final int DEFAULT_ITEM_COUNT = 1;

    private Context mContext;
    private List<Integer> mItems;
    private static List<ActivityServer> activityServerList;
    private static RefreshLayoutAdapterCallback callback;
    private int mCurrentItemId = 0;
    private static int mCurrentPosition = 0;



    public static class SimpleViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context mContext;
        private final RelativeLayout pieceBox,pieceBox2,pieceBox3,pieceBox4,pieceBox5;
        private final ImageView cubeUpperBoxIcon3, cubeLowerBoxIcon3, pieceIcon3;
        private final ImageView cubeUpperBoxIcon2, cubeLowerBoxIcon2, pieceIcon2;
        private final ImageView cubeUpperBoxIcon4, cubeLowerBoxIcon4, pieceIcon4;
        private final ImageView cubeUpperBoxIcon5, cubeLowerBoxIcon5, pieceIcon5;
        private final ImageView cubeUpperBoxIcon, cubeLowerBoxIcon, pieceIcon;

        private FirebaseAnalytics mFirebaseAnalytics;

        public SimpleViewHolder(View view, Context context) {
            super(view);
            mContext = context;

            pieceBox = (RelativeLayout) view.findViewById(R.id.pieceBox);
            pieceBox2 = (RelativeLayout) view.findViewById(R.id.pieceBox2);
            pieceBox3 = (RelativeLayout) view.findViewById(R.id.pieceBox3);
            pieceBox4 = (RelativeLayout) view.findViewById(R.id.pieceBox4);
            pieceBox5 = (RelativeLayout) view.findViewById(R.id.pieceBox5);

            cubeUpperBoxIcon = (ImageView) view.findViewById(R.id.cubeUpperBoxIcon);
            cubeLowerBoxIcon = (ImageView) view.findViewById(R.id.cubeLowerBoxIcon);
            pieceIcon = (ImageView) view.findViewById(R.id.pieceIcon);

            cubeUpperBoxIcon2 = (ImageView) view.findViewById(R.id.cubeUpperBoxIcon2);
            cubeLowerBoxIcon2 = (ImageView) view.findViewById(R.id.cubeLowerBoxIcon2);
            pieceIcon2 = (ImageView) view.findViewById(R.id.pieceIcon2);

            cubeUpperBoxIcon3 = (ImageView) view.findViewById(R.id.cubeUpperBoxIcon3);
            cubeLowerBoxIcon3 = (ImageView) view.findViewById(R.id.cubeLowerBoxIcon3);
            pieceIcon3 = (ImageView) view.findViewById(R.id.pieceIcon3);

            cubeUpperBoxIcon4 = (ImageView) view.findViewById(R.id.cubeUpperBoxIcon4);
            cubeLowerBoxIcon4 = (ImageView) view.findViewById(R.id.cubeLowerBoxIcon4);
            pieceIcon4 = (ImageView) view.findViewById(R.id.pieceIcon4);

            cubeUpperBoxIcon5 = (ImageView) view.findViewById(R.id.cubeUpperBoxIcon5);
            cubeLowerBoxIcon5 = (ImageView) view.findViewById(R.id.cubeLowerBoxIcon5);
            pieceIcon5 = (ImageView) view.findViewById(R.id.pieceIcon5);

            pieceBox.setOnClickListener(this);
            pieceBox2.setOnClickListener(this);
            pieceBox3.setOnClickListener(this);
            pieceBox4.setOnClickListener(this);
            pieceBox5.setOnClickListener(this);

            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        }

        @Override
        public void onClick(View v) {
            int id = v.getId();
            int click = 2;
            int i;
            RelativeLayout layout;
            LinearLayout.LayoutParams params;
            RelativeLayout mPieceBox[] = {pieceBox, pieceBox2, pieceBox3, pieceBox4, pieceBox5};
            ImageView mUpper[] = {cubeUpperBoxIcon, cubeUpperBoxIcon2, cubeUpperBoxIcon3, cubeUpperBoxIcon4, cubeUpperBoxIcon5};
            ImageView mLower[] = {cubeLowerBoxIcon, cubeLowerBoxIcon2, cubeLowerBoxIcon3, cubeLowerBoxIcon4, cubeLowerBoxIcon5};
            ImageView mPiece[] = {pieceIcon, pieceIcon2, pieceIcon3, pieceIcon4, pieceIcon5};

            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "recoverCube" + getClass().getSimpleName());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, getClass().getSimpleName());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

            if (id == pieceBox.getId())
                click = 0;
            else if (id == pieceBox2.getId())
                click = 1;
            else if (id == pieceBox3.getId())
                click = 2;
            else if (id == pieceBox4.getId())
                click = 3;
            else if (id == pieceBox5.getId())
                click = 4;

            for(i=0;i<5;i++){
                layout = mPieceBox[i];
                params = (LinearLayout.LayoutParams)layout.getLayoutParams();

                if(i == click) {
                    setSizeCube(mUpper[i], mLower[i], mPiece[i], true);
                    params.setMargins(0, 0, 0, 0);
                }
                else {
                    setSizeCube(mUpper[i], mLower[i], mPiece[i], false);
                    params.setMargins(0, 0, 0, (int)Utilities.convertDpToPixel(15f,mContext));
                }

                layout.setLayoutParams(params);
            }

            if(callback != null)
                callback.setCurrent(activityServerList.get(click+5*mCurrentPosition));
        }

        public void setSizeCube(ImageView upper, ImageView lower, ImageView icon, boolean size) {

            if(!size){
                upper.getLayoutParams().height = (int)Utilities.convertDpToPixel(9.5f,mContext);
                upper.getLayoutParams().width=(int)Utilities.convertDpToPixel(50f,mContext);
                lower.getLayoutParams().height=(int)Utilities.convertDpToPixel(40f,mContext);
                lower.getLayoutParams().width=(int)Utilities.convertDpToPixel(50f,mContext);
                icon.getLayoutParams().height=(int)Utilities.convertDpToPixel(30f,mContext);
                icon.getLayoutParams().width=(int)Utilities.convertDpToPixel(30f,mContext);

            }else{
                upper.getLayoutParams().height = (int) Utilities.convertDpToPixel(18.5f, mContext);
                upper.getLayoutParams().width=(int)Utilities.convertDpToPixel(100f,mContext);
                lower.getLayoutParams().height=(int)Utilities.convertDpToPixel(80f,mContext);
                lower.getLayoutParams().width=(int)Utilities.convertDpToPixel(100f,mContext);
                icon.getLayoutParams().height=(int)Utilities.convertDpToPixel(60f,mContext);
                icon.getLayoutParams().width=(int)Utilities.convertDpToPixel(60f,mContext);
            }

            icon.requestLayout();
            lower.requestLayout();
            upper.requestLayout();
        }

        public void setVisibilityBox(RelativeLayout pieceBox, int viewType) {
            pieceBox.setVisibility(viewType);
        }
    }

    public void setCurrentPosition(int position){
        mCurrentPosition = position;
    }

    public int getCurrentPosition(){
        return mCurrentPosition;
    }

    public ActivityServer getCurrentActivity(){
        int nItens;
        if(mCurrentPosition == (mItems.size()-1)) {
            nItens = activityServerList.size() - 5 * mCurrentPosition;
            return activityServerList.get((mCurrentPosition * 5) + (nItens / 2));
        }
        else
            return activityServerList.get(mCurrentPosition*5+2);
    }

    public RecoverAdapter(Context context) {
       this(context, DEFAULT_ITEM_COUNT);
    }

    public RecoverAdapter(Context context, int itemCount) {
        mContext = context;
        activityServerList = new ArrayList<>();
        mItems = new ArrayList<>(itemCount);
        for (int i = 0; i < itemCount; i++) {
            addItem(i);
        }
    }

    public void addActivityList(List<ActivityServer> list) {
        int pages,i;

        activityServerList.clear();
        this.clear();
        mCurrentItemId = 0;
        activityServerList.addAll(list);


        if(activityServerList.size()%5 == 0)
            pages = activityServerList.size()/5;
        else
            pages = (activityServerList.size()/5) + 1;

        for(i = 0; i < pages; i++)
            addItem(i);

    }

    public void addItem(int position) {
        final int id = mCurrentItemId++;
        mItems.add(position, id);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    public void clear(){
        int size = mItems.size();
        for(int i=0; i<size;i++)
            removeItem(0);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.list_recover_pieces, parent, false);
        return new SimpleViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, final int position) {
        int i, posActivity;
        RelativeLayout layout;
        ActivityServer activityServer;
        LinearLayout.LayoutParams params;
        RelativeLayout mPieceBox[] = {holder.pieceBox, holder.pieceBox2, holder.pieceBox3, holder.pieceBox4, holder.pieceBox5};
        ImageView mUpper[] = {holder.cubeUpperBoxIcon, holder.cubeUpperBoxIcon2, holder.cubeUpperBoxIcon3, holder.cubeUpperBoxIcon4, holder.cubeUpperBoxIcon5};
        ImageView mLower[] = {holder.cubeLowerBoxIcon, holder.cubeLowerBoxIcon2, holder.cubeLowerBoxIcon3, holder.cubeLowerBoxIcon4, holder.cubeLowerBoxIcon5};
        ImageView mPiece[] = {holder.pieceIcon, holder.pieceIcon2, holder.pieceIcon3, holder.pieceIcon4, holder.pieceIcon5};

        for(i=0;i<5;i++)
            holder.setVisibilityBox(mPieceBox[i], View.GONE);

        if(position == (mCurrentItemId-1)){
            int nItens = activityServerList.size() -5*position;
            for (i = 0; i < nItens; i++) {
                posActivity = i + 5*position;
                activityServer = activityServerList.get(posActivity);
                layout = mPieceBox[i];
                params = (LinearLayout.LayoutParams) layout.getLayoutParams();

                holder.setVisibilityBox(mPieceBox[i], View.VISIBLE);

                if (i == nItens/2) {
                    holder.setSizeCube(mUpper[i], mLower[i], mPiece[i], true);
                    params.setMargins(0, 0, 0, 0);
                } else {
                    holder.setSizeCube(mUpper[i], mLower[i], mPiece[i], false);
                    params.setMargins(0, 0, 0, (int) Utilities.convertDpToPixel(15f, mContext));
                }

                mUpper[i].setColorFilter(activityServer.getCubeColorUpper());
                mLower[i].setColorFilter(activityServer.getCubeColor());

                Glide.clear(mPiece[i]);
                Glide.with(mContext)
                        .load(activityServer.getCubeIcon())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mPiece[i]);

                layout.setLayoutParams(params);
            }
        }else {
            for (i = 0; i < 5; i++) {
                posActivity = i + 5*position;
                activityServer = activityServerList.get(posActivity);
                layout = mPieceBox[i];
                params = (LinearLayout.LayoutParams) layout.getLayoutParams();

                holder.setVisibilityBox(mPieceBox[i], View.VISIBLE);

                if (i == 2) {
                    holder.setSizeCube(mUpper[i], mLower[i], mPiece[i], true);
                    params.setMargins(0, 0, 0, 0);
                } else {
                    holder.setSizeCube(mUpper[i], mLower[i], mPiece[i], false);
                    params.setMargins(0, 0, 0, (int) Utilities.convertDpToPixel(15f, mContext));
                }

                mUpper[i].setColorFilter(activityServer.getCubeColorUpper());
                mLower[i].setColorFilter(activityServer.getCubeColor());

                Glide.clear(mPiece[i]);
                Glide.with(mContext)
                        .load(activityServer.getCubeIcon())
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(mPiece[i]);

                layout.setLayoutParams(params);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setCallback(RefreshLayoutAdapterCallback callback){

        this.callback = callback;
    }


    public interface RefreshLayoutAdapterCallback {

        public void setCurrent(ActivityServer activityServer);
    }
}
