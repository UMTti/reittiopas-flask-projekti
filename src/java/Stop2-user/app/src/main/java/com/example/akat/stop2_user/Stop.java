package com.example.akat.stop2_user;

import java.util.*;
    
public class Stop {
    
    private String stop_code;
    private String stop_name;
    private List<Bus> schedule;
    
    public Stop(String code, String name, List<Bus> schedule) {
        this.stop_code = code;
        this.stop_name = name;
        this.schedule = schedule;
    }
    
    public void setStop_code(String code) {
        this.stop_code = code;
    }
    
    public String getStop_code() {
        return this.stop_code;
    }
    
    public void setStop_name(String name) {
        this.stop_name = name;
    }
    
    public String getStop__name() {
        return this.stop_name;
    }
    
    public void setSchedule(List<Bus> shcedule) {
        this.schedule = schedule;
    }
    
    public List<Bus> getSchedule() {
        return this.schedule;
    }
}
