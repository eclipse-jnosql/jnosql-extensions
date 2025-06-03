package org.eclipse.jnsoql.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public class PrimitiveNumber {

    @Id
    private UUID uuid;
    @Column
    private int integer;
    @Column
    private long longNumber;
    @Column
    private float floatNumber;
    @Column
    private double doubleNumber;
    @Column
    private byte byteNumber;

    @Column
    private short shortNumber;

    @Column
    private boolean booleanSample;

}
