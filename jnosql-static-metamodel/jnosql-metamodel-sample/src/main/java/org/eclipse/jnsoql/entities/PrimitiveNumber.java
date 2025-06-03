package org.eclipse.jnsoql.entities;

import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.UUID;

@Entity
public class PrimitiveNumber {

    @Id
    private UUID uuid;


    private int integer;
    private long longNumber;
    private float floatNumber;
    private double doubleNumber;

    private byte byteNumber;

    private short shortNumber;

    private boolean booleanSample;

}
