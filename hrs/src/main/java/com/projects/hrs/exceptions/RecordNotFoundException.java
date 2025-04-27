package com.projects.hrs.exceptions;


public class RecordNotFoundException extends BaseException {
    private String message;
    public RecordNotFoundException(String message){
        super(message);
    }
}
