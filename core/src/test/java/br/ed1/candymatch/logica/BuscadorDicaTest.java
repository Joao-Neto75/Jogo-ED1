package br.ed1.candymatch.logica;

import br.ed1.candymatch.FabricaTabuleiro;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Tabuleiro;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuscadorDicaTest {

    private final BuscadorCombinacoes buscadorCombinacoes = new BuscadorCombinacoes();
    private final BuscadorDica dica = new BuscadorDica(buscadorCombinacoes);

    @Test
    void encontraJogadaQueGeraCombinacao() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "VVAV",
                "AEMR",
                "MRER");
        String antes = FabricaTabuleiro.paraTexto(t);
        Optional<Jogada> jogada = dica.buscar(t);
        assertTrue(jogada.isPresent());
        // A busca não pode alterar o tabuleiro real.
        assertEquals(antes, FabricaTabuleiro.paraTexto(t));

        Tabuleiro copia = t.copiar();
        copia.trocar(jogada.get().origem(), jogada.get().destino());
        assertTrue(buscadorCombinacoes.existeCombinacao(copia));
    }

    @Test
    void tabuleiroSemJogadasRetornaVazio() {
        Tabuleiro t = FabricaTabuleiro.deTexto(
                "VAE",
                "MRL",
                "VAE");
        assertFalse(dica.existeJogadaPossivel(t));
    }
}
