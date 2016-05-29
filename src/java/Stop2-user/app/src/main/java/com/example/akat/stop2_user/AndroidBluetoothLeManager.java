package com.example.akat.stop2_user;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by marku on 12.3.2016.
 */
public class AndroidBluetoothLeManager implements BluetoothLeManager {

    // Ideally an odd number. If we are scanning for beacons once a second then e.g. a value of 5
// means that there's at most a 2...3 second delay when reacting to a sudden change.
    public static final int PROXIMITY_MEDIAN_FILTER_WINDOW = 5;
    public static final int RSSI_UNKNOWN_VALUE = -99;
    public static final double DISTANCE_UNKNOWN_VALUE = 99.9;

    // private static final int REQUEST_ENABLE_BT = 1; // Any random value will do
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothLeManagerClient> mClients =
            new ArrayList<BluetoothLeManagerClient>(); // TODO: Switch to HashSet to automatically avoid duplicates(?)


    private boolean mBluetoothAvailable;
    private BluetoothAdapter.LeScanCallback mScanCallback;
    private boolean mKeepScanning;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    // A collection of beacons that were within the scanning range at
    // the time of starting a new scan
    private Map<String, BeaconInstance> mBeacons = new HashMap<String, BeaconInstance>();
    // A temporary collection of beacons found with the latest scan
    private Map<String, BeaconInstance> mLatestBeacons = new HashMap<String, BeaconInstance>();
    // Scan cyclically
    private Runnable mCycleTimer;

    // Not every beacon is always seen on every scan cycle even if scanning for several seconds. So
    // a short scan quite frequently will also do the job.
    // Scan (roughly) every x milliseconds
    private static final long CYCLE_PERIOD = 1 * 1000; // TODO: Optimal value?
    // Scan for x milliseconds and stop
    private static final long SCAN_DURATION = 500; // TODO: Optimal value?
    // Set beacon as "lost" after x milliseconds.
    private static final long BEACON_LOST_THRESHOLD = 10 * 1000; // TODO: Optimal value?
    private Thread mCycleThread;

    public AndroidBluetoothLeManager(final Context context) {


        // Initializes the Bluetooth adapter.
        mBluetoothManager =
                (BluetoothManager) context.getSystemService(
                        Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();


        // Ensures Bluetooth is available on the device and it is enabled.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAvailable = false;
            // TODO: A UI app. could request the user to switch BT on with:
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothAvailable = true;
        }

        // Instantiate and implement the scan cycle timer
        mCycleTimer = new Runnable() {

            @Override
            public void run() {
                while (mKeepScanning) {
                    // Start a scan
                    scanLeDevice(true);
                    // Wait
                    try {
                        Thread.sleep(CYCLE_PERIOD);
                    } catch (InterruptedException e) {
                        // The thread was already interrupted. No big deal.
                        e.printStackTrace();
                    }
                }
            }
        };

        // Set up a callback for the scanner
        mScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 final byte[] scanRecord) {


                //Log.d(this.getClass().getName(), " => onLeScan()");

                String address = "";
                String uuid = "";
                int major = 0;
                int minor = 0;
                byte txPower = 0;

                // A device was found. Parse the data.
                // For the technical details look at:
                // http://kittensandcode.blogspot.co.uk/2014/08/ibeacons-and-android-parsing-uuid-major.html
                // http://stackoverflow.com/questions/18906988/what-is-the-ibeacon-bluetooth-profile
                // http://docs.kontakt.io/beacon/kontakt-beacon-v2.pdf
                int startByte = 2;
                boolean patternFound = false;
                while (startByte <= 5) {
                    if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                            ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                        patternFound = true;
                        break;
                    }
                    startByte++;
                }

                if (patternFound && mKeepScanning) {
                    // Convert to hex String
                    byte[] uuidBytes = new byte[16];
                    System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                    String hexString = bytesToHex(uuidBytes);

                    // Address
                    address = device.getAddress();

                    // UUID
                    uuid = hexString.substring(0, 8) + "-" +
                            hexString.substring(8, 12) + "-" +
                            hexString.substring(12, 16) + "-" +
                            hexString.substring(16, 20) + "-" +
                            hexString.substring(20, 32);

                    // Major value
                    major = (scanRecord[startByte + 20] & 0xff) * 0x100 +
                            (scanRecord[startByte + 21] & 0xff);

                    // Minor value
                    minor = (scanRecord[startByte + 22] & 0xff) * 0x100 +
                            (scanRecord[startByte + 23] & 0xff);

                    // txPower
                    txPower = (byte) (scanRecord[startByte + 24]);


                    final String finalAddress = address;
                    final String finalUuid = uuid;
                    final int finalMajor = major;
                    final int finalMinor = minor;
                    final byte finalTxPower = txPower;


                    // Do data updates on main thread. (Avoids ConcurrentModificationException from
                    // Java Collection classes' iterators.)
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Add any new beacon to the set of found beacons.
                            // Use the address as a unique identifier to prevent
                            // adding the same beacon again as the callback is
                            // triggered again even for the same beacon as before.
                            if (!mLatestBeacons.containsKey(finalAddress)) {
// Instantiate a Beacon and set the basic data
                                final BeaconInstance beacon = new BeaconInstance();
                                beacon.setAddress(finalAddress);
                                beacon.setName(device.getName());
                                beacon.setUuid(finalUuid);
                                beacon.setMajor(finalMajor);
                                beacon.setMinor(finalMinor);
                                beacon.setRssi(rssi);
                                beacon.setTxPower(finalTxPower);

                                mLatestBeacons.put(beacon.getAddress(), beacon);

                                // Logs each beacon only once per scanning cycle
                                //Log.d(this.getClass().toString(), "----------\n");
                                //Log.d(this.getClass().toString(), beacon.toString());
                                //Log.d(this.getClass().toString(), "----------\n");

                            } else {
                                // Average the RSSI value of previously found beacons
                                // with a 50/50 weighted average of the latest value
                                // vs. previous averaged value.
                                mLatestBeacons.get(finalAddress).setRssi(
                                        (mLatestBeacons.get(finalAddress).getRssi() +
                                                rssi) / 2);
                            }

                            // Update the "last seen" time stamp.
                            mLatestBeacons.get(finalAddress).
                                    setLastSeen(System.currentTimeMillis());

                            // Logs each beacon every time it's (re)discovered on a scanning cycle
                            /*
                            Log.d(this.getClass().toString(), "----------\n");
				            Log.d(this.getClass().toString(), beacon.toString());
				            Log.d(this.getClass().toString(), "----------\n");
				            */
                        }
                    });
                } else {
                    // Not an iBeacon or StopListening() has been called
                }
                //Log.d(this.getClass().getName(), " <= onLeScan()");
            }
        };

        mCycleThread = new Thread(mCycleTimer);

    }


    // Scans for Bluetooth LE devices for SCAN_DURATION milliseconds and
// triggers mScanCallback for each found device
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after SCAN_DURATION milliseconds.

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mBluetoothAdapter.stopLeScan(mScanCallback);
                    onScanStopped();
                }
            }, SCAN_DURATION);

            // Starts the scanning
            boolean scanStarted = mBluetoothAdapter.startLeScan(mScanCallback);
            //Log.d(this.getClass().toString(), "Scanning started: " + scanStarted);
        } else {
            // Stops scanning for other reasons - Call scanLeDevice(false) to stop
            mBluetoothAdapter.stopLeScan(mScanCallback);
        }

    }

    protected void onScanStopped() {
        // We come here once a scanning cycle has stopped.
        //Log.d(this.getClass().getName(), " => onScanStopped()");
        if (mKeepScanning) {
            //Log.d(this.getClass().toString(), "Beacons found: " + mLatestBeacons.size());
            // The listener is running
            for (String s : mLatestBeacons.keySet()) {

                // Estimate the distance

                /* Option 1
                // This is based just on the inverse-square law
                // (http://en.wikipedia.org/wiki/Inverse-square_law)
                // and doesn't consider attenuation of electromagnetic radiation.
                // There isn't much difference with option #2 in close distance (< 10 m, estimated)
                // which is the most important for us. This seems to give higher distance values
                // than option #2 when further away.

                // As the values are in decibels the ratio is the difference.
                // Converting to linear values with 10^(power/10) and dividing
                // TxPower / RSSI would just give the same value with more work.
                double dbRatio = mLatestBeacons.get(s).getTxPower() -
                        mLatestBeacons.get(s).getRssi();

                // Convert the ratio in dB into a linear ratio:
                double linearRatio = Math.pow(10, dbRatio / 10);
                // If we wanted to include a path loss coefficient the formula
                // above could be:
                // double linearRatio = Math.pow(10, dbRatio / (10 * path_loss_coefficient));

                // According to the inverse-square law:
                // RSSI = TxPower/ distance^2
                // i.e. distance = SQRT(TxPower / RSSI) where
                // TxPower / RSSI = linearRatio.
                double estimatedDistance = Math.sqrt(linearRatio);
                // No need for several decimals or exact rounding. This will be sufficient:
                int temp = (int)(10 * estimatedDistance);
                estimatedDistance = temp;
                estimatedDistance /= 10;
                */

                // Option 2
                // This is an experimentation based formula suggested by a Radius (beacon
                // manufacturer) software developer on StackOverflow:
                // http://stackoverflow.com/questions/20416218/understanding-ibeacon-distancing
                // More about the topic:
                // http://developer.radiusnetworks.com/2014/12/04/fundamentals-of-beacon-ranging.html
                // This is supposed to "roughly approximate what iOS does" which would be ideal for
                // us. The behaviour will vary from device to device however.
                // Somebody else points out on the same StackOverflow topic that the "accuracy"
                // according to Apple's documentation "indicates the one sigma horizontal accuracy
                // in meters" which in practise would mean that "if mean of measurement is X, and
                // one sigma is ?, then 68% of all measurements will be between X - ? and X + ?".
                double estimatedDistance;
                double ratio = mLatestBeacons.get(s).getRssi();
                ratio /= mLatestBeacons.get(s).getTxPower();
                if (ratio < 1.0) {
                    estimatedDistance = Math.pow(ratio, 10);
                } else {
                    estimatedDistance = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
                }
                // No need for several decimals or exact rounding. This will be sufficient:
                int temp = (int) (10 * estimatedDistance);
                estimatedDistance = temp;
                estimatedDistance /= 10;

                if (!mBeacons.containsKey(s)) {
                    //Log.d(this.getClass().toString(), "New beacon");
                    // This is a newly found beacon. (Not seen in previous scans)
                    // Copy the beacon to the set of recently found beacons.
                    mBeacons.put(s, mLatestBeacons.get(s));

                    // Inform the clients.
                    for (BluetoothLeManagerClient client : mClients) {
                        //Log.d(this.getClass().toString(), "Inform clients...");
                        //client.newlyFoundBeacon(mLatestBeacons.get(s));
                        client.onNewBeaconFound(mLatestBeacons.get(s));
                    }

                } else {
                    // Update the already existing beacon's details.
                    mBeacons.get(s).setRssi(mLatestBeacons.get(s).getRssi());
                    mBeacons.get(s).setEstimatedDistance(estimatedDistance);
                    mBeacons.get(s).setLastSeen(System.currentTimeMillis());


                    // Some median filtering for the estimated distance to filter out sudden spikes.
                    //
                    // According to Radius(*) iOS uses quite heavy averaging which causes the
                    // "accuracy" to follow the user's moves with a delay. Radius also uses some
                    // averaging. Based on a quick test with some recorded RSSI values and Excel a
                    // 5 point median filtering would smooth out random spikes quite well and not
                    // introduce any significant delay.
                    // (*) http://developer.radiusnetworks.com/2014/12/04/fundamentals-of-beacon-ranging.html
                    mBeacons.get(s).getLatestDistances().add(
                            mBeacons.get(s).getEstimatedDistance());
                    if (mBeacons.get(s).getLatestDistances().size() >
                            PROXIMITY_MEDIAN_FILTER_WINDOW) {
                        mBeacons.get(s).getLatestDistances().remove(0);

                        Double[] tempArray = mBeacons.get(s).getLatestDistances().
                                toArray(new Double[5]);
                        Arrays.sort(tempArray);
                        mBeacons.get(s).setEstimatedDistance(
                                tempArray[PROXIMITY_MEDIAN_FILTER_WINDOW / 2]);

                    }


                    // Inform the clients.
                    for (BluetoothLeManagerClient client : mClients) {
                        //client.rescannedBeacon(mBeacons.get(s));
                        client.onBeaconRescan(mBeacons.get(s));
                    }
                }

            }

            // Remove beacons that we haven't seen for a while(?)
            Iterator<String> iterator = mBeacons.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (mBeacons.get(key).getLastSeen() <
                        System.currentTimeMillis() - (BEACON_LOST_THRESHOLD)) {
                    // We haven't seen this beacon in x milliseconds.
                    // Inform the clients.
                    for (BluetoothLeManagerClient client : mClients) {
                        // Set RSSI and estimated distance to dummy values.
                        mBeacons.get(key).setRssi(RSSI_UNKNOWN_VALUE);
                        mBeacons.get(key).setEstimatedDistance(DISTANCE_UNKNOWN_VALUE);
                        // Clear the median filtering cache.
                        mBeacons.get(key).getLatestDistances().clear();

                        client.onBeaconLost(mBeacons.get(key));
                    }

                    // Remove it from the set of visible beacons.
                    iterator.remove();
                }
            }

            // Clear the temporary set of beacons from the last scan.
            mLatestBeacons.clear();
        } else {
            // We came here because the listener was stopped.

        }
        //Log.d(this.getClass().getName(), " <= onScanStopped()");
    }

    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    public void startMonitoring() {
        Log.d(this.getClass().getName(), "startMonitoring()");
        if (mBluetoothAvailable) {
            mKeepScanning = true;

            if (!mCycleThread.isAlive()) {
                mCycleThread = new Thread(mCycleTimer);
                mCycleThread.start();
                Log.d(this.getClass().getName(), "...Started!");
            } else {
                Log.d(this.getClass().getName(), "...Already running!");
            }

        } else {
            Log.d(this.getClass().getName(), "*** BlueTooth not available! ***");
            // Just don't do anything(?)
        }
    }

    @Override
    public void stopMonitoring() {
        Log.d(this.getClass().getName(), "stopMonitoring()");
        mKeepScanning = false;
        if (mCycleThread != null && mCycleThread.isAlive() && !mCycleThread.isInterrupted()) {
            mCycleThread.interrupt();
        }
        scanLeDevice(false);
    }

    @Override
    public void addClient(BluetoothLeManagerClient client) {
        Log.d(this.getClass().getName(), "addClient(" + client.toString() + ")");
        if (mClients.isEmpty()) {
            startMonitoring();
        }
        mClients.add(client);
        Log.d(this.getClass().getName(), "..." + mClients.size() + " clients now");
    }

    @Override
    public void removeClient(BluetoothLeManagerClient client) {
        Log.d(this.getClass().getName(), "removeClient(" + client.toString() + ")");
        if (mClients.contains(client)) {
            mClients.remove(client);
        }
        if (mClients.isEmpty()) {
            Log.d(this.getClass().getName(), "...No more clients");
            stopMonitoring();
        }
    }

    public boolean isBluetoothAvailable() {
        return mBluetoothAvailable;
    }

    public void setBluetoothAvailable(boolean bluetoothAvailable) {
        this.mBluetoothAvailable = bluetoothAvailable;
    }
}

