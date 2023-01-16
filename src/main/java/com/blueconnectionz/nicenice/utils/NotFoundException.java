package com.blueconnectionz.nicenice.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotFoundException extends RuntimeException{
    public NotFoundException(String exception){
        super(exception);
    }
}