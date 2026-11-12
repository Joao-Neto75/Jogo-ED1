package br.ed1.candymatch.estruturas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ListaEncadeadaTest {

    @Test
    void adicionaNoFimENoInicioMantendoOrdem() {
        ListaEncadeada<Integer> lista = new ListaEncadeada<>();
        lista.adicionar(2);
        lista.adicionar(3);
        lista.adicionarNoInicio(1);

        assertEquals(3, lista.tamanho());
        assertEquals(1, lista.obter(0));
        assertEquals(3, lista.obter(2));
        assertEquals("[1, 2, 3]", lista.toString());
    }

    @Test
    void removerUltimoAtualizaFimParaNovasInsercoes() {
        ListaEncadeada<String> lista = ListaEncadeada.deVetor(new String[]{"a", "b", "c"});

        assertEquals("c", lista.remover(2));
        lista.adicionar("d");

        assertEquals("[a, b, d]", lista.toString());
    }

    @Test
    void removerUnicoElementoDeixaListaVaziaEReutilizavel() {
        ListaEncadeada<String> lista = new ListaEncadeada<>();
        lista.adicionar("x");

        assertTrue(lista.removerElemento("x"));
        assertTrue(lista.estaVazia());
        lista.adicionar("y");
        assertEquals("[y]", lista.toString());
    }

    @Test
    void removerElementoInexistenteRetornaFalse() {
        ListaEncadeada<String> lista = ListaEncadeada.deVetor(new String[]{"a"});
        assertFalse(lista.removerElemento("z"));
        assertEquals(1, lista.tamanho());
    }

    @Test
    void contemEParaVetor() {
        ListaEncadeada<Integer> lista = ListaEncadeada.deVetor(new Integer[]{5, 6, 7});

        assertTrue(lista.contem(6));
        assertFalse(lista.contem(9));
        assertArrayEquals(new Integer[]{5, 6, 7}, lista.paraVetor(Integer[]::new));
    }

    @Test
    void iteracaoPercorreTodosOsElementos() {
        ListaEncadeada<Integer> lista = ListaEncadeada.deVetor(new Integer[]{1, 2, 3});
        int soma = 0;
        for (int valor : lista) {
            soma += valor;
        }
        assertEquals(6, soma);
    }

    @Test
    void indiceInvalidoENuloLancamExcecao() {
        ListaEncadeada<Integer> lista = new ListaEncadeada<>();
        assertThrows(IndexOutOfBoundsException.class, () -> lista.obter(0));
        assertThrows(IndexOutOfBoundsException.class, () -> lista.remover(-1));
        assertThrows(NullPointerException.class, () -> lista.adicionar(null));
    }
}
