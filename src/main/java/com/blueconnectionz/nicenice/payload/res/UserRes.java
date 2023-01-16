package com.blueconnectionz.nicenice.payload.res;

import com.blueconnectionz.nicenice.model.Document;
import lombok.*;

import java.util.List;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRes {
    private String fullName;
    private String email;
    private String phoneNumber;
    List<Document> documents;

}

