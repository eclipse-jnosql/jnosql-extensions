package org.eclipse.jnsoql.entities;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
public class WishList {

    @Id
    private UUID id;

    @Column
    private List<String> products;

    @Column
    private String[] travels;

    @Column
    private Map<String, String> people;

}
