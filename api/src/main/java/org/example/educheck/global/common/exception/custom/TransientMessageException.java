package org.example.educheck.global.common.exception.custom;

import org.example.educheck.global.common.exception.custom.common.GlobalException;

public class TransientMessageException extends RuntimeException {
    public TransientMessageException(String message, GlobalException e) {
        super(message, e);
    }
}
