package com.milomade0.colonel.common.safe.parameter;

import com.milomade0.colonel.common.build.CommandContext;
import com.milomade0.colonel.common.build.CommandParameter;

public class StringParameter extends CommandParameter {

    public StringParameter(String name) {
        super(name);
    }

    //

    @Override
    public Object parse(CommandContext context, String input) {
        return input;
    }
}
