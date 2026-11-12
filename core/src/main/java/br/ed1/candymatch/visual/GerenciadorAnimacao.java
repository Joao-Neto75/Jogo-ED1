package br.ed1.candymatch.visual;

import br.ed1.candymatch.estruturas.Fila;
import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;

import java.util.Arrays;
import java.util.Objects;

/**
 * Consome a fila de eventos da partida e anima as mudanças na ordem em que ocorreram.
 * Requisito: Fila (sem ela, quedas de uma cascata poderiam ser animadas antes das remoções).
 *
 * <p>A lógica já aplicou tudo no tabuleiro real; aqui existe um <b>tabuleiro visual</b> que começa igual
 * ao estado antes da jogada e recebe os eventos um grupo por vez. Eventos consecutivos compatíveis
 * (todas as remoções de uma rodada; quedas + criações) formam uma <b>etapa</b>, animada em paralelo.
 * Ao fim da última etapa o tabuleiro visual é idêntico ao real.
 */
public class GerenciadorAnimacao {

    /** Avisado no início de cada etapa (usado para tocar sons). */
    public interface Ouvinte {
        /** @param rodadaRemocao 1 na primeira remoção do lote, 2 na primeira cascata, ... (0 se não for remoção) */
        void aoIniciarEtapa(EventoTabuleiro.Tipo tipo, int rodadaRemocao);
    }

    private static final float DURACAO_TROCA = 0.18f;
    private static final float DURACAO_REMOCAO = 0.22f;
    private static final float DURACAO_QUEDA_POR_CELULA = 0.06f;
    private static final float DURACAO_QUEDA_MINIMA = 0.16f;

    private final Fila<EventoTabuleiro> pendentes = new Fila<>();
    private final ListaEncadeada<EventoTabuleiro> etapa = new ListaEncadeada<>();
    private final Tabuleiro tabuleiroReal;
    private final Tabuleiro visual;
    /** Células cujas gemas estão em movimento na etapa atual e não devem ser desenhadas paradas. */
    private final boolean[][] emMovimento;
    /** Quantas gemas novas caem em cada coluna na etapa atual (para saber de que altura surgem). */
    private final int[] criacoesPorColuna;
    private final Ouvinte ouvinte;

    private EventoTabuleiro.Tipo tipoEtapa;
    private float tempo;
    private float duracao;
    private int rodadaRemocao;

    public GerenciadorAnimacao(Tabuleiro tabuleiroReal, Ouvinte ouvinte) {
        this.tabuleiroReal = Objects.requireNonNull(tabuleiroReal, "tabuleiro não pode ser nulo");
        this.ouvinte = Objects.requireNonNull(ouvinte, "ouvinte não pode ser nulo");
        this.visual = tabuleiroReal.copiar();
        this.emMovimento = new boolean[tabuleiroReal.getLinhas()][tabuleiroReal.getColunas()];
        this.criacoesPorColuna = new int[tabuleiroReal.getColunas()];
    }

    /** Transfere os eventos da partida para a fila de animação, esvaziando a origem. */
    public void receber(Fila<EventoTabuleiro> eventosDaPartida) {
        Objects.requireNonNull(eventosDaPartida, "fila de eventos não pode ser nula");
        if (!estaAnimando()) {
            rodadaRemocao = 0;
        }
        while (!eventosDaPartida.estaVazia()) {
            pendentes.enfileirar(eventosDaPartida.desenfileirar());
        }
    }

    /** Descarta animações pendentes e copia o tabuleiro real (após desfazer, por exemplo). */
    public void sincronizar() {
        pendentes.limpar();
        etapa.limpar();
        tipoEtapa = null;
        visual.restaurar(tabuleiroReal);
        limparMarcacoes();
    }

    public void atualizar(float delta) {
        if (tipoEtapa == null) {
            iniciarProximaEtapa();
            return;
        }
        tempo += delta;
        if (tempo >= duracao) {
            concluirEtapa();
            iniciarProximaEtapa();
        }
    }

    /** Enquanto houver animação, a entrada do jogador deve ser ignorada. */
    public boolean estaAnimando() {
        return tipoEtapa != null || !pendentes.estaVazia();
    }

    /** Estado que está sendo exibido; exposto ao pacote para os testes conferirem a reprodução dos eventos. */
    Tabuleiro getVisual() {
        return visual;
    }

    public void desenhar(ShapeRenderer sr, RenderizadorTabuleiro renderizador) {
        for (int linha = 0; linha < visual.getLinhas(); linha++) {
            for (int coluna = 0; coluna < visual.getColunas(); coluna++) {
                Gema gema = visual.obterGema(new Posicao(linha, coluna));
                if (gema != null && !emMovimento[linha][coluna]) {
                    renderizador.desenharGema(sr, gema.getTipo(), renderizador.centroX(coluna),
                            renderizador.centroY(linha), 1f, 1f);
                }
            }
        }
        if (tipoEtapa == null) {
            return;
        }
        float progresso = Math.min(1f, tempo / duracao);
        for (EventoTabuleiro evento : etapa) {
            desenharEvento(sr, renderizador, evento, progresso);
        }
    }

    private void desenharEvento(ShapeRenderer sr, RenderizadorTabuleiro r, EventoTabuleiro evento, float progresso) {
        switch (evento.tipo()) {
            case TROCA -> {
                float p = Interpolation.smooth.apply(progresso);
                desenharDeslizando(sr, r, visual.obterGema(evento.origem()), evento.origem(), evento.destino(), p);
                desenharDeslizando(sr, r, visual.obterGema(evento.destino()), evento.destino(), evento.origem(), p);
            }
            case REMOCAO -> {
                float escala = 1f + 0.25f * Interpolation.sine.apply(Math.min(1f, progresso * 2f))
                        - 1.25f * Interpolation.pow2In.apply(progresso);
                r.desenharGema(sr, evento.gema().getTipo(), r.centroX(evento.origem().coluna()),
                        r.centroY(evento.origem().linha()), escala, 1f - progresso * 0.5f);
            }
            case QUEDA -> desenharDeslizando(sr, r, evento.gema(), evento.origem(), evento.destino(),
                    Interpolation.bounceOut.apply(progresso));
            case CRIACAO -> {
                Posicao destino = evento.destino();
                float linhaInicial = destino.linha() - criacoesPorColuna[destino.coluna()];
                float linha = linhaInicial + (destino.linha() - linhaInicial) * Interpolation.bounceOut.apply(progresso);
                r.desenharGema(sr, evento.gema().getTipo(), r.centroX(destino.coluna()), r.centroY(linha), 1f,
                        Math.min(1f, progresso * 2.5f));
            }
            case EMBARALHAMENTO -> {
                // Instantâneo: tratado em iniciarProximaEtapa.
            }
        }
    }

    private static void desenharDeslizando(ShapeRenderer sr, RenderizadorTabuleiro r, Gema gema, Posicao de,
                                           Posicao para, float p) {
        if (gema == null) {
            return;
        }
        float linha = de.linha() + (para.linha() - de.linha()) * p;
        float coluna = de.coluna() + (para.coluna() - de.coluna()) * p;
        r.desenharGema(sr, gema.getTipo(), r.centroX(coluna), r.centroY(linha), 1f, 1f);
    }

    private void iniciarProximaEtapa() {
        tipoEtapa = null;
        etapa.limpar();
        limparMarcacoes();
        if (pendentes.estaVazia()) {
            return;
        }
        EventoTabuleiro primeiro = pendentes.desenfileirar();
        if (primeiro.tipo() == EventoTabuleiro.Tipo.EMBARALHAMENTO) {
            // Embaralhamento é sempre o último evento do lote, então o tabuleiro real já é o estado final.
            visual.restaurar(tabuleiroReal);
            ouvinte.aoIniciarEtapa(EventoTabuleiro.Tipo.EMBARALHAMENTO, 0);
            iniciarProximaEtapa();
            return;
        }
        etapa.adicionar(primeiro);
        // Cada troca é uma etapa própria (troca inválida = ida e volta em duas etapas).
        if (primeiro.tipo() != EventoTabuleiro.Tipo.TROCA) {
            while (!pendentes.estaVazia() && compativeis(primeiro.tipo(), pendentes.espiar().tipo())) {
                etapa.adicionar(pendentes.desenfileirar());
            }
        }
        tipoEtapa = primeiro.tipo();
        tempo = 0f;
        marcarCelulasEmMovimento();
        duracao = calcularDuracao();
        if (tipoEtapa == EventoTabuleiro.Tipo.REMOCAO) {
            rodadaRemocao++;
        }
        ouvinte.aoIniciarEtapa(tipoEtapa, tipoEtapa == EventoTabuleiro.Tipo.REMOCAO ? rodadaRemocao : 0);
    }

    private static boolean compativeis(EventoTabuleiro.Tipo a, EventoTabuleiro.Tipo b) {
        boolean aCai = a == EventoTabuleiro.Tipo.QUEDA || a == EventoTabuleiro.Tipo.CRIACAO;
        boolean bCai = b == EventoTabuleiro.Tipo.QUEDA || b == EventoTabuleiro.Tipo.CRIACAO;
        return a == b || (aCai && bCai);
    }

    private float calcularDuracao() {
        if (tipoEtapa == EventoTabuleiro.Tipo.TROCA) {
            return DURACAO_TROCA;
        }
        if (tipoEtapa == EventoTabuleiro.Tipo.REMOCAO) {
            return DURACAO_REMOCAO;
        }
        int maiorDistancia = 1;
        for (EventoTabuleiro evento : etapa) {
            if (evento.tipo() == EventoTabuleiro.Tipo.QUEDA) {
                maiorDistancia = Math.max(maiorDistancia, evento.destino().linha() - evento.origem().linha());
            }
        }
        for (int quantidade : criacoesPorColuna) {
            maiorDistancia = Math.max(maiorDistancia, quantidade);
        }
        return Math.max(DURACAO_QUEDA_MINIMA, maiorDistancia * DURACAO_QUEDA_POR_CELULA + 0.1f);
    }

    private void marcarCelulasEmMovimento() {
        for (EventoTabuleiro evento : etapa) {
            switch (evento.tipo()) {
                case TROCA -> {
                    marcar(evento.origem());
                    marcar(evento.destino());
                }
                case REMOCAO, QUEDA -> marcar(evento.origem());
                case CRIACAO -> criacoesPorColuna[evento.destino().coluna()]++;
                case EMBARALHAMENTO -> {
                }
            }
        }
    }

    /** Aplica os eventos da etapa ao tabuleiro visual, na mesma ordem em que a lógica os produziu. */
    private void concluirEtapa() {
        for (EventoTabuleiro evento : etapa) {
            switch (evento.tipo()) {
                case TROCA -> visual.trocar(evento.origem(), evento.destino());
                case REMOCAO -> visual.removerGema(evento.origem());
                case QUEDA -> {
                    visual.removerGema(evento.origem());
                    visual.definirGema(evento.destino(), evento.gema());
                }
                case CRIACAO -> visual.definirGema(evento.destino(), evento.gema());
                case EMBARALHAMENTO -> visual.restaurar(tabuleiroReal);
            }
        }
    }

    private void marcar(Posicao posicao) {
        emMovimento[posicao.linha()][posicao.coluna()] = true;
    }

    private void limparMarcacoes() {
        for (boolean[] linha : emMovimento) {
            Arrays.fill(linha, false);
        }
        Arrays.fill(criacoesPorColuna, 0);
    }
}
