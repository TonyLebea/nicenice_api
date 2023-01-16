package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllCarsRes {
    private String image;
    private String make;
    private String model;
    private String type;
    private String platform;
    private String owner;
    private String status;
    private String totalCredits;
}
