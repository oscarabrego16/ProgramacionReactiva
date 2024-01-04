package com.oscarrdrgz.springboot.reactor.app.model;

import java.util.ArrayList;
import java.util.List;

public class Comentarios {
	private List<String> comentarios;

	public Comentarios() {
		super();
		this.comentarios = new ArrayList<>();
	}

	public void addComentario(String comentario) {
		this.comentarios.add(comentario);
	}

	@Override
	public String toString() {
		return "Comentarios= "+ comentarios;
	}
	
	
	

}
