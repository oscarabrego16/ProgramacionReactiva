package com.oscarrdrgz.springboot.webflux.app.controllers;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;

import com.oscarrdrgz.springboot.webflux.app.models.dao.ProductoDao;
import com.oscarrdrgz.springboot.webflux.app.models.documents.Categoria;
import com.oscarrdrgz.springboot.webflux.app.models.documents.Producto;
import com.oscarrdrgz.springboot.webflux.app.models.services.ProductoService;


import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SessionAttributes("producto")
@Controller
public class ProductoController {
	
	@Autowired
	private ProductoService productoService;
	
	@Value("${config.uploads.path}")
	private String path;
	
	private static final Logger logger= LoggerFactory.getLogger(ProductoController.class);
	
	@ModelAttribute("categorias")
	public Flux<Categoria> categorias(){
		return productoService.findAllCategoria();
	}
	
	@GetMapping("/uploads/img/{nombreFoto:.+}")
	public Mono<ResponseEntity<Resource>> verFoto(@PathVariable String nombreFoto) throws MalformedURLException{
		System.out.println(nombreFoto);
		System.out.println(path);
		Path ruta = Paths.get(path).resolve(nombreFoto).toAbsolutePath();
		System.out.println(ruta);
		Resource imagen = new UrlResource(ruta.toUri());
		
		return Mono.just(
				ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagen.getFilename()+"\"")
				.body(imagen)
);
	}
	
	
	@GetMapping("ver/{id}")
	public Mono<String> ver(Model model, @PathVariable String id){
		return productoService.findById(id)
				.doOnNext(
						p->{
							model.addAttribute("producto", p);
							model.addAttribute("titulo", "Detalle de producto");
						}
						).switchIfEmpty(Mono.just(new Producto()))
				.flatMap(p->{
					if(p.getId()==null) {
						return Mono.error(new InterruptedException("No existe el producto"));
					}
					return Mono.just(p);
				}).then(Mono.just("ver"))
				.onErrorResume(ex-> Mono.just("redirect:/listar?error=no+existe+el+producto"));
	}
	
	
	@GetMapping({"listar", "/"})
	public Mono<String> listar(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreEnUpperCase();
		
		productos.subscribe(prod-> logger.info(prod.getNombre()));
		model.addAttribute("productos",productos);
		model.addAttribute("titulo", "Listado de productos");
		return Mono.just("listar");
	}
	
	@GetMapping("/form")
	public Mono<String> crear(Model model){
		//se crea este nuevo producto para luego llenarlo y guardarlo
		model.addAttribute("producto", new Producto());
		model.addAttribute("titulo", "Formulario del producto");
		model.addAttribute("boton", "Crear");
		return Mono.just("form");
	}
	
	@GetMapping("/form/{id}")
	public Mono<String> editar(@PathVariable(name="id") String idProducto, Model model){
		Mono<Producto> produMono = productoService.findById(idProducto).doOnNext(p->{
			logger.info("Producto: "+ p.getNombre());			
		}).defaultIfEmpty(new Producto());
		
		model.addAttribute("titulo", "Editar Producto");
		model.addAttribute("producto", produMono);
		model.addAttribute("boton", "Editar");
		return Mono.just("form");
	}
	
	@GetMapping("/form-v2/{id}")
	public Mono<String> editarV2(@PathVariable(name="id") String idProducto, Model model){
		return productoService.findById(idProducto).doOnNext(p->{

			model.addAttribute("titulo", "Editar Producto");
			model.addAttribute("producto", p);
			model.addAttribute("boton", "Editar");
			
			logger.info("Producto: "+ p.getNombre());			
		}).defaultIfEmpty(new Producto())
				.flatMap(p->{
					if(p.getId() ==null) {
						return Mono.error(new InterruptedException("No existe el producto"));
						
					}
					return Mono.just(p);
				})
				.then(Mono.just("form"))
				.onErrorResume(ex-> Mono.just("redirect:/listar?error=no+existe+el+producto"));
		
	}
	
	@PostMapping("/form")
	public Mono<String> guardar(@Valid Producto producto, BindingResult result, Model model, @RequestPart(name="file") FilePart file,SessionStatus status){
		if(result.hasErrors()) {
			model.addAttribute("titulo", "Errores en formulario producto");
			model.addAttribute("boton", "Guardar");
			return Mono.just("form");
		}else {
			status.setComplete();
			
			
			
			Mono<Categoria> categoria = productoService.findCategoriaById(producto.getCategoria().getId());
			return categoria.flatMap(c->{
				
				if(producto.getCreatedAt()== null) {
					producto.setCreatedAt(new Date());
				} 
				
				if(!file.filename().isEmpty()) {
					//producto.setFoto(UUID.randomUUID().toString()+" - " + file.filename()
					producto.setFoto(file.filename()
							.replace(" ", "")
							.replace(":", "")
							.replace("\\", "")
							
							);
				}
				
				producto.setCategoria(c);
				return productoService.save(producto);
			}).doOnNext(p->{
				logger.info("categoria asignada: "+p.getCategoria()+" Id: "+ p.getCategoria().getId());
				logger.info("Producto almacenado: "+p.getNombre()+" Id: "+ p.getId());
			})
					.flatMap(p->{
						if(!file.filename().isEmpty()) {
							return file.transferTo(new File(path+ p.getFoto()));
						}
						return Mono.empty();
						
					})
					.thenReturn("redirect:/listar?success=producto+guardado+con+exito");
		}
		
	}
	
	@GetMapping("eliminar/{id}")
	public Mono<String> eliminar(@PathVariable String id){
		return productoService.findById(id)
				.defaultIfEmpty(new Producto())
				.flatMap(p->{
					if(p.getId() ==null) {
						return Mono.error(new InterruptedException("No existe el producto a eliminar"));
						
					}
					return Mono.just(p);
				})
				.flatMap(p->{
					logger.info("Eliminando producto: "+ p.getNombre()+ " "+ p.getId());
			return productoService.delete(p);
		}).then(Mono.just("redirect:/listar?success=producto+eliminado+con+exito"))
		.onErrorResume(ex-> Mono.just("redirect:/listar?error=no+existe+el+producto+a+eliminar"));
	}
	
	//Una de las formas mas eficientes de manejar contrapresion
	//asi el buffer maneja cantidad de elementos y no por bytes
	//datadriver
	@GetMapping("listar-datadriver")
	public String listarDataDriver(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreEnUpperCase()
				.delayElements(Duration.ofSeconds(1));
		
		productos.subscribe(prod-> logger.info(prod.getNombre()));
		model.addAttribute("productos",new ReactiveDataDriverContextVariable(productos, 2));
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	
	//chunker, chunk pedazos de 
	@GetMapping("/listar-full")
	public String listarFull(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreEnUpperCaseConRepeat();
		
		model.addAttribute("productos",productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar";
	}
	@GetMapping("/listar-chunked")
	public String listarChunked(Model model) {
		Flux<Producto> productos = productoService.findAllConNombreEnUpperCaseConRepeat();
		
		model.addAttribute("productos",productos);
		model.addAttribute("titulo", "Listado de productos");
		return "listar-chunked";
	}
	
}
