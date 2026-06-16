package com.milomade0.colonel.common.build;

@FunctionalInterface
public interface CommandSourceMapper {

    Object map(Object source) throws Throwable;

}