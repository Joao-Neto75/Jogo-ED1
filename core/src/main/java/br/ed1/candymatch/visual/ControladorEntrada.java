package br.ed1.candymatch.visual;

import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Posicao;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Converte clique ou arraste do jogador em uma {@link Jogada} e a repassa para a tela do jogo.
 * Modo clique: seleciona uma gema e depois clica numa vizinha. Modo arraste: arrasta meia célula numa direção.
 */
public class ControladorEntrada extends InputAdapter {

    /** Fração da célula que o arraste precisa percorrer para virar jogada. */
    private static final float LIMIAR_ARRASTE = 0.45f;

    private final RenderizadorTabuleiro renderizador;
    private final Viewport viewport;
    private final Consumer<Jogada> aoJogar;
    private final BooleanSupplier bloqueado;
    private final Vector2 auxiliar = new Vector2();
    private final Vector2 inicioArraste = new Vector2();

    private Posicao selecionada;
    private Posicao origemArraste;

    /** @param bloqueado quando true (animação em andamento, fim de jogo), a entrada é ignorada */
    public ControladorEntrada(RenderizadorTabuleiro renderizador, Viewport viewport, Consumer<Jogada> aoJogar,
                              BooleanSupplier bloqueado) {
        this.renderizador = Objects.requireNonNull(renderizador, "renderizador não pode ser nulo");
        this.viewport = Objects.requireNonNull(viewport, "viewport não pode ser nulo");
        this.aoJogar = Objects.requireNonNull(aoJogar, "callback aoJogar não pode ser nulo");
        this.bloqueado = Objects.requireNonNull(bloqueado, "condição de bloqueio não pode ser nula");
    }

    @Override
    public boolean touchDown(int telaX, int telaY, int ponteiro, int botao) {
        if (bloqueado.getAsBoolean()) {
            return false;
        }
        Vector2 mundo = viewport.unproject(auxiliar.set(telaX, telaY));
        Optional<Posicao> clicada = renderizador.posicaoEm(mundo.x, mundo.y);
        if (clicada.isEmpty()) {
            selecionada = null;
            return false;
        }
        Posicao posicao = clicada.get();
        if (selecionada != null && selecionada.ehAdjacente(posicao)) {
            jogar(selecionada, posicao);
            return true;
        }
        selecionada = posicao.equals(selecionada) ? null : posicao;
        origemArraste = posicao;
        inicioArraste.set(mundo);
        return true;
    }

    @Override
    public boolean touchDragged(int telaX, int telaY, int ponteiro) {
        if (origemArraste == null || bloqueado.getAsBoolean()) {
            return false;
        }
        Vector2 mundo = viewport.unproject(auxiliar.set(telaX, telaY));
        float dx = mundo.x - inicioArraste.x;
        float dy = mundo.y - inicioArraste.y;
        float limiar = renderizador.getTamanhoCelula() * LIMIAR_ARRASTE;
        if (Math.max(Math.abs(dx), Math.abs(dy)) < limiar) {
            return true;
        }
        Posicao destino;
        if (Math.abs(dx) > Math.abs(dy)) {
            destino = new Posicao(origemArraste.linha(), origemArraste.coluna() + (dx > 0 ? 1 : -1));
        } else {
            // Arrastar para cima (dy > 0) diminui o índice da linha.
            destino = new Posicao(origemArraste.linha() + (dy > 0 ? -1 : 1), origemArraste.coluna());
        }
        Vector2 centroDestino = auxiliar.set(renderizador.centroX(destino.coluna()), renderizador.centroY(destino.linha()));
        if (renderizador.posicaoEm(centroDestino.x, centroDestino.y).isPresent()) {
            jogar(origemArraste, destino);
        } else {
            origemArraste = null;
        }
        return true;
    }

    @Override
    public boolean touchUp(int telaX, int telaY, int ponteiro, int botao) {
        origemArraste = null;
        return false;
    }

    /** Gema selecionada no modo clique (para a tela destacar), ou null. */
    public Posicao getSelecionada() {
        return selecionada;
    }

    public void limparSelecao() {
        selecionada = null;
        origemArraste = null;
    }

    private void jogar(Posicao origem, Posicao destino) {
        limparSelecao();
        aoJogar.accept(new Jogada(origem, destino));
    }
}
