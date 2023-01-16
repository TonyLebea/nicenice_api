package com.blueconnectionz.nicenice.payload.req;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Component
public class CarRegisterReq {
/*    private MultipartFile doubleDisk;
    private MultipartFile carImage;*/
    private String make;
    private String model;
    private String year;
    private String city;
    private String weeklyTarget;
    private boolean depositRequired;
    private boolean hasInsurance;
    private boolean hasTracker;
    private boolean activeOnHailingPlatforms;
    private boolean active;

/*
    public MultiValueMap<String, Object>  getRequestBody() {
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("doubleDisk", this.getDoubleDisk());
        requestBody.add("carImage", this.getCarImage());
        requestBody.add("make", this.getMake());
        requestBody.add("model", this.getModel());
        requestBody.add("year", this.getYear());
        requestBody.add("city", this.getCity());
        requestBody.add("weeklyTarget", this.getWeeklyTarget());
        requestBody.add("depositRequired", this.isDepositRequired());
        requestBody.add("hasInsurance", this.isHasInsurance());
        requestBody.add("activeOnHailingPlatforms", this.isActiveOnHailingPlatforms());
        requestBody.add("active", this.isActive());
        return requestBody;
    }
*/






}
