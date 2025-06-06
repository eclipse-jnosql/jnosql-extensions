/*
 *  Copyright (c) 2025 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */
package org.eclipse.jnsoql.entities;

import org.junit.jupiter.api.Test;
import org.assertj.core.api.SoftAssertions;

import java.util.UUID;
class SoccerTeamTest {


    @Test
    void shouldCreateIdAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_SoccerTeam.id.name()).isEqualTo("_id");
            soft.assertThat(_SoccerTeam.id.declaringType()).isEqualTo(SoccerTeam.class);
            soft.assertThat(_SoccerTeam.id.attributeType()).isEqualTo(String.class);
        });
    }

    @Test
    void shouldCreateNameAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_SoccerTeam.players_name.name()).isEqualTo("players.name");
            soft.assertThat(_SoccerTeam.players_name.declaringType()).isEqualTo(SoccerPlayer.class);
            soft.assertThat(_SoccerTeam.players_name.attributeType()).isEqualTo(String.class);
        });
    }

    @Test
    void shouldCreateScoreAttribute() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_SoccerTeam.players_score.name()).isEqualTo("players.score");
            soft.assertThat(_SoccerTeam.players_score.declaringType()).isEqualTo(SoccerPlayer.class);
            soft.assertThat(_SoccerTeam.players_score.attributeType()).isEqualTo(int.class);
        });
    }

    @Test
    void shouldCreatePlayers() {
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(_SoccerTeam.players.name()).isEqualTo("players");
            soft.assertThat(_SoccerTeam.players.declaringType()).isEqualTo(SoccerTeam.class);
            soft.assertThat(_SoccerTeam.players.attributeType()).isEqualTo(SoccerPlayer.class);
        });
    }
}