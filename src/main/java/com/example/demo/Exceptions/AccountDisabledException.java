package com.example.demo.Exceptions;

public class AccountDisabledException extends RuntimeException {
    public AccountDisabledException(String message) { super(message); }
}