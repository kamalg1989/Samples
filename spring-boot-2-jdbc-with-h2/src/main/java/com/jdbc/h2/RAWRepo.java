package com.jdbc.h2;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;
@RepositoryRestResource
@Transactional
public interface RAWRepo extends CrudRepository<RawPOJO, Long>{

}
