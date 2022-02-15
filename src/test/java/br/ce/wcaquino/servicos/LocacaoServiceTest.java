package br.ce.wcaquino.servicos;

import br.ce.wcaquino.builders.LocacaoBuilder;
import br.ce.wcaquino.daos.LocacaoDAO;
import br.ce.wcaquino.daos.LocacaoDAOFake;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.util.*;

import static br.ce.wcaquino.builders.FilmeBuilder.*;
import static br.ce.wcaquino.builders.LocacaoBuilder.*;
import static br.ce.wcaquino.builders.UsuarioBuilder.*;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
import static br.ce.wcaquino.utils.DataUtils.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

public class LocacaoServiceTest {

    private static final Double VALOR_ESPERADO = 5.0;
    private LocacaoService locacaoService;
    private SPCService spcService;
    private LocacaoDAO dao;
    private EmailService emailService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Before
    // executa antes de todo teste
    public void setup() {
        locacaoService = new LocacaoService();
        dao = mock(LocacaoDAO.class);
        locacaoService.setLocacaoDAO(dao);
        spcService = mock(SPCService.class);
        locacaoService.setSpcService(spcService);
        emailService = mock(EmailService.class);
        locacaoService.setEmailService(emailService);
    }

    @After
    // executa depois de todo teste
    public void after() {

    }

    @BeforeClass
    // executa antes de inicializar a classe
    public static void beforeClass() {

    }

    @AfterClass
    // executa depois de finalizar a classe
    public static void afterClass() {

    }

    @Test
    public void deveAlugarFilme() throws Exception {
        Assume.assumeFalse(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        // Arrange
        Usuario usuario = umUsuario().agora();
        List<Filme> filmeLista = Arrays.asList(umFilme().comPreco(5.0).agora());

        // Act
        Locacao locacao = locacaoService.alugarFilme(usuario, filmeLista);

        // Assert
        error.checkThat(locacao.getValor(), is(equalTo(VALOR_ESPERADO)));
        error.checkThat(locacao.getDataLocacao(), ehHoje());
        error.checkThat(locacao.getDataRetorno(), ehHojeDataDiferencaDias(1));

    }

    @Test(expected = FilmeSemEstoqueException.class)
    public void naoDeveAlugarFilmeSemEstoque() throws Exception {
        // Arrange
        Usuario usuario = umUsuario().agora();
        List<Filme> filmeLista = Arrays.asList(umFilme().semEstoque().agora());

        // Act
        locacaoService.alugarFilme(usuario, filmeLista);
    }

    @Test
    public void naoDeveAlugarFilmeUsuarioVazio() throws FilmeSemEstoqueException {
        // Arrange
        List<Filme> filmeLista = Arrays.asList(umFilme().agora());

        try {
            // Act
            locacaoService.alugarFilme(null, filmeLista);

            // Assert
            fail("Deveria ter lancado uma excecao");
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario vazio"));
        }
    }

    @Test
    public void naoDeveAlugarFilmeVazio() throws FilmeSemEstoqueException, LocadoraException {
        // Arrange
        Usuario usuario = umUsuario().agora();

        // Act
        expectedException.expect(LocadoraException.class);
        expectedException.expectMessage("Filme vazio");
        locacaoService.alugarFilme(usuario, null);
    }

    @Test
    public void deveDovolverNaSegundaAoAlugarNoSabado() throws FilmeSemEstoqueException, LocadoraException {
        Assume.assumeTrue(verificarDiaSemana(new Date(), Calendar.SATURDAY));

        // Arrange
        Usuario usuario = umUsuario().agora();
        List<Filme> filmeLista = Arrays.asList(umFilme().agora());

        // Act
        Locacao retorno = locacaoService.alugarFilme(usuario, filmeLista);

        // Assert
        boolean ehSegunda = verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);
        assertTrue(ehSegunda);
        assertThat(retorno.getDataRetorno(), caiEm(Calendar.MONDAY));
        assertThat(retorno.getDataRetorno(), caiEmUmaSegunda());
    }

    @Test
    public void naoDeveAlugarFilmeParaNegativadoSPC() throws FilmeSemEstoqueException, LocadoraException {

        //Arrange
        Usuario usuario = umUsuario().agora();
        List<Filme> filmes = Arrays.asList(umFilme().agora());

        when(spcService.possuiNegativacao(usuario)).thenReturn(true);

        // act
        try {
            locacaoService.alugarFilme(usuario, filmes);
        // Assert
            fail();
        } catch (LocadoraException e) {
            assertThat(e.getMessage(), is("Usuario Negativado"));
        }

        verify(spcService).possuiNegativacao(usuario);
    }

    @Test
    public void deveEnviarEmailParaLocacoesAtrasadas() {
        // Arrange
        Usuario usuario = umUsuario().agora();
        List<Locacao> locacoes = Arrays.asList(umLocacao().comUsuario(usuario).comDataRetorno(obterDataComDiferencaDias(-2)).agora());
        when(dao.obterLocacoesPendentes()).thenReturn(locacoes);

        // Act
        locacaoService.notificarAtrasos();

        // Assert
        verify(emailService).notificarAtraso(usuario);
    }
}
