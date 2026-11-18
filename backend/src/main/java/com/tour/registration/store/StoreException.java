package com.tour.registration.store;

/** 存取試算表失敗（網路、權限、配額等）。對外回 503。 */
public class StoreException extends RuntimeException {
    public StoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
