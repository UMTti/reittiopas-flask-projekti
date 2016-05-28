package com.example.reli.driverview;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ShowStops extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_stops);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] stopArray = {"Suursuon sairaala", "Pirjontie", "Maunula", "Maunulanmäki", "Kuusikkotie", "Vesakkotie", "Koivikkotie", "Niittyläntie", "Autokuljetuskeskus 2", "Autokuljetuskeskus 1", "Posti 2", "Posti 1", "Ilmalan bussivarikko", "Ilmalan seisake", "Ilmalanrinne", "Ilmalantori", "TV-keskus"};


        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, stopArray);
        ListView listView = (ListView) findViewById(R.id.stop_list);
        listView.setAdapter(adapter);

 //       FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
 //       fab.setOnClickListener(new View.OnClickListener() {
 //           @Override
 //           public void onClick(View view) {
 //               Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
 //                       .setAction("Action", null).show();
 //           }
 //       });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_stops, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
