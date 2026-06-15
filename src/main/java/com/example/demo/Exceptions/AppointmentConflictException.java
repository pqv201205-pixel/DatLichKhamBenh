package com.example.demo.Exceptions;

public class AppointmentConflictException extends RuntimeException {
    public AppointmentConflictException(String message) { super(message); }
}