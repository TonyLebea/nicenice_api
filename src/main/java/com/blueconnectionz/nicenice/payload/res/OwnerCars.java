package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OwnerCars {
    String image;
    String name;
    int numConnections;
    boolean available;
}
