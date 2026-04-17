package org.eclipse.jnosql.spring.boot.autoconfigure.oracle;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface Library extends CrudRepository<BookEntity,String> {
}
