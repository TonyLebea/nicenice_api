package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllDriversRes {
    private Long id;
    private String fullName;
    private int views;
    private int age;
    private String location;
    private int numReferences;
    private String imageURL;
    private boolean online;
}
