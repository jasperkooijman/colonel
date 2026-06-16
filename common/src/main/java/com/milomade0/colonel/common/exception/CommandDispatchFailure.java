package com.milomade0.colonel.common.exception;

public class CommandDispatchFailure extends CommandFailure {

    private String path;

    CommandDispatchFailure(Throwable cause) {
        super(cause);
    }

    public CommandDispatchFailure withPath(String path) {
        this.path = path;
        return this;
    }

    public String path() {
        return path;
    }
}
