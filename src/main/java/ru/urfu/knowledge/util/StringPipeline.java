package ru.urfu.knowledge.util;

import java.util.function.Function;

public class StringPipeline {
    private String value;

    private StringPipeline(String value) {
        this.value = value;
    }

    public static StringPipeline of(String value) {
        return new StringPipeline(value);
    }

    public StringPipeline then(Function<String, String> transform) {
        this.value = transform.apply(this.value);
        return this;
    }

    public String build() {
        return this.value;
    }
}
