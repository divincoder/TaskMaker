package com.google.developer.taskmaker;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.TaskUpdateService;
import com.google.developer.taskmaker.reminders.AlarmScheduler;
import com.google.developer.taskmaker.views.DatePickerFragment;
import com.google.developer.taskmaker.views.TaskTitleView;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener{
    private TaskTitleView nameView;
    private TextView dateView;
    private ImageView priorityView;
    private Uri mUri;
    private long mDueDate = Long.MAX_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        //Task must be passed to this activity as a valid provider Uri
        mUri = getIntent().getData();

        //TODO: Display attributes of the provided task in the UI
        nameView = (TaskTitleView) findViewById(R.id.text_description);
        dateView = (TextView) findViewById(R.id.text_date);
        priorityView = (ImageView) findViewById(R.id.priority);
        displayTaskData(mUri);
    }

    public void displayTaskData(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null)
            return;
        cursor.moveToFirst();
        int priority = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.TaskColumns.IS_PRIORITY));
        long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.TaskColumns.DUE_DATE));
        nameView.setText(cursor.getString(cursor.getColumnIndex(DatabaseContract.TaskColumns.DESCRIPTION)));

        if (priority == 1) {
            priorityView.setImageResource(R.drawable.ic_priority);
        } else {
            priorityView.setImageResource(R.drawable.ic_not_priority);
        }
        if (dueDate == Long.MAX_VALUE) {
            dateView.setVisibility(View.INVISIBLE);
        } else {
            CharSequence formatted = DateUtils.getRelativeTimeSpanString(this, dueDate);
            dateView.append(" " + formatted);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Implementing Click actions for the menu items
        switch(item.getItemId()){
            case R.id.action_delete:
                TaskUpdateService.deleteTask(this, mUri);
                finish();
                break;
            case R.id.action_reminder:
                DatePickerFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getSupportFragmentManager(), "datePicker");
                scheduleAlarm();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //TODO: Handle date selection from a DatePickerFragment
        //Set to noon on the selected day
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 15);
        c.set(Calendar.MINUTE, 17);
        c.set(Calendar.SECOND, 0);
        setDateSelection(c.getTimeInMillis());
    }

    private void setDateSelection(long dueDate){
        mDueDate = dueDate;
    }

    public long getDateSelection() {
        return mDueDate;
    }

    //schedules alarm at noon for the chosen day
    private void scheduleAlarm(){
            AlarmScheduler.scheduleAlarm(this, getDateSelection(), mUri);
    }
}
