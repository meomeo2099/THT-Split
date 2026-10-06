package com.tht.split.core;

import java.io.IOException;

/** Keeps the connection stage/endpoint visible while retaining the original cause. */
public final class ConnectionFailure extends IOException {
    public ConnectionFailure(String context, Throwable cause) { super(context, cause); }
}
