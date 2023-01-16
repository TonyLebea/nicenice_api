package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarInfo {
    private long id;
    private String image;
    private String make;
    private String model;
    private String year;
    private String city;
    private String weeklyTarget;
    private boolean depositRequired;
    private boolean hasInsurance;
    private boolean hasTracker;
    private boolean activeOnHailingPlatforms;
    private int numConnections;
    private long age;
    private int views;
}
