/* (C)2023 Tim Holdaway */
package com.timholdaway;

import java.util.function.Function;

public class Result<T> {
    final String errorMessage;
    final T result;

    private String originalUrl;

    public Result(T result, String originalUrl) {
        this.result = result;
        errorMessage = null;
        this.originalUrl = originalUrl;
    }

    public Result(String message, String originalUrl) {
        result = null;
        errorMessage = message;
        this.originalUrl = originalUrl;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public boolean isOk() {
        return result != null;
    }

    public T extractOk() {
        return isOk() ? result : null;
    }

    public String extractError() {
        return isError() ? originalUrl + ": " + errorMessage : null;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    private Result<T> withOriginalUrl(String url) {
        originalUrl = url;
        return this;
    }

    public <S> Result<S> mapResult(Function<T, Result<S>> mapperFn) {
        return isOk()
                ? mapperFn.apply(result).withOriginalUrl(originalUrl)
                : error(errorMessage, originalUrl);
    }

    public static <S> Result<S> ok(S body, String originalUrl) {
        return new Result<S>(body, originalUrl);
    }

    public static <S> Result<S> ok(S body) {
        return new Result<S>(body, "no original url provided");
    }

    public static <S> Result<S> error(String message, String originalUrl) {
        return new Result<S>(message, originalUrl);
    }

    public static <S> Result<S> error(String message) {
        return new Result<S>(message, "no original url provided");
    }
}
