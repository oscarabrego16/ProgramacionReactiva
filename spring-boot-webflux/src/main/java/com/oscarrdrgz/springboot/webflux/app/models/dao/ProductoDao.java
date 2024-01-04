package com.oscarrdrgz.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{

}
