package org.eclipse.jnsoql.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

@Embeddable
public record Driver(@Column String name, @Column String licenseNumber) {
}
