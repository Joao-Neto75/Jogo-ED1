package br.ed1.candymatch.arquivos;

import br.ed1.candymatch.FabricaTabuleiro;
import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.EstadoPartida;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.RegistroPontuacao;
import br.ed1.candymatch.modelo.TipoGema;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** FileHandle com File absoluto funciona sem inicializar o libGDX, então dá para testar no JUnit. */
class ArquivosTest {

    @TempDir
    File pasta;

    private FileHandle arquivo(String nome) {
        return new FileHandle(new File(pasta, nome));
    }

    @Test
    void carregaNiveisReaisDoProjeto() {
        ArquivoNivel carregador = new ArquivoNivel(new FileHandle(new File("../assets/niveis")));
        assertTrue(carregador.contarNiveis() >= 2);
        Nivel nivel1 = carregador.carregar(1);
        assertEquals(1, nivel1.getNumero());
        assertTrue(nivel1.getMetaPontos() > 0);
        assertTrue(nivel1.getTiposDisponiveis().length >= 3);
    }

    @Test
    void carregaNivelComComentariosELinhasVazias() {
        arquivo("nivel1.txt").writeString("# teste\n\nmetaPontos=500\nlimiteJogadas = 12\ntipos=vermelha, AZUL,VERDE\n",
                false, "UTF-8");
        Nivel n = new ArquivoNivel(new FileHandle(pasta)).carregar(1);
        assertEquals(500, n.getMetaPontos());
        assertEquals(12, n.getLimiteJogadas());
        assertArrayEquals(new TipoGema[]{TipoGema.VERMELHA, TipoGema.AZUL, TipoGema.VERDE}, n.getTiposDisponiveis());
    }

    @Test
    void nivelInvalidoOuAusenteLancaExcecaoClara() {
        ArquivoNivel carregador = new ArquivoNivel(new FileHandle(pasta));
        assertThrows(IllegalArgumentException.class, () -> carregador.carregar(9));
        arquivo("nivel1.txt").writeString("metaPontos=abc\nlimiteJogadas=1\ntipos=AZUL,VERDE,ROXA", false, "UTF-8");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> carregador.carregar(1));
        assertTrue(e.getMessage().contains("linha 1"));
        arquivo("nivel2.txt").writeString("metaPontos=10\nlimiteJogadas=1\ntipos=AZUL,ROSA,ROXA", false, "UTF-8");
        assertThrows(IllegalArgumentException.class, () -> carregador.carregar(2));
        arquivo("nivel3.txt").writeString("metaPontos=10\n", false, "UTF-8");
        assertThrows(IllegalArgumentException.class, () -> carregador.carregar(3));
    }

    @Test
    void rankingInexistenteCarregaVazio() {
        assertTrue(new ArquivoRanking(arquivo("ranking.txt")).carregar().estaVazia());
    }

    @Test
    void rankingSalvaECarregaIgnorandoLinhasInvalidas() {
        FileHandle f = arquivo("ranking.txt");
        f.writeString("Ana;300\nlixo\n;10\nBia;-5\nCaio;abc\nDavi;120\n", false, "UTF-8");
        ArquivoRanking g = new ArquivoRanking(f);
        ListaEncadeada<RegistroPontuacao> lista = g.carregar();
        assertEquals(2, lista.tamanho());
        assertEquals("Ana", lista.obter(0).getNome());
        assertEquals(120, lista.obter(1).getPontos());

        g.salvar(lista);
        assertEquals("Ana;300\nDavi;120\n", f.readString("UTF-8"));
    }

    @Test
    void registrarMantemTop10OrdenadoNoArquivo() {
        ArquivoRanking g = new ArquivoRanking(arquivo("ranking.txt"));
        for (int i = 1; i <= 12; i++) {
            g.registrar(new RegistroPontuacao("Jogador" + i, i * 10));
        }
        ListaEncadeada<RegistroPontuacao> lista = g.carregar();
        assertEquals(ArquivoRanking.MAXIMO_REGISTROS, lista.tamanho());
        assertEquals(120, lista.obter(0).getPontos());
        assertEquals(30, lista.obter(9).getPontos());
        assertFalse(g.registrar(new RegistroPontuacao("Fraco", 1)));
        assertEquals(110, g.carregarRanking().buscarRecorde("jogador11").orElseThrow().getPontos());
    }

    @Test
    void sanitizarRemoveSeparador() {
        assertEquals("AnaBia", ArquivoRanking.sanitizarNome(" Ana;Bia "));
    }

    @Test
    void salvamentoIdaEVolta() {
        ArquivoSalvamento g = new ArquivoSalvamento(arquivo("salvamento.txt"));
        assertFalse(g.existeSalvamento());
        assertTrue(g.carregar().isEmpty());

        EstadoPartida estado = new EstadoPartida(FabricaTabuleiro.deTexto("VAE", "MRL", ".AV"), 2, 340, 7);
        g.salvar(estado);
        assertTrue(g.existeSalvamento());
        EstadoPartida lido = g.carregar().orElseThrow();
        assertEquals(2, lido.numeroNivel());
        assertEquals(340, lido.pontos());
        assertEquals(7, lido.jogadasRestantes());
        assertEquals("VAE\nMRL\n.AV", FabricaTabuleiro.paraTexto(lido.tabuleiro()));

        g.apagar();
        assertFalse(g.existeSalvamento());
    }

    @Test
    void salvamentoCorrompidoRetornaVazio() {
        FileHandle f = arquivo("salvamento.txt");
        ArquivoSalvamento g = new ArquivoSalvamento(f);
        f.writeString("1;2\nVAE\n", false, "UTF-8");
        assertTrue(g.carregar().isEmpty());
        f.writeString("1;2;3\nVAE\nVA\n", false, "UTF-8");
        assertTrue(g.carregar().isEmpty());
        f.writeString("1;2;3\nVAX\n", false, "UTF-8");
        assertTrue(g.carregar().isEmpty());
    }
}
