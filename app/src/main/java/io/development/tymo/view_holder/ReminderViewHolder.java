package io.development.tymo.view_holder;

        import android.content.Context;
        import android.support.v4.content.ContextCompat;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.LinearLayout;
        import android.widget.TextView;

        import com.jude.easyrecyclerview.adapter.BaseViewHolder;

        import io.development.tymo.R;
        import io.development.tymo.models.cards.Reminder;


public class ReminderViewHolder extends BaseViewHolder<Reminder> {
    public TextView text;
    public LinearLayout cardBox;
    public TextView time;
    public LinearLayout box;
    public Context mContext;

    public ReminderViewHolder(ViewGroup parent, Context context) {
        super(parent, R.layout.card);
        time = $(R.id.timeBox);
        text = $(R.id.infoBox);
        box = $(R.id.iconBox);
        cardBox = $(R.id.cardBox);
        mContext = context;

        box.setVisibility(View.GONE);
        text.setVisibility(View.VISIBLE);
    }

    @Override
    public void setData(Reminder card){
        time.setText(card.getTime());
        text.setText(card.getText());
        time.setTextColor(ContextCompat.getColor(mContext,R.color.black));
        time.setBackgroundColor(ContextCompat.getColor(mContext,R.color.transparent));
        cardBox.setBackgroundResource(R.drawable.ic_reminder);
    }
}
