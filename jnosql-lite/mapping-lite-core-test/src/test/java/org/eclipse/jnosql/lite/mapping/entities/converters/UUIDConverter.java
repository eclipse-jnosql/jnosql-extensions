package org.eclipse.jnosql.lite.mapping.entities.converters;

import jakarta.nosql.AttributeConverter;
import jakarta.nosql.Converter;

import java.util.UUID;


@Converter(autoApply = false)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    @Override
    public String convertToDatabaseColumn(UUID attribute) {
        return attribute.toString();
    }

    @Override
    public UUID convertToEntityAttribute(String dbData) {
        return UUID.fromString(dbData);
    }
}
