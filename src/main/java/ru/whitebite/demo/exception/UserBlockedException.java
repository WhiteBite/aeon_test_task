 
package ru.whitebite.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class UserBlockedException extends RuntimeException {

    private final String ip;

    public UserBlockedException(String ip) {
        super(String.format("Ip [%s] was blocked !", ip));
        this.ip = ip;
    }
}