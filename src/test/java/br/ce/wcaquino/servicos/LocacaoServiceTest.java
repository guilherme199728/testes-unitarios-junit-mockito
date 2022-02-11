package br.ce.wcaquino.servicos;

import br.ce.wcaquino.builders.FilmeBuilder;
import br.ce.wcaquino.builders.UsuarioBuilder;
import br.ce.wcaquino.entidades.Filme;
import br.ce.wcaquino.entidades.Locacao;
import br.ce.wcaquino.entidades.Usuario;
import br.ce.wcaquino.exceptions.FilmeSemEstoqueException;
import br.ce.wcaquino.exceptions.LocadoraException;
import br.ce.wcaquino.utils.DataUtils;
import buildermaster.BuilderMaster;
import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;

import java.util.*;

import static br.ce.wcaquino.builders.FilmeBuilder.*;
import static br.ce.wcaquino.builders.UsuarioBuilder.*;
import static br.ce.wcaquino.matchers.MatchersProprios.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LocacaoServiceTest {

    private static final Double VALOR_ESPERADO = 5.0;
    private LocacaoService locacaoService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public ErrorCollector error = new ErrorCollector();

    @Before
    // executa antes de todo teste
    public void setup() {
        locacaoService = new LocacaoService();
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
        Assume.assumeFalse(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

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
        Assume.assumeTrue(DataUtils.verificarDiaSemana(new Date(), Calendar.SATURDAY));

        // Arrange
        Usuario usuario = umUsuario().agora();
        List<Filme> filmeLista = Arrays.asList(umFilme().agora());

        // Act
        Locacao retorno = locacaoService.alugarFilme(usuario, filmeLista);

        // Assert
        boolean ehSegunda = DataUtils.verificarDiaSemana(retorno.getDataRetorno(), Calendar.MONDAY);
        assertTrue(ehSegunda);
        assertThat(retorno.getDataRetorno(), caiEm(Calendar.MONDAY));
        assertThat(retorno.getDataRetorno(), caiEmUmaSegunda());
    }
}
