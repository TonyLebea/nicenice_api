package com.blueconnectionz.nicenice.payload.res;

import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRes {
    private Long id;
    private String email;
    private List<String> role;
}
