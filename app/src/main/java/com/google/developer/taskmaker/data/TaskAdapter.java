package com.google.developer.taskmaker.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.R;
import com.google.developer.taskmaker.views.TaskTitleView;
import com.google.developer.taskmaker.data.DatabaseContract.TaskColumns;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, long id);

        void onItemToggled(boolean active, long id);
    }

    /* ViewHolder for each task item */
    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TaskTitleView nameView;
        private TextView dateView;
        private ImageView priorityView;
        private CheckBox checkBox;

        public TaskHolder(View itemView) {
            super(itemView);

            nameView = (TaskTitleView) itemView.findViewById(R.id.text_description);
            dateView = (TextView) itemView.findViewById(R.id.text_date);
            priorityView = (ImageView) itemView.findViewById(R.id.priority);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (v == checkBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.checkBox.isChecked(), (long) holder.itemView.getTag());
        }

    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, (long) holder.itemView.getTag());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_task, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {
        //TODO: Bind the task data to the views
        if (mCursor.moveToPosition(position)) {
            String description = mCursor.getString(mCursor.getColumnIndexOrThrow(TaskColumns.DESCRIPTION));
            int priority = mCursor.getInt(mCursor.getColumnIndexOrThrow(TaskColumns.IS_PRIORITY));
            int checkBoxState = mCursor.getInt(mCursor.getColumnIndexOrThrow(TaskColumns.IS_COMPLETE));
            long dueDate = mCursor.getLong(mCursor.getColumnIndexOrThrow(TaskColumns.DUE_DATE));
            long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(TaskColumns._ID));

            holder.nameView.setText(description);

            if (checkBoxState == 0) {
                holder.checkBox.setChecked(false);
            } else {
                holder.checkBox.setChecked(true);
            }

            if (priority == 1) {
                holder.priorityView.setImageResource(R.drawable.ic_priority);
            } else {
                holder.priorityView.setImageResource(R.drawable.ic_not_priority);
            }
            if (!getItem(position).hasDueDate()) {
                holder.dateView.setVisibility(View.INVISIBLE);
            } else {
                CharSequence formatted = DateUtils.getRelativeTimeSpanString(mContext, dueDate);
                holder.dateView.setText(formatted);
            }
            holder.itemView.setTag(id);
        }
    }

    @Override
    public int getItemCount() {

        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }

}
