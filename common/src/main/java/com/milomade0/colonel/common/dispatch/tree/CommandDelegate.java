package com.milomade0.colonel.common.dispatch.tree;

import com.milomade0.colonel.common.build.CommandContext;

public interface CommandDelegate extends Runnable {

    CommandContext context();

}
