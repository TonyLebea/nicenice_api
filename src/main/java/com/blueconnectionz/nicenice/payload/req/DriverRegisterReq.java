package com.blueconnectionz.nicenice.payload.req;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class DriverRegisterReq {
    private String fullName;
    private String email;
    private String password;
    private String location;
    //private List<MultipartFile> documents;
    private String phoneNumber;
    private String platform;
    private String reference1;
    private String reference2;
}
