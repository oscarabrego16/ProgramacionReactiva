package com.oscarrdrgz.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.oscarrdrgz.springboot.webflux.app.models.documents.Categoria;
import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;
import com.oscarrdrgz.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Flux;


@EnableDiscoveryClient
@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner{
	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);
	@Autowired
	private ReactiveMongoTemplate mongoTemplate;
	@Autowired
	private ProductoService service;
	

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {

		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();
		
		Categoria electronico = new Categoria("Electronico");
		Categoria deporte= new Categoria("Deporte");
		Categoria informatico= new Categoria("Informatico");
		Categoria muebles= new Categoria("muebles");
		
		Flux.just(electronico, deporte, informatico, muebles)
		.flatMap(service::saveCategoria)
		.doOnNext(c->{
			log.info("Categoria creada: "+c.getNombre() +" Id: "+c.getId());
		}).thenMany(
				Flux.just(new Producto("Televisor",114.99, electronico),
						new Producto("Camara",499.99, electronico),
						new Producto("Mesa",899.99, muebles),
						new Producto("Silla",99.99, muebles),
						new Producto("Audifonos",999.99,informatico))
				.flatMap(producto ->{
					producto.setCreatedAt(new Date());
					return service.save(producto);
					}))
		.subscribe(producto -> log.info("Insert: "+producto.getId()+" "+ producto.getNombre()));
	}
}
