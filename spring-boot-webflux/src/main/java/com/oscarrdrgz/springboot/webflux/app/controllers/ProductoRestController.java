package com.oscarrdrgz.springboot.webflux.app.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;
import com.oscarrdrgz.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/productos")
public class ProductoRestController {
	@Autowired
	private ProductoService productoService;
	
	private static final Logger logger= LoggerFactory.getLogger(ProductoController.class);
	
	@GetMapping()
	public Flux<Producto> index(){
		Flux<Producto> productos = productoService.findAll();
		return productos;
	}

	@GetMapping("/{id}")
	public Mono<Producto> show(@PathVariable String id){
		//Mono<Producto> producto = productoDao.findById(id);
		Flux<Producto> productos = productoService.findAll();
		Mono<Producto> producto = productos.filter(p-> p.getId().equals(id))
									.next()
									.doOnNext(prod-> logger.info(prod.getNombre()));
		 
		return producto;
	}
}
