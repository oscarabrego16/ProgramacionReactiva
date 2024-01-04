package com.oscarrdrgz.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.oscarrdrgz.springboot.webflux.app.models.documents.Categoria;

public interface CategoriaDao extends ReactiveMongoRepository<Categoria, String>{

}
