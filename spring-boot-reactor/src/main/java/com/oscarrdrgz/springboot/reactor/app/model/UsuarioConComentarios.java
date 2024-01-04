package com.oscarrdrgz.springboot.reactor.app.model;

public class UsuarioConComentarios {
	private Usuario usuario;
	private Comentarios comentarios;
	public UsuarioConComentarios(Usuario usuario, Comentarios comentarios) {
		super();
		this.usuario = usuario;
		this.comentarios = comentarios;
	}
	@Override
	public String toString() {
		return "UsuarioConComentarios [usuario=" + usuario + ", comentarios=" + comentarios + "]";
	}
	
	
	

}
