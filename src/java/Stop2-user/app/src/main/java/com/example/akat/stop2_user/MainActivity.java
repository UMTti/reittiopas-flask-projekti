package com.example.akat.stop2_user;

// import android.support.design.widget.FloatingActionButton;
// import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.util.JsonReader;

public class MainActivity extends Activity implements BluetoothLeManagerClient{

    // Beacons
    private BluetoothLeManager bluetoothLeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);

        // Beacons
        bluetoothLeManager = new AndroidBluetoothLeManager(this);
        bluetoothLeManager.addClient(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothLeManager.stopMonitoring();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothLeManager.startMonitoring();
    }

    // Beacons
    @Override
    public void onNewBeaconFound(BeaconInstance iBeacon) {
        // if (iBeacon.getUuid().equals("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6\n")) {
        // This is "bus beacon"

        if (iBeacon.getUuid().equals("7b44b47b-52a1-5381-90c2-f09b6838c5d4")) {
            // This is Ronja's iPhone
            showBuses(iBeacon.getUuid());

                if ((iBeacon.getMajor() == 46581 && iBeacon.getMinor() == 63707) || // FLn1
                    iBeacon.getMajor() == 50809 && iBeacon.getMinor() == 50105) { // Efyk

                Log.d(this.getClass().getName(), "Found a new beacon. Distance: " + iBeacon.getEstimatedDistance());

                if (iBeacon.getEstimatedDistance() <= 1)  {
                    Log.d(this.getClass().getName(), "MIPsoft beacon is near.");
                }
            }
        }
    }

    @Override
    public void onBeaconRescan(BeaconInstance iBeacon) {

        // if (iBeacon.getUuid().equals("2f234454-cf6d-4a0f-adf2-f4911ba9ffa6\n")) {
        // This is "bus beacon"
        if (iBeacon.getUuid().equals("7b44b47b-52a1-5381-90c2-f09b6838c5d4")) {
            // This is Ronja's iPhone
            
            showBuses(iBeacon.getUuid());
            if ((iBeacon.getMajor() == 46581 && iBeacon.getMinor() == 63707) || // FLn1
                    iBeacon.getMajor() == 50809 && iBeacon.getMinor() == 50105) { // Efyk

                // Near
                if (iBeacon.getEstimatedDistance() <= 1) {

                    Log.d(this.getClass().getName(), "beacon is near. Distance: " + iBeacon.getEstimatedDistance());
                }
                // Far
                else if (iBeacon.getEstimatedDistance() > 4) {
                    // It's no longer nearby.
                    Log.d(this.getClass().getName(), "beacon is not near. Distance: " + iBeacon.getEstimatedDistance());
                }
            }
        }
    }

    @Override
    public void onBeaconLost(BeaconInstance iBeacon) {

        if ((iBeacon.getMajor() == 46581 && iBeacon.getMinor() == 63707) || // FLn1
                iBeacon.getMajor() == 50809 && iBeacon.getMinor() == 50105) { // Efyk

            // beacon was lost.
        }
    }
    
    // Data
    public void showBuses(String beaconId) {
        // Test data
        String[] busArray = {"733", "724", "717", "731"};
        
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, busArray);
        ListView listView = (ListView) findViewById(R.id.stop_list);
        listView.setAdapter(adapter);
        
    }

    // Read json
    // public List readJsonStream(InputStream is) throws IOException {
    //     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
    //
    //     try {
    //         return readJsonArray(reader);
    //     } finally {
    //         reader.close();
    //     }
    // }
    //
    // public List readJsonArray(JsonReader reader) throws IOException {
    //     List jsons = new ArrayList();
    //     reader.beginArray();
    //
    //     while (reader.hasNext()) {
    //         jsons.add(readMessage(reader));
    //     }
    //
    //     reader.endArray();
    //
    //     return jsons;
    // }
    //
    // public Stop readMessages(JsonReader reader) throws IOException {
    //     String code = null;
    //     String stopname = null;
    //     List<Buss> buses;
    //
    //     reader.beginObject();
    //
    //     while (reader.hashNext()) {
    //         String name = reader.nextName();
    //         if (name.equals("stop_code")) {
    //             code = reader.nextString();
    //         } else if (name.equals("stop_name")) {
    //             stopname = reader.nextString();
    //         } else if (name.equals("schedule") && reader.peek() != JsonToken.NULL) {
    //             buses.add(readBuses(reader));
    //         } else {
    //             reader.skipValue();
    //         }
    //     }
    //
    //     reader.endObject();
    //
    //     return new Stop(code, stopname, buses);
    // }
    //
    // public Bus readBuses(JsonReader reader) throws IOException {
    //     String line = null;
    //     String arrival = null;
    //
    //     reader.beginObject();
    //
    //     while (reader.hasNext()) {
    //         String name = reader.nextName();
    //
    //         if (name.equals("line")) {
    //             line = reader.nextString();
    //         } else if (name.equals("arrival")) {
    //             arrival = reader.nextString();
    //         } else {
    //             reader.skipValue();
    //         }
    //     }
    //
    //     reader.endObject();
    //
    //     return new Bus(line, arrival);
    // }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //     return true;
        // }
        return super.onOptionsItemSelected(item);
    }
    
}
