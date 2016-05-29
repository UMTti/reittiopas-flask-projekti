package com.example.akat.stop2_user;

public class Bus {
    
    private String line;
    private String arrival;
    
    public Bus(String line, String arrival) {
        this.line = line;
        this.arrival = arrival;
    }
    
    public void setLine(String line) {
        this.line = line;
    }
    
    public String getLine() {
        return this.line;
    }
    
    public void setArrival(String arrival) {
        this.arrival = arrival;
    }
    
    public String getArrival() {
        return this.arrival;
    }
    
    public String toString() {
        return this.line;
    }

}
