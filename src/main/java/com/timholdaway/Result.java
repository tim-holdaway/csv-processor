/* (C)2023 Tim Holdaway */
package com.timholdaway;

import java.util.function.Function;

public class Result<T> {
    final String errorMessage;
    final T result;

    public Result(T result) {
        this.result = result;
        errorMessage = null;
    }

    public Result(String message) {
        result = null;
        errorMessage = message;
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
        return isError() ? errorMessage : null;
    }

    public <S> Result<S> mapResult(Function<T, Result<S>> mapperFn) {
        return isOk() ? mapperFn.apply(result) : error(errorMessage);
    }

    public static <S> Result<S> ok(S body) {
        return new Result<S>(body);
    }

    public static <S> Result<S> error(String message) {
        return new Result<S>(message);
    }
}
