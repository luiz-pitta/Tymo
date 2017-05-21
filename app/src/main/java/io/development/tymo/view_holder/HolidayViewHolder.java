package io.development.tymo.view_holder;

        import android.content.Context;
        import android.support.v4.content.ContextCompat;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.jude.easyrecyclerview.adapter.BaseViewHolder;

        import io.development.tymo.R;
        import io.development.tymo.models.cards.HolidayCard;


public class HolidayViewHolder extends BaseViewHolder<HolidayCard> {
    public ImageView icon;
    private Context mContext;



    public HolidayViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card_special);
        icon = $(R.id.icon);
        this.mContext = context;
    }

    @Override
    public void setData(HolidayCard card){
        icon.setImageResource(R.drawable.ic_holiday);
    }
}
