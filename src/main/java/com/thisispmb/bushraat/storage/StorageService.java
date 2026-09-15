package com.thisispmb.bushraat.storage;

import jakarta.servlet.http.Part;

public interface StorageService {
    String storeBook(Part file) throws Exception;

    String storeCover(Part file) throws Exception;

    StorageObject open(String objectKey) throws Exception;

    StorageObject openRange(String objectKey, long start, long length) throws Exception;

    long size(String objectKey) throws Exception;

    void delete(String objectKey) throws Exception;

    final class UnsupportedMediaTypeException extends Exception {
        public UnsupportedMediaTypeException(String message) {
            super(message);
        }
    }

    static StorageService create() {
        return Holder.INSTANCE;
    }

    final class Holder {
        private static final StorageService INSTANCE = new R2StorageService();

        private Holder() {
        }
    }
}
