package com.oscarrdrgz.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.oscarrdrgz.springboot.reactor.app.model.Comentarios;
import com.oscarrdrgz.springboot.reactor.app.model.Usuario;
import com.oscarrdrgz.springboot.reactor.app.model.UsuarioConComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner{

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);
	
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		//ejemploIterable();
		//ejemploFlatMap();
		//ejemploToString();
		//ejemploToCollectList();
		//ejemploUsuarioComentariosFlatMap();
		//ejemploUsuarioComentariosZipWith();
		//ejemploUsuarioComentariosZipWithForma2();
		//ejemploZipWithRangos();
		//ejemploInterval();
		//ejemploDelayElements();
		//ejemploIntervalInfinito();
		//ejemploIntervalDesdeCreate();
		ejemploContrapresion();
		
	}
	
	public void ejemploContrapresion() {
		Flux.range(0, 10)
		.log()
		.subscribe(new Subscriber<Integer>() {
			
			private Subscription s;
			private Integer limite=5;
			private Integer consumido= 0;

			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				s.request(limite);
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumido++;
				if(consumido ==limite) {
					consumido=0;
					s.request(limite);
				}
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});
		
		
	}
	
	
	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter->{
			Timer timer = new Timer();
			timer.schedule((
					new TimerTask() {
						private Integer contador= 0;
						
						@Override
						public void run() {
							emitter.next(++contador);
							if(contador==10) {
								timer.cancel();
								emitter.complete();
							}
							if(contador ==5) {
								timer.cancel();
								emitter.error(new InterruptedException("Error, detenido en 5"));
							}
							
						}
					}
					),  1000, 1000);
		})
		//.doOnNext(next-> log.info(next.toString()))
		//.doOnComplete(()-> log.info("Se ha terminado"))
		.subscribe(next -> log.info(next.toString()),
				error-> log.error(error.getMessage()),
				()-> log.info("Hemos terminado"));
	} 
	
	public void ejemploIntervalInfinito() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		.doOnTerminate(()-> latch.countDown())
		.flatMap(i-> {
			if(i>=5) {
				return Flux.error(new InterruptedException("Solo hasta el 5."));
			}
			return Flux.just(i);
		})
		.map(i-> "Hola "+i)
		//.doOnNext(s-> log.info(s))
		.retry(2)
		.subscribe(s-> log.info(s), e-> log.error(e.getMessage()));
		
		latch.await();
		
	}
	
	public void ejemploDelayElements() throws InterruptedException {
		Flux<Integer> rango = Flux.range(0, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i-> log.info(i.toString()));
		rango.subscribe();
		
		Thread.sleep(13000);
		
	}
	
	public void ejemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso, (ra, re)-> ra)
		.doOnNext(i-> log.info(i.toString()))
		//.subscribe();
		.blockLast() //suscribe pero bloqueando, no es recomendable de msotrar, es didactico
		;
	}
	
	public void ejemploZipWithRangos() {
		Flux.just(1,2,3,4)
		.map(i-> (i*2))
		.zipWith(Flux.range(0, 4), (uno, dos)-> String.format("Primer Flux %d, Segundo Flux: %d", uno, dos))
		.subscribe(texto-> log.info(texto))
		;
	}
	
	
	public void ejemploUsuarioComentariosZipWithForma2() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()->{
			return new Usuario("John", "Doe");
		});
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios= new Comentarios();
			comentarios.addComentario("Hola");
			comentarios.addComentario("Que dia");
			comentarios.addComentario("Como esta");
			return comentarios;
		});
		
		Mono<UsuarioConComentarios>userConComment=  usuarioMono
				.zipWith(comentariosUsuarioMono)
				.map(tuple -> { //el map hace el stream de la dupla porque ya se hizo el zip
					Usuario u = tuple.getT1();
					Comentarios com= tuple.getT2();
					return new UsuarioConComentarios(u, com);
				});
		userConComment.subscribe(uc -> log.info(uc.toString()));
		
	}
	

	public void ejemploUsuarioComentariosZipWith() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()->{
			return new Usuario("John", "Doe");
		});
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios= new Comentarios();
			comentarios.addComentario("Hola");
			comentarios.addComentario("Que dia");
			comentarios.addComentario("Como esta");
			return comentarios;
		});
		
		Mono<UsuarioConComentarios>userConComment=  usuarioMono.zipWith(comentariosUsuarioMono, (usuario, comentariosUsuario)-> new UsuarioConComentarios(usuario, comentariosUsuario));
		userConComment.subscribe(uc -> log.info(uc.toString()));
		
	}
	
	public void ejemploUsuarioComentariosFlatMap() {
		Mono<Usuario> usuarioMono = Mono.fromCallable(()->{
			return new Usuario("John", "Doe");
		});
		Mono<Comentarios> comentariosUsuarioMono = Mono.fromCallable(()->{
			Comentarios comentarios= new Comentarios();
			comentarios.addComentario("Hola");
			comentarios.addComentario("Que dia");
			comentarios.addComentario("Como esta");
			return comentarios;
		});
		
		usuarioMono.flatMap(us-> comentariosUsuarioMono.map(c-> new UsuarioConComentarios(us,c)))
		.subscribe(uc -> log.info(uc.toString()));
		
	}
	
	
	
	public void ejemploToCollectList() throws Exception{
		List<Usuario> usuariosAdd = new ArrayList<>();
		usuariosAdd.add(new Usuario("Ejemplo","A"));
		usuariosAdd.add(new Usuario("Ejemplo","B"));
		usuariosAdd.add(new Usuario("Ejemplo","C"));
		usuariosAdd.add(new Usuario("Ejemplo","D"));
		usuariosAdd.add(new Usuario("Bruce","Alner"));
		usuariosAdd.add(new Usuario("Bruce","Belner"));
		
		Flux.fromIterable(usuariosAdd)
		.collectList()
		//se convirtio a un mono
		//un solo objeto
		.subscribe(lista-> log.info(lista.toString()));
		
	}
	
	public void ejemploToString() throws Exception{
		List<Usuario> usuariosAdd = new ArrayList<>();
		usuariosAdd.add(new Usuario("Ejemplo","A"));
		usuariosAdd.add(new Usuario("Ejemplo","B"));
		usuariosAdd.add(new Usuario("Ejemplo","C"));
		usuariosAdd.add(new Usuario("Ejemplo","D"));
		usuariosAdd.add(new Usuario("Bruce","Alner"));
		usuariosAdd.add(new Usuario("Bruce","Belner"));
		
		Flux.fromIterable(usuariosAdd)
		.map(usuario -> usuario.getNombre().toUpperCase().concat(" ").concat(usuario.getApellido()))
		.flatMap(nombre -> {
			if(nombre.contains("bruce".toUpperCase())){
				return Mono.just(nombre);
			}else {
				return Mono.empty();
			}
		})
		.map(nombre ->{
			return nombre.toLowerCase();
		})
		.subscribe(u-> log.info(u.toString()));
		
	}
	
	public void ejemploFlatMap() throws Exception{
	//////Crear un Flux (observable a partir de una lista)
			List<String> usuariosAdd = new ArrayList<>();
			usuariosAdd.add("Ejemplo A");
			usuariosAdd.add("Ejemplo B");
			usuariosAdd.add("Ejemplo C");
			usuariosAdd.add("Ejemplo D");
			usuariosAdd.add("Bruce Alner");
			usuariosAdd.add("Bruce Belner");
			
			/*
			Flux.fromIterable(usuariosAdd)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.map(usuario ->{
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				})
				.subscribe(u-> log.info(u.toString()));
			*/
			
			
			Flux.fromIterable(usuariosAdd)
			.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
			.flatMap(usuario -> {
				if(usuario.getNombre().equalsIgnoreCase("bruce")){
					return Mono.just(usuario);
				}else {
					return Mono.empty();
				}
			})
			.map(usuario ->{
				String nombre = usuario.getNombre().toLowerCase();
				usuario.setNombre(nombre);
				return usuario;
			})
			.subscribe(u-> log.info(u.toString()));
			
		
	}
	
	
	public void ejemploIterable() throws Exception{

		//flux es un plubisher
		//cada vez que flux recibe un elemento entonces informa
		/**Flux<String> nombres = Flux.just("Oscar","Alejandro","Rodriguez")
				.doOnNext(elemento -> {System.out.println(elemento);});
		**/
		//por referencia de metodo
		Flux<Usuario> nombres = Flux.just("Oscar Abrego","Alejandro Rivera","Daniel Rodriguez", "Bruce A", "Bruce B")
				.map(nombre-> 
					new Usuario(nombre.split(" ")[0].toUpperCase(), null)
				)
				.filter(usuario-> usuario.getNombre().toLowerCase().equals("bruce"))
				.doOnNext(usuario->{
					if(usuario == null) {
						throw new RuntimeException("Nombres no pueden estar vacios");
					}{
						System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
					}
						
				})
				.map(usuario-> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}
			)
				;
		
		//cuando nos suscribimos puede consumir el flujo, pero ademas de consumir
		//tambien puede manejar cualqueir tipo de error
		//se ejecuta la tarea, imprime por medio del log
		//se sobreescribe la funcion
		nombres.subscribe(e-> log.info(e.getNombre().concat(" ").concat(e.getApellido())),
				error -> log.error(error.getMessage()),
				new Runnable() {
					
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable.");
					}
				});
		
		
		
		//////Crear un Flux (observable a partir de una lista)
		List<String> usuariosAdd = new ArrayList<>();
		usuariosAdd.add("Ejemplo A");
		usuariosAdd.add("Ejemplo B");
		usuariosAdd.add("Ejemplo C");
		usuariosAdd.add("Ejemplo D");
		usuariosAdd.add("Bruce A");
		usuariosAdd.add("Bruce B");
		
		Flux<String> nombresPorList = Flux.fromIterable(usuariosAdd);
		
		nombresPorList.subscribe(e-> log.info(e),
				error -> log.error(error.getMessage()),
				new Runnable() {
					
					@Override
					public void run() {
						log.info("Ha finalizado la ejecucion del observable.");
					}
				});
		
	}

}
