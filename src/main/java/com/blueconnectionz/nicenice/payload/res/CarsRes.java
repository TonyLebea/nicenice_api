package com.blueconnectionz.nicenice.payload.res;

import com.blueconnectionz.nicenice.model.Car;
import lombok.*;


@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarsRes {
    private Car car;
    private String imageURL;
}
