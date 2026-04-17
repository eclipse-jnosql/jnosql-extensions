package org.eclipse.jnosql.spring.boot.autoconfigure.mongodb;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface Library extends CrudRepository<BookEntity,String> {
}
