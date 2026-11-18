package com.tour.registration.service;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super("找不到這筆報名");
    }
}
