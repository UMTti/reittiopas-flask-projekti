package com.example.akat.stop2_user;

/**
 * Created by marku on 12.3.2016.
 */
public interface BluetoothLeManager {
    public void startMonitoring();
    public void stopMonitoring();
    public void addClient(BluetoothLeManagerClient client);
    public void removeClient(BluetoothLeManagerClient client);

    //public List<BeaconInstance> getRecentlySeenBeacons(); Needed?

    void setBluetoothAvailable(boolean b);
}
