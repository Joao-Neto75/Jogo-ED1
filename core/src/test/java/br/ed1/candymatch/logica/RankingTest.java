package br.ed1.candymatch.logica;

import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.RegistroPontuacao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingTest {

    private static Ranking rankingCom(int capacidade, RegistroPontuacao... registros) {
        return new Ranking(ListaEncadeada.deVetor(registros), capacidade);
    }

    @Test
    void construtorOrdenaPorPontosDecrescente() {
        Ranking r = rankingCom(10,
                new RegistroPontuacao("Ana", 50),
                new RegistroPontuacao("Bia", 300),
                new RegistroPontuacao("Caio", 120));
        RegistroPontuacao[] vetor = r.porPontos();
        assertEquals("Bia", vetor[0].getNome());
        assertEquals("Caio", vetor[1].getNome());
        assertEquals("Ana", vetor[2].getNome());
    }

    @Test
    void registrarMantemApenasOsMelhoresDentroDaCapacidade() {
        Ranking r = rankingCom(2, new RegistroPontuacao("Ana", 50), new RegistroPontuacao("Bia", 300));
        assertTrue(r.registrar(new RegistroPontuacao("Caio", 100)));
        assertEquals(2, r.tamanho());
        assertEquals("Caio", r.porPontos()[1].getNome());
        assertFalse(r.registrar(new RegistroPontuacao("Davi", 10)));
        assertEquals(2, r.tamanho());
    }

    @Test
    void jogadorRepetidoMantemSoORecorde() {
        Ranking r = rankingCom(10, new RegistroPontuacao("Ana", 200));
        assertFalse(r.registrar(new RegistroPontuacao("ana", 100)));
        assertEquals(200, r.buscarRecorde("Ana").orElseThrow().getPontos());
        assertTrue(r.registrar(new RegistroPontuacao("ANA", 500)));
        assertEquals(1, r.tamanho());
        assertEquals(500, r.buscarRecorde("ana").orElseThrow().getPontos());
    }

    @Test
    void buscaRecordePorNomeComBuscaBinaria() {
        Ranking r = rankingCom(10,
                new RegistroPontuacao("Zeca", 10),
                new RegistroPontuacao("Ana", 50),
                new RegistroPontuacao("Marcos", 999),
                new RegistroPontuacao("Bia", 300));
        assertEquals(999, r.buscarRecorde("marcos").orElseThrow().getPontos());
        assertEquals(10, r.buscarRecorde("Zeca").orElseThrow().getPontos());
        assertTrue(r.buscarRecorde("Ninguém").isEmpty());
        assertTrue(r.buscarRecorde("  ").isEmpty());
        assertEquals(1, r.posicaoDe("Marcos"));
        assertEquals(0, r.posicaoDe("Ninguém"));
    }
}
