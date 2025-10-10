package com.codingshuttle.projects.airBnbApp.ExceptionHandler;

public class DuplicateGuestException extends RuntimeException{
    public DuplicateGuestException(String message){super(message);}
}
