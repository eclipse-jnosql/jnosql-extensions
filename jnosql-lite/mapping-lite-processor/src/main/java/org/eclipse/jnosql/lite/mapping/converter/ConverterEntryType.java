package org.eclipse.jnosql.lite.mapping.converter;

public class ConverterEntryType {

    private final String type;

    private final String converter;

    public ConverterEntryType(String type, String converter) {
        this.type = type;
        this.converter = converter;
    }
}
