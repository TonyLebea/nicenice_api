package com.blueconnectionz.nicenice.payload.req;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DriverReq {
    private String fullName;
    String phoneNumber;
    private String location;
    private int creditBalance;
}
