package br.ce.wcaquino.servicos;

import static br.ce.wcaquino.utils.DataUtils.adicionarDias;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;

public class LocacaoService {

	private LocacaoDAO dao;

	public Locacao alugarFilme(Usuario usuario, List<Filme> filmeLista) throws FilmeSemEstoqueException, LocadoraException {
		if(usuario == null) {
			throw new LocadoraException("Usuario vazio");
		}

		if(filmeLista == null || filmeLista.isEmpty()) {
			throw new LocadoraException("Filme vazio");
		}

		for (Filme filme : filmeLista) {
			if (filme.getEstoque() == 0) {
				throw new FilmeSemEstoqueException();
			}
		}

		Double precoLocacao = 0.0;
		int quantidadeDeFilme = 1;
		for (Filme filme : filmeLista) {

			switch (quantidadeDeFilme) {
				case 3:
					precoLocacao += filme.getPrecoLocacao() * 0.75;
					break;
				case 4:
					precoLocacao += filme.getPrecoLocacao() * 0.50;
					break;
				case 5:
					precoLocacao += filme.getPrecoLocacao() * 0.25;
					break;
				case 6:
					precoLocacao += filme.getPrecoLocacao() * 0;
					break;
				default:
					precoLocacao += filme.getPrecoLocacao();
			}

			quantidadeDeFilme++;
		}

		Locacao locacao = new Locacao();
		locacao.setFilme(filmeLista);
		locacao.setUsuario(usuario);
		locacao.setDataLocacao(new Date());
		locacao.setValor(precoLocacao);

		//Entrega no dia seguinte
		Date dataEntrega = new Date();
		dataEntrega = adicionarDias(dataEntrega, 1);

		if (DataUtils.verificarDiaSemana(dataEntrega, Calendar.SUNDAY)) {
			dataEntrega = DataUtils.adicionarDias(dataEntrega, 1);
		}

		locacao.setDataRetorno(dataEntrega);
		
		//Salvando a locacao...	
		//dao.salvar(locacao);
		
		return locacao;
	}
}