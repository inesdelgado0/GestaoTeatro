package com.teatro;

import com.teatro.entities.Evento;
import com.teatro.services.EventoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TeatroApplication {

	public static void main(String[] args) {
		SpringApplication.run(TeatroApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(EventoService eventoService) {
		return args -> {
			System.out.println("\n=======================================");
			System.out.println("   DEMONSTRAÇÃO DA CAMADA BLL - TEATRO ");
			System.out.println("=======================================\n");

			// 1. CRIAR UM EVENTO (Exemplo de utilização do Builder com as tuas colunas)
			Evento novoEvento = Evento.builder()
					.titulo("O Fantasma da Ópera")
					.descricao("Um clássico do teatro musical no Porto.")
					.duracaomin(150)
					.classificacaoetaria("M/12")
					.genero("Musical")
					.build();

			// 2. UTILIZAR O MÉTODO DA BLL (Salvar)
			try {
				Evento guardado = eventoService.criarEvento(novoEvento);
				System.out.println("[BLL] Sucesso ao criar evento: " + guardado.getTitulo());
			} catch (Exception e) {
				System.err.println("[BLL] Erro de validação: " + e.getMessage());
			}

			// 3. UTILIZAR O MÉTODO DA BLL (Listar)
			System.out.println("\n[BLL] A recuperar lista de eventos da Base de Dados:");
			eventoService.listarTodos().forEach(ev -> {
				System.out.println(" >> [" + ev.getId() + "] " + ev.getTitulo() + " (" + ev.getGenero() + ")");
			});

			System.out.println("\n=======================================");
		};
	}
}