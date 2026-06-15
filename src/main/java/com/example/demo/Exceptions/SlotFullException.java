package com.example.demo.Exceptions;

public class SlotFullException extends RuntimeException {
    public SlotFullException(String message) { super(message); }
}