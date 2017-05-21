package io.development.tymo.view_holder;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.jude.easyrecyclerview.adapter.BaseViewHolder;

import io.development.tymo.R;
import io.development.tymo.model_server.FlagServer;
import io.development.tymo.utils.Constants;
import io.development.tymo.utils.Utilities;

import static android.content.Context.MODE_PRIVATE;


public class FeedFlagViewHolder extends BaseViewHolder<FlagServer> {
    private TextView textViewTitle, textViewDescription;
    private ImageView triangle, photoCreator;
    private RelativeLayout pieceBox, textBox;
    private View photoCreatorRing;
    private RelativeLayout photoCreatorRingBox;

    RotateAnimation rotation;
    TranslateAnimation animation, animation2;
    Context context;

    private String email;

    public FeedFlagViewHolder(ViewGroup parent, final Context context) {
        super(parent, R.layout.list_item_feed_zoom_less);

        SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.USER_CREDENTIALS, MODE_PRIVATE);
        email = mSharedPreferences.getString(Constants.EMAIL, "");

        photoCreator = $(R.id.photoCreator);
        textViewTitle = $(R.id.title);
        textViewDescription = $(R.id.description);
        textBox = $(R.id.infoBox);
        pieceBox = $(R.id.pieceBox);
        triangle = $(R.id.triangle);
        photoCreatorRing = $(R.id.photoCreatorRing);
        photoCreatorRingBox = $(R.id.photoCreatorRingBox);
        this.context = context;

        animation = new TranslateAnimation(0.0f, 0.0f,-Utilities.convertDpToPixel(2, context), Utilities.convertDpToPixel(2, context));
        animation.setDuration(1700);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

        animation2 = new TranslateAnimation(0.0f, 0.0f,Utilities.convertDpToPixel(2, context), -Utilities.convertDpToPixel(2, context));
        animation2.setDuration(800);
        animation2.setRepeatCount(Animation.INFINITE);
        animation2.setRepeatMode(Animation.REVERSE);

        rotation = new RotateAnimation(-3, 3,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotation.setDuration(1200);
        rotation.setRepeatCount(Animation.INFINITE);
        rotation.setRepeatMode(Animation.REVERSE);

        $(R.id.cubeLowerBox).setVisibility(View.GONE);
        $(R.id.cubeUpperBoxIcon).setVisibility(View.GONE);
        $(R.id.flagIcon).setVisibility(View.VISIBLE);
    }

    public void setAnimation(){
        pieceBox.startAnimation(animation2);
        textBox.startAnimation(animation);
        triangle.startAnimation(animation);
    }

    @Override
    public void setData(FlagServer cube){
        itemView.setAlpha(1);

        if(!cube.getUser().getPhoto().matches("")) {
            Glide.clear(photoCreator);
            Glide.with(context)
                    .load(cube.getUser().getPhoto())
                    .asBitmap()
                    .thumbnail(0.1f)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new BitmapImageViewTarget(photoCreator) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            photoCreator.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
        else
            photoCreator.setImageResource(R.drawable.ic_profile_photo_empty);

        if(cube.getFavoriteCreator() > 0){
            photoCreatorRingBox.setVisibility(View.VISIBLE);
            photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_favorite_zoom_less);
        }
        else if(cube.getKnowCreator() > 0){
            photoCreatorRingBox.setVisibility(View.VISIBLE);
            photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_my_contact_zoom_less);
        }
        else if(cube.getUser().getEmail().matches(email)){
            photoCreatorRingBox.setVisibility(View.VISIBLE);
            photoCreatorRing.setBackgroundResource(R.drawable.bg_shape_ring_you_zoom_less);
        }
        else{
            photoCreatorRingBox.setVisibility(View.INVISIBLE);
        }

        textViewTitle.setText(context.getResources().getString(R.string.flag_available));
        textViewTitle.setTextColor(ContextCompat.getColor(context, R.color.flag_available));
        textViewDescription.setText(cube.getTitle());

        pieceBox.startAnimation(animation2);

        textBox.startAnimation(animation);
        triangle.startAnimation(animation);
    }


}
