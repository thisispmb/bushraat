package com.thisispmb.bushraat.storage;

import java.io.IOException;
import java.io.InputStream;

public record StorageObject(
        InputStream stream, long contentLength, String contentType
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
