package io.development.tymo.view_holder;

        import android.content.Context;
        import android.os.Build;
        import android.support.v4.content.ContextCompat;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.TextView;

        import com.jude.easyrecyclerview.adapter.BaseViewHolder;

        import butterknife.BindView;
        import io.development.tymo.R;
        import io.development.tymo.models.cards.Flag;


public class FlagViewHolder extends BaseViewHolder<Flag> {
    public ImageView icon;
    public TextView time;
    public LinearLayout box, cardBox;
    private Context mContext;

    public FlagViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card);
        icon = $(R.id.icon);
        time = $(R.id.timeBox);
        box = $(R.id.iconBox);
        cardBox = $(R.id.cardBox);
        this.mContext = context;
    }

    @Override
    public void setData(Flag card){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cardBox.setBackground(null);
        } else {
            cardBox.setBackgroundDrawable(null);
        }

        int color;
        if(card.getFree()) {
            card.setIcon(R.drawable.ic_feed_flag_available);
            color = R.color.flag_available;
        }
        else {
            card.setIcon(R.drawable.ic_feed_flag_unavailable);
            color = R.color.flag_unavailable;
        }
        icon.setImageResource(card.getIcon());
        time.setText(card.getTime());
        time.setTextColor(ContextCompat.getColor(mContext,color));

        if(card.getPaint()) {
            box.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_100));
            time.setBackgroundColor(ContextCompat.getColor(mContext, R.color.grey_100));
        }else {
            box.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
            time.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }
    }
}