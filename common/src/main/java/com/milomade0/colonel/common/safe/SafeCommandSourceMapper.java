package com.milomade0.colonel.common.safe;

@FunctionalInterface
public interface SafeCommandSourceMapper<S> {

    Object map(S source);

}
