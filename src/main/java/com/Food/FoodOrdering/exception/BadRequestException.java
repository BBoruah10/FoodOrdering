package com.Food.FoodOrdering.exception;


public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}

