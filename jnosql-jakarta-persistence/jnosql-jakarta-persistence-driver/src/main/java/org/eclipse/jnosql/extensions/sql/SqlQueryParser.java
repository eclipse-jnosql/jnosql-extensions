/*
 *  Copyright (c) 2026 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.extensions.sql;

import org.eclipse.jnosql.communication.QueryException;
import org.eclipse.jnosql.communication.query.data.QueryType;
import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.eclipse.jnosql.communication.semistructured.CommunicationObserverParser;
import org.eclipse.jnosql.communication.semistructured.CommunicationPreparedStatement;
import org.eclipse.jnosql.communication.semistructured.DatabaseManager;
import org.eclipse.jnosql.communication.semistructured.DeleteQueryParser;
import org.eclipse.jnosql.communication.semistructured.QueryParser;
import org.eclipse.jnosql.communication.semistructured.SelectQueryParser;
import org.eclipse.jnosql.communication.semistructured.UpdateQueryParser;

import java.util.Objects;
import java.util.stream.Stream;

final class SqlQueryParser {

 static final QueryParser INSTANCE = new QueryParser();

 private final SelectQueryParser select = new SelectQueryParser();
 private final DeleteQueryParser delete = new DeleteQueryParser();
 private final UpdateQueryParser update = new UpdateQueryParser();


 public <T> Stream<CommunicationEntity> query(String query, String entity, DatabaseManager manager) {
  validation(query, manager);
  var command = QueryType.parse(query);
  return switch (command) {
   case DELETE -> delete.query(query, manager, CommunicationObserverParser.EMPTY);
   case UPDATE -> update.query(query, manager, CommunicationObserverParser.EMPTY);
   default -> select.query(query, entity, manager, CommunicationObserverParser.EMPTY);
  };
 }

 /**
  * Executes a query and returns a {@link CommunicationPreparedStatement}, when the operations are <b>insert</b>, <b>update</b> and <b>select</b>
  * command it will return the result of the operation when the command is <b>delete</b> it will return an empty collection.
  *
  * @param query    the query as {@link String}
  * @param manager  the manager
  * @return a {@link CommunicationPreparedStatement} instance
  * @throws NullPointerException     when there is parameter null
  * @throws IllegalArgumentException when the query has value parameters
  * @throws QueryException           when there is error in the syntax
  */
 public CommunicationPreparedStatement prepare(String query, String entity, DatabaseManager manager) {
  validation(query, manager);
  var command = QueryType.parse(query);
  return switch (command) {
   case DELETE -> delete.prepare(query, manager, CommunicationObserverParser.EMPTY);
   case UPDATE -> update.prepare(query, manager, CommunicationObserverParser.EMPTY);
   default -> select.prepare(query, entity, manager, CommunicationObserverParser.EMPTY);
  };
 }

 private void validation(String query, DatabaseManager manager) {
  Objects.requireNonNull(query, "query is required");
  Objects.requireNonNull(manager, "manager is required");
 }
}
