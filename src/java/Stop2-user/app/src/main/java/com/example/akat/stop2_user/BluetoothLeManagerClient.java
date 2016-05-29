package com.example.akat.stop2_user;

/**
 * Created by marku on 12.3.2016.
 */
public interface BluetoothLeManagerClient {

    /**
     * Run when an iBeacon that hasn't been seen ever or lately is discovered.
     * @param iBeacon Details of the discovered iBeacon
     */
    public void onNewBeaconFound(BeaconInstance iBeacon);

    /**
     * Run when a previously seen iBeacon is seen again on a cyclical scan. The RSSI and estimated
     * distance may or may not have changed.
     * @param iBeacon   Details of the iBeacon
     */
    public void onBeaconRescan(BeaconInstance iBeacon);

    /**
     * Run when an iBeacon that was previously seen has not been seen in X minutes.
     * TODO: Decide the time.
     * @param iBeacon   Details of the iBeacon
     */
    public void onBeaconLost(BeaconInstance iBeacon);

}
