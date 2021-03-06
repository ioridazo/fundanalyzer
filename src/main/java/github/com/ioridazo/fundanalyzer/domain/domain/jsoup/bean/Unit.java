package github.com.ioridazo.fundanalyzer.domain.domain.jsoup.bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Unit {

    THOUSANDS_OF_YEN("単位：千円", 1000),
    MILLIONS_OF_YEN("単位：百万円", 1000000),
    ;

    private final String name;

    private final int value;

    Unit(final String name, final int value) {
        this.name = name;
        this.value = value;
    }

    @JsonCreator
    public static Unit fromName(final String name) {
        return Arrays.stream(values())
                .filter(v -> v.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.valueOf(name)));
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    @JsonValue
    public int getValue() {
        return this.value;
    }
}
