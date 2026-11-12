package br.ed1.candymatch.estruturas;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PilhaTest {

    @Test
    void ultimoEmpilhadoEhPrimeiroDesempilhado() {
        Pilha<String> pilha = new Pilha<>();
        pilha.empilhar("a");
        pilha.empilhar("b");

        assertEquals("b", pilha.espiar());
        assertEquals("b", pilha.desempilhar());
        assertEquals("a", pilha.desempilhar());
        assertTrue(pilha.estaVazia());
    }

    @Test
    void operacoesEmPilhaVaziaLancamExcecao() {
        Pilha<String> pilha = new Pilha<>();
        assertThrows(NoSuchElementException.class, pilha::desempilhar);
        assertThrows(NoSuchElementException.class, pilha::espiar);
    }

    @Test
    void limparZeraOTamanho() {
        Pilha<Integer> pilha = new Pilha<>();
        pilha.empilhar(1);
        pilha.empilhar(2);
        pilha.limpar();
        assertEquals(0, pilha.tamanho());
    }
}
