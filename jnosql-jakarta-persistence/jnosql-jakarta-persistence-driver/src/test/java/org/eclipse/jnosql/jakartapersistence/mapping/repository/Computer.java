package org.eclipse.jnosql.jakartapersistence.mapping.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.Objects;

import static jakarta.persistence.GenerationType.AUTO;

@Entity
public class Computer {

    @Id
    @GeneratedValue(strategy = AUTO)
    private long id;

    @Column
    private String model;

    @Column
    private long age;


    Computer() {
    }

    public long getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public long getAge() {
        return age;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Computer computer)) {
            return false;
        }
        return id == computer.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Computer{" +
                "id=" + id +
                ", model='" + model + '\'' +
                ", age=" + age +
                '}';
    }
}
