package com.oscarrdrgz.springboot.webflux.client.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicate;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.oscarrdrgz.springboot.webflux.client.app.handler.ProductoHandler;

@Configuration
public class RouterConfig {
	@Bean
	public RouterFunction<ServerResponse> rutas(ProductoHandler handler) {
		return route(GET("/api/client"), handler::listar)
				.andRoute(GET("/api/client/{id}"), handler::verDetalle)
				.andRoute(POST("/api/client/"), handler::crear)
				.andRoute(PUT("/api/client/{id}"), handler::editar)
				.andRoute(DELETE("/api/client/{id}"), handler::eliminar)
				.andRoute(POST("/api/client/upload/{id}"), handler::upload)
				;
	}
}
