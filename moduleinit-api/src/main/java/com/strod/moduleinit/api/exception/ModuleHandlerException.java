package com.strod.moduleinit.api.exception;

/**
 * 主流程的处理异常
 *
 */
public class ModuleHandlerException extends RuntimeException {
    public ModuleHandlerException(String detailMessage) {
        super(detailMessage);
    }
}
