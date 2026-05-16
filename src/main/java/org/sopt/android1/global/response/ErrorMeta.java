package org.sopt.android1.global.response;

import java.time.Instant;

public record ErrorMeta(String path, String timestamp) {

    public static ErrorMeta of(String path) {
        return new ErrorMeta(path, Instant.now().toString());
    }
}
