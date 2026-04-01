package com.example.takeit;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends BaseAdapter {

    public interface OnReminderClickListener {
        void onEdit(Reminder reminder);
        void onDelete(Reminder reminder);
    }

    private final Context context;
    private final List<Reminder> reminders;
    private final OnReminderClickListener listener;

    public ReminderAdapter(Context context, List<Reminder> reminders, OnReminderClickListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.listener = listener;
    }

    @Override public int getCount() { return reminders.size(); }
    @Override public Object getItem(int pos) { return reminders.get(pos); }
    @Override public long getItemId(int pos) { return reminders.get(pos).getId(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_reminder, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Reminder reminder = reminders.get(position);
        holder.tvTime.setText(formatTime(reminder.getTimeMinutes()));
        holder.tvTitle.setText(reminder.getTitle());
        holder.tvDescription.setText(reminder.getDescription());
        holder.tvDescription.setVisibility(
                reminder.getDescription() != null && !reminder.getDescription().isEmpty()
                        ? View.VISIBLE : View.GONE);

        // Apply themed card background
        int surfaceColor = NightModeHelper.surface(context);
        int textColor    = NightModeHelper.text(context);
        int hintColor    = NightModeHelper.hint(context);
        int accentColor  = NightModeHelper.accent(context);

        GradientDrawable card = new GradientDrawable();
        card.setShape(GradientDrawable.RECTANGLE);
        card.setColor(surfaceColor);
        card.setCornerRadius(dp(16));
        convertView.setBackground(card);

        holder.tvTime.setTextColor(accentColor);
        holder.tvTitle.setTextColor(textColor);
        holder.tvDescription.setTextColor(hintColor);

        // Tint icons to hint color
        holder.btnEdit.setColorFilter(hintColor);
        holder.btnDelete.setColorFilter(hintColor);

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { listener.onEdit(reminder); }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { listener.onDelete(reminder); }
        });

        return convertView;
    }

    private String formatTime(int timeMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, timeMinutes / 60);
        cal.set(Calendar.MINUTE, timeMinutes % 60);
        return new SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.getTime());
    }

    private float dp(float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static class ViewHolder {
        TextView tvTime, tvTitle, tvDescription;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View v) {
            tvTime        = (TextView)     v.findViewById(R.id.tvDateTime);
            tvTitle       = (TextView)     v.findViewById(R.id.tvTitle);
            tvDescription = (TextView)     v.findViewById(R.id.tvDescription);
            btnEdit       = (ImageButton)  v.findViewById(R.id.btnEdit);
            btnDelete     = (ImageButton)  v.findViewById(R.id.btnDelete);
        }
    }
}
