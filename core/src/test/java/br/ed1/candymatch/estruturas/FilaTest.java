package br.ed1.candymatch.estruturas;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class FilaTest {

    @Test
    void primeiroEnfileiradoEhPrimeiroDesenfileirado() {
        Fila<Integer> fila = new Fila<>();
        fila.enfileirar(1);
        fila.enfileirar(2);
        fila.enfileirar(3);

        assertEquals(1, fila.espiar());
        assertEquals(1, fila.desenfileirar());
        assertEquals(2, fila.desenfileirar());
        assertEquals(1, fila.tamanho());
    }

    @Test
    void esvaziarEReutilizarMantemOrdem() {
        Fila<String> fila = new Fila<>();
        fila.enfileirar("a");
        fila.desenfileirar();
        fila.enfileirar("b");
        fila.enfileirar("c");

        assertEquals("b", fila.desenfileirar());
        assertEquals("c", fila.desenfileirar());
    }

    @Test
    void operacoesEmFilaVaziaLancamExcecao() {
        Fila<String> fila = new Fila<>();
        assertThrows(NoSuchElementException.class, fila::desenfileirar);
        assertThrows(NoSuchElementException.class, fila::espiar);
    }
}
