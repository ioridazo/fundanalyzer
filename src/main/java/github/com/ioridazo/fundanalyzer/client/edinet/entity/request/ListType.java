package github.com.ioridazo.fundanalyzer.client.edinet.entity.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ListType {
    DEFAULT("1"),
    GET_LIST("2");

    private final String code;

    ListType(final String code) {
        this.code = code;
    }

    @JsonCreator
    public static ListType fromValue(final String code) {
        return Arrays.stream(values())
                .filter(v -> v.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(code)));
    }

    @JsonValue
    public String toValue() {
        return this.code;
    }

    @Override
    public String toString() {
        return String.format("ListType[code = %s]", this.code);
    }

}
