package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DriverTable {
    Long id;
    String fullName;
    String email;
    String phoneNumber;
    String city;
    boolean active;
    int creditBalance;
    String reference1;
    String reference2;
    String documentId;
}
