package com.smartHome.webapp.repositories;

import com.smartHome.webapp.entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.CrossOrigin;

//TODO @kosikova mozem toto zmazat? pouzivame to este v angulary?
@Repository
public interface UserRepository extends CrudRepository<User, Long>{}