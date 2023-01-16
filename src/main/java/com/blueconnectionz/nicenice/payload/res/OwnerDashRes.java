package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerDashRes {
    private int totalListings;
    private int accepted;
    private int rejected;
    List<OwnerCars> ownerCars;
}
