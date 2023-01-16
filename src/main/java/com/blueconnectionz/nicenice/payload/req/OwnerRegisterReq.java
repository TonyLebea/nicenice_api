package com.blueconnectionz.nicenice.payload.req;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class OwnerRegisterReq {
    @Email
    @Size(max = 50)
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 8, max = 50)
    private String password;
    @NotBlank
    @Size(min = 10)
    private String phoneNumber;
}