package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardRes {
    int newOwners;
    int newDrivers;
    int totalUsers;
    int approvedOwners;
    int approvedDrivers;
    int niceNiceDeals;
    int mobileUsers;
    int webUsers;
    String totalRevenue;
}
