/*
 *   Copyright (c) 2023 Contributors to the Eclipse Foundation
 *    All rights reserved. This program and the accompanying materials
 *    are made available under the terms of the Eclipse Public License v1.0
 *    and Apache License v2.0 which accompanies this distribution.
 *    The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *    and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *    You may elect to redistribute this code under either of these licenses.
 *
 *    Contributors:
 *
 *    Otavio Santana
 */
package org.eclipse.jnosql.lite.mapping.entities;



import jakarta.nosql.Column;
import jakarta.nosql.Entity;

import java.util.List;
import java.util.Map;


@Entity
public class Actor extends Person {

    @Column
    private Map<String, String> movieCharacter;

    @Column
    private Map<String, Integer> movieRating;


    Actor() {
    }

    public Map<String, String> getMovieCharacter() {
        return movieCharacter;
    }

    public Map<String, Integer> getMovieRating() {
        return movieRating;
    }

    public void setMovieCharacter(Map<String, String> movieCharacter) {
        this.movieCharacter = movieCharacter;
    }

    public void setMovieRating(Map<String, Integer> movieRating) {
        this.movieRating = movieRating;
    }
}
