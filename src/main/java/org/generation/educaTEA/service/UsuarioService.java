package org.generation.educaTEA.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.generation.educaTEA.model.UserLogin;
import org.generation.educaTEA.model.Usuario;
import org.generation.educaTEA.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;
	

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
		usuario.setSenha(criptografarSenha(usuario.getSenha()));
		return Optional.of(usuarioRepository.save(usuario));
	}

	public ResponseEntity<Usuario> atualizarUsuario(Usuario usuario) {
		if (usuarioRepository.findById(usuario.getId()).isPresent()) {
			Optional<Usuario> buscaUsuario = usuarioRepository.findByUsuario(usuario.getUsuario());
			if (buscaUsuario.isPresent()) {
				if (buscaUsuario.get().getId()!= usuario.getId())
					throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe!", null);
			}
			usuario.setSenha(criptografarSenha(usuario.getSenha()));
			return ResponseEntity.status(200).body(usuarioRepository.save(usuario));
		}
		throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado!", null);
		
	}

	public Optional<UserLogin> logarUsuario(Optional<UserLogin> usuarioLogin) {
		Optional<Usuario> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		if (usuario.isPresent()) {
			if (compararSenhas(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setFoto(usuario.get().getFoto());
				usuarioLogin.get().setTipo(usuario.get().getTipo());
				usuarioLogin.get().setToken(generatorBasicToken(usuarioLogin.get().getUsuario(), usuarioLogin.get().getSenha()));
				usuarioLogin.get().setSenha(usuario.get().getSenha());
				return usuarioLogin;
			}
		}
		throw new ResponseStatusException(
				HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos!", null);
	}

	private String criptografarSenha(String senha) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String senhaEncoder = encoder.encode(senha);
		return senhaEncoder;
	}

	private boolean compararSenhas(String senhaDigitada, String senhaBanco) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder.matches(senhaDigitada, senhaBanco);
	}

	private String generatorBasicToken(String email, String password) {
		String structure = email + ":" + password;
		byte[] structureBase64 = Base64.encodeBase64(structure.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(structureBase64);
	}
	
}
