package com.oscarrdrgz.springboot.webflux.app;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.List;

import com.oscarrdrgz.springboot.webflux.app.models.documents.Categoria;
import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;
import com.oscarrdrgz.springboot.webflux.app.models.services.ProductoService;

import reactor.core.publisher.Mono;

//levantando un servidor real
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

@AutoConfigureWebTestClient //solo necesaria para el mock
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) //sin levantar un servidor
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	@Autowired
	private ProductoService service;
	
	@Value("${config.base.endpoint}")
	private String url;

	@Test
	public void listartTest() {
		client.get()
		.uri(url)
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBodyList(Producto.class)
		.consumeWith(response->{
			List<Producto> productos = response.getResponseBody();
			productos.forEach(p->{
				System.out.println(p.getNombre());
			});
			Assertions.assertThat(productos.size()>0).isTrue();
		});
		//.hasSize(5);
	}
	
	@Test
	public void verTest() throws InterruptedException {
		Producto producto = service.findByNombre("Televisor").block();

		
		client.get()
		.uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
		.accept(MediaType.APPLICATION_JSON)
		.exchange()
		.expectStatus().isOk()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto p = response.getResponseBody();
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getId().length()>0).isTrue();
			Assertions.assertThat(p.getNombre()).isEqualTo("Televisor");
		/*.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Televisor");*/
	});
	}
	
	//con el json
	@Test
	public void crearTest() {
		Categoria categoria = service.findCategoriaByNombre("Electronico").block();
		
		Producto producto= new Producto("Mesa comedor", 100.0, categoria);
		
		client.post()
		.uri(url)
		//lo que se va a recibir
		.contentType(MediaType.APPLICATION_JSON)
		//el aceptable que se debe enviar con la respuesta, o lo que se espera
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Mesa comedor")
		.jsonPath("$.categoria.nombre").isEqualTo("Electronico");
	}
	
	//Con la forma de objeto producto
	@Test
	public void crear2Test() {
		
		Categoria categoria = service.findCategoriaByNombre("Electronico").block();
		
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post()
		.uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(Producto.class)
		.consumeWith(response -> {
			Producto p = response.getResponseBody();
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
			Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Electronico");
		});
	}
	
	@Test
	public void editarTest() {
		Producto producto = service.findByNombre("Camara").block();
		Categoria categoria = service.findCategoriaByNombre("Deporte").block();
		Producto productoEditado = new Producto("Televisor deportivo", 12.99, categoria);
		
		client.put()
		.uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(productoEditado), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.id").isNotEmpty()
		.jsonPath("$.nombre").isEqualTo("Televisor deportivo")
		.jsonPath("$.categoria.nombre").isEqualTo("Deporte");
	}	

	@Test
	public void eliminarTest() {
		Producto producto = service.findByNombre("Audifonos").block();
		client.delete()
		.uri(url + "/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()// para enviar y ejecutar la peiticion
		.expectStatus().isNoContent()
		.expectBody()
		.isEmpty();
		
		client.get()
		.uri("/api/v2/productos" + "/{id}", Collections.singletonMap("id", producto.getId()))
		.exchange()// para enviar y ejecutar la peiticion
		.expectStatus().isNotFound()
		.expectBody()
		.isEmpty();
	}
	
	/* pruebas crear modificadas para el rest controller
	 * 
	 * @Test
	public void crearTest() {
		
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody()
		.jsonPath("$.producto.id").isNotEmpty()
		.jsonPath("$.producto.nombre").isEqualTo("Mesa comedor")
		.jsonPath("$.producto.categoria.nombre").isEqualTo("Muebles");
	}
 
	@Test
	public void crear2Test() {
		
		Categoria categoria = service.findCategoriaByNombre("Muebles").block();
		
		Producto producto = new Producto("Mesa comedor", 100.00, categoria);
		
		client.post().uri(url)
		.contentType(MediaType.APPLICATION_JSON)
		.accept(MediaType.APPLICATION_JSON)
		.body(Mono.just(producto), Producto.class)
		.exchange()
		.expectStatus().isCreated()
		.expectHeader().contentType(MediaType.APPLICATION_JSON)
		.expectBody(new ParameterizedTypeReference<LinkedHashMap<String, Object>>() {})
		.consumeWith(response -> {
			Object o = response.getResponseBody().get("producto");
			Producto p = new ObjectMapper().convertValue(o, Producto.class);
			Assertions.assertThat(p.getId()).isNotEmpty();
			Assertions.assertThat(p.getNombre()).isEqualTo("Mesa comedor");
			Assertions.assertThat(p.getCategoria().getNombre()).isEqualTo("Muebles");
		});
	}
	 * 
	 * 
	 * */
	
}
	
