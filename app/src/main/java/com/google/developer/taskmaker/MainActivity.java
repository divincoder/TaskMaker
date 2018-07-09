package com.google.developer.taskmaker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.developer.taskmaker.data.DatabaseContract;
import com.google.developer.taskmaker.data.TaskAdapter;
import com.google.developer.taskmaker.data.DatabaseContract.TaskColumns;
import com.google.developer.taskmaker.data.TaskUpdateService;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        TaskAdapter.OnItemClickListener,
        View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private TaskAdapter mAdapter;
    private Cursor mCursor;
    RecyclerView recyclerView;
    private static final int TASK_LOADER = 1;
    private String sortOrder = DatabaseContract.DATE_SORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);
        //set up sharedPreferences
        setupSharedPreferences();

        // Kick off the loader
        getSupportLoaderManager().initLoader(TASK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, long id) {
        //TODO: Handle list item click event
        Uri currentTaskUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, id);
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.setData(currentTaskUri);
        startActivity(intent);
    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, long id) {
        //TODO: Handle task item checkbox event
        ContentValues values = new ContentValues();
        if (active) {
            values.put(TaskColumns.IS_COMPLETE, 0);
        } else {
            values.put(TaskColumns.IS_COMPLETE, 1);
        }
        Uri currentTaskUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, id);
        TaskUpdateService.updateTask(this, currentTaskUri, values);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (id == TASK_LOADER) {
            return new CursorLoader(this,
                    DatabaseContract.CONTENT_URI,
                    null,
                    null,
                    null,
                    sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case TASK_LOADER:
                //Update the TaskAdapter with the new Cursor
                if (data.getCount() > 0) {
                    mAdapter.swapCursor(data);
                } else {
                    mAdapter.swapCursor(null);
                }
                break;
        }
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mAdapter.swapCursor(null);
    }

    private void setupSharedPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Register the listener
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.pref_sortBy_due))) {
            sortOrder = DatabaseContract.DATE_SORT;
        } else {
            sortOrder = DatabaseContract.DEFAULT_SORT;
        }
        getSupportLoaderManager().restartLoader(TASK_LOADER, null, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
