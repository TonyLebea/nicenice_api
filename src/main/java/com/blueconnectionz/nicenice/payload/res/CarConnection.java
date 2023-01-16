package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarConnection {
    int balance;
    int dealCost;
    String driverRequest;
    int peopleInDeal;
    int peopleWhoAboveMin;

}
