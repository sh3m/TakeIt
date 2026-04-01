package com.example.takeit;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderAdapter extends BaseAdapter {

    public interface OnReminderClickListener {
        void onEdit(Reminder reminder);
        void onDelete(Reminder reminder);
        void onToggleDone(Reminder reminder, boolean done);
    }

    private final Context context;
    private final List<Reminder> reminders;
    private final OnReminderClickListener listener;
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault());

    public ReminderAdapter(Context context, List<Reminder> reminders, OnReminderClickListener listener) {
        this.context = context;
        this.reminders = reminders;
        this.listener = listener;
    }

    @Override
    public int getCount() { return reminders.size(); }

    @Override
    public Object getItem(int position) { return reminders.get(position); }

    @Override
    public long getItemId(int position) { return reminders.get(position).getId(); }

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
        holder.tvTitle.setText(reminder.getTitle());
        holder.tvDescription.setText(reminder.getDescription());
        holder.tvDateTime.setText(DATE_FORMAT.format(new Date(reminder.getDateTimeMillis())));

        holder.tvDescription.setVisibility(
                reminder.getDescription() != null && !reminder.getDescription().isEmpty()
                        ? View.VISIBLE : View.GONE);

        // Apply night mode colors to this item
        int surface = NightModeHelper.surface(context);
        int textColor = NightModeHelper.text(context);
        int hintColor = NightModeHelper.hint(context);
        int accentColor = NightModeHelper.accent(context);

        convertView.setBackgroundColor(surface);
        holder.tvTitle.setTextColor(textColor);
        holder.tvDescription.setTextColor(hintColor);
        holder.tvDateTime.setTextColor(accentColor);

        holder.cbDone.setOnCheckedChangeListener(null);
        holder.cbDone.setChecked(reminder.isDone());
        applyDoneStyle(holder, reminder.isDone());

        final ViewHolder finalHolder = holder;
        holder.cbDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btn, boolean checked) {
                applyDoneStyle(finalHolder, checked);
                listener.onToggleDone(reminder, checked);
            }
        });

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onEdit(reminder);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(reminder);
            }
        });

        return convertView;
    }

    private void applyDoneStyle(ViewHolder holder, boolean done) {
        if (done) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(0.5f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setAlpha(1.0f);
        }
    }

    static class ViewHolder {
        TextView tvTitle, tvDescription, tvDateTime;
        CheckBox cbDone;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View v) {
            tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvDescription = (TextView) v.findViewById(R.id.tvDescription);
            tvDateTime = (TextView) v.findViewById(R.id.tvDateTime);
            cbDone = (CheckBox) v.findViewById(R.id.cbDone);
            btnEdit = (ImageButton) v.findViewById(R.id.btnEdit);
            btnDelete = (ImageButton) v.findViewById(R.id.btnDelete);
        }
    }
}
