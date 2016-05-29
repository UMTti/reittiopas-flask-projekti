package com.example.akat.stop2_user;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.Notification;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.StreamHandler;

/**
 * Created by reli on 28.5.2016.
 */

public class ListViewLoader extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    SimpleCursorAdapter mAdapter;

    static final String[] PROJECTION = new String[] {}; // here are the elements from which to select

    static final String SELECTION = ""; // here comes, how to select from database, i.e. which are the stops on this route

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // progress bar to display while loading stops
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new android.support.v7.app.ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);

        // for cursor adapter, specify, which columns go into which views
        String[] fromColumns = {"joo"};
        int[] toViews = {android.R.id.text1}; // the text view in simple_list_item_1

        mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, null, fromColumns, toViews, 0);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(0, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(this, null, PROJECTION, SELECTION, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
