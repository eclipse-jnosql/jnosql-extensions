package org.eclipse.jnosql.lite.mapping.entities.converters;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public record NoSQLDatabase(@Id UUID id, @Column String name) {
}
