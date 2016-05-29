package com.example.akat.stop2_user;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by marku on 12.3.2016.
 */
public class BeaconInstance {

    // General iBeacon features
    private String address;
    private String name;
    private String uuid;
    private int major;
    private int minor;
    private int txPower;
    private int rssi;
    // Other data
    private long lastSeen; // Epoch time (UTC)
    private double estimatedDistance; // meters

    private ArrayList<Double> latestDistances = new ArrayList<>();

    /**
     * @return	The time in milliseconds since the beacon was last seen
     */
    public long secondsLastSeen() {
        return System.currentTimeMillis() - lastSeen;
    }

    /**
     * Sets the beacon's "last seen" time to the current moment
     */
    public void updateLastSeen() {
        lastSeen = System.currentTimeMillis();
    }

    /* Not needed except for debugging */
    @Override
    public String toString() {
        Field[] objectFields = this.getClass().getDeclaredFields();
        Object fieldValue;
        String beaconAsString = "\n";

        for (Field f : objectFields) {
            f.setAccessible(true);
            String fieldName = f.getName();
            beaconAsString += fieldName + ":";
            try {
                fieldValue = f.get(this);
                beaconAsString += fieldValue.toString() + "\n";
            } catch (Exception e) {
                beaconAsString += "ERROR" + "\n";
            }
        }
        return beaconAsString;
    }


    // TODO: Setters could have some input data validation implemented
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public int getMajor() {
        return major;
    }
    public void setMajor(int major) {
        this.major = major;
    }
    public int getMinor() {
        return minor;
    }
    public void setMinor(int minor) {
        this.minor = minor;
    }
    public int getRssi() {
        return rssi;
    }
    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
    public long getLastSeen() {
        return lastSeen;
    }
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    public int getTxPower() {
        return txPower;
    }
    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }
    public double getEstimatedDistance() {
        return estimatedDistance;
    }
    public void setEstimatedDistance(double estimatedDistance) {
        this.estimatedDistance = estimatedDistance;
    }
    public ArrayList<Double> getLatestDistances() {
        return latestDistances;
    }

    public void setLatestDistances(ArrayList<Double> latestDistances) {
        this.latestDistances = latestDistances;
    }


}
