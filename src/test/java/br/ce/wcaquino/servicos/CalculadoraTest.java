package br.ce.wcaquino.servicos;

import org.junit.Assert;
import org.junit.Test;

public class CalculadoraTest {

    private static final int SOMA_ESPERADA = 8;
    private static final int SUBTRACAO_ESPERADA = 2;

    @Test
    public void deveSomarDoisValores() {
        // Arrange
        int a = 5;
        int b = 3;

        Calculadora calculadora = new Calculadora();

        // act
        int resultado = calculadora.somar(a, b);

        // Assert
        Assert.assertEquals(SOMA_ESPERADA, resultado);

    }

    @Test
    public void deveSubtrairDoisValores() {
        // Arrange
        int a = 5;
        int b = 3;

        Calculadora calculadora = new Calculadora();

        // act
        int resultado = calculadora.subtrair(a, b);

        // Assert
        Assert.assertEquals(SUBTRACAO_ESPERADA, resultado);

    }
}
