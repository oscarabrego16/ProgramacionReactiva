package com.oscarrdrgz.springboot.webflux.app.models.dao;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Mono;

public interface ProductoDao extends ReactiveMongoRepository<Producto, String>{
	public Mono<Producto> findByNombre(String nombre);
}
