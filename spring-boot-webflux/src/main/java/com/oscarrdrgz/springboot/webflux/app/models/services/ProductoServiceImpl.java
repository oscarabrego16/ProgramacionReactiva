package com.oscarrdrgz.springboot.webflux.app.models.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oscarrdrgz.springboot.webflux.app.models.dao.CategoriaDao;
import com.oscarrdrgz.springboot.webflux.app.models.dao.ProductoDao;
import com.oscarrdrgz.springboot.webflux.app.models.documents.Categoria;
import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


//podriamos implementar la logica de negocios por medio de muchos DAOS, y asi se desacopla del controller
@Service
public class ProductoServiceImpl implements ProductoService{
	@Autowired
	private ProductoDao productoDao;
	@Autowired
	private CategoriaDao categoriaDao;

	@Override
	public Flux<Producto> findAll() {
		return productoDao.findAll();
	}

	@Override
	public Mono<Producto> findById(String id) {
		return productoDao.findById(id);
	}

	@Override
	public Mono<Producto> save(Producto producto) {
		return productoDao.save(producto);
	}

	@Override
	public Mono<Void> delete(Producto producto) {
		return productoDao.delete(producto);
	}

	@Override
	public Flux<Producto> findAllConNombreEnUpperCase() {
		return productoDao.findAll().map(producto->{
			producto.setNombre(producto.getNombre().toUpperCase());
			return producto;
		});
		
	}

	@Override
	public Flux<Producto> findAllConNombreEnUpperCaseConRepeat() {

			return findAllConNombreEnUpperCase().repeat(5000);
		
	}

	@Override
	public Flux<Categoria> findAllCategoria() {
		return categoriaDao.findAll();
	}

	@Override
	public Mono<Categoria> findCategoriaById(String id) {
		return categoriaDao.findById(id);
	}

	@Override
	public Mono<Categoria> saveCategoria(Categoria categoria) {
		return categoriaDao.save(categoria);
	}

}
