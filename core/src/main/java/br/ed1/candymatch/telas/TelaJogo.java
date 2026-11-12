package br.ed1.candymatch.telas;

import br.ed1.candymatch.JogoPrincipal;
import br.ed1.candymatch.logica.Partida;
import br.ed1.candymatch.modelo.EventoTabuleiro;
import br.ed1.candymatch.modelo.Jogada;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.visual.Botao;
import br.ed1.candymatch.visual.ControladorEntrada;
import br.ed1.candymatch.visual.GerenciadorAnimacao;
import br.ed1.candymatch.visual.GerenciadorSom;
import br.ed1.candymatch.visual.Hud;
import br.ed1.candymatch.visual.RenderizadorTabuleiro;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Tela da partida: liga a lógica ({@link Partida}) ao desenho, à entrada e às animações.
 * Requisitos demonstrados: Pilha (Desfazer), Fila (animação dos eventos), Busca (Dica), Arquivos (salvar ao sair).
 */
public class TelaJogo extends TelaBase {

    private static final float TAMANHO_CELULA = 80f;
    private static final float ORIGEM_TABULEIRO_Y = 118f;
    private static final float DURACAO_DICA = 3f;
    private static final float ESPERA_FIM_DE_JOGO = 1.3f;
    private static final float TOM_EXTRA_POR_CASCATA = 0.12f;
    private static final float LARGURA_BOTAO = 220f;
    private static final float ALTURA_BOTAO = 62f;
    private static final float Y_BOTOES = 26f;
    private static final Color COR_SELECAO = new Color(1f, 1f, 1f, 1f);
    private static final Color COR_DICA = new Color(1f, 0.9f, 0.3f, 1f);

    private final Partida partida;
    private final RenderizadorTabuleiro renderizador;
    private final GerenciadorAnimacao animacao;
    private final ControladorEntrada controlador;
    private final Hud hud;
    private final Botao botaoDesfazer;
    private final Botao botaoDica;

    private Jogada dicaAtual;
    private float tempoDica;
    private float tempo;
    private float tempoAposFim;
    private boolean fimAnunciado;

    public TelaJogo(JogoPrincipal jogo, Partida partida) {
        super(jogo);
        this.partida = Objects.requireNonNull(partida, "partida não pode ser nula");
        Tabuleiro tabuleiro = partida.getTabuleiro();
        float larguraTabuleiro = tabuleiro.getColunas() * TAMANHO_CELULA;
        renderizador = new RenderizadorTabuleiro((LARGURA_MUNDO - larguraTabuleiro) / 2f, ORIGEM_TABULEIRO_Y,
                TAMANHO_CELULA, tabuleiro.getLinhas(), tabuleiro.getColunas());
        animacao = new GerenciadorAnimacao(tabuleiro, this::aoIniciarEtapa);
        controlador = new ControladorEntrada(renderizador, viewport, this::jogar,
                () -> animacao.estaAnimando() || partida.estaFinalizada());
        float topoTabuleiro = renderizador.getTopo();
        hud = new Hud(30f, topoTabuleiro + 22f, LARGURA_MUNDO - 60f, ALTURA_MUNDO - topoTabuleiro - 32f);
        hud.sincronizarPontos(partida.getPontos());

        float espaco = (LARGURA_MUNDO - 3 * LARGURA_BOTAO) / 4f;
        botaoDesfazer = botoes.adicionar(new Botao("Desfazer", espaco, Y_BOTOES, LARGURA_BOTAO, ALTURA_BOTAO,
                new Color(0.3f, 0.55f, 0.95f, 1f), this::desfazer));
        botaoDica = botoes.adicionar(new Botao("Dica", 2 * espaco + LARGURA_BOTAO, Y_BOTOES, LARGURA_BOTAO,
                ALTURA_BOTAO, new Color(0.95f, 0.6f, 0.2f, 1f), this::mostrarDica));
        botoes.adicionar(new Botao("Menu", 3 * espaco + 2 * LARGURA_BOTAO, Y_BOTOES, LARGURA_BOTAO, ALTURA_BOTAO,
                new Color(0.4f, 0.3f, 0.75f, 1f), this::voltarAoMenu));
    }

    @Override
    protected InputProcessor criarEntradaExtra() {
        InputAdapter teclado = new InputAdapter() {
            @Override
            public boolean keyDown(int tecla) {
                switch (tecla) {
                    case Input.Keys.U, Input.Keys.Z -> desfazer();
                    case Input.Keys.H, Input.Keys.D -> mostrarDica();
                    case Input.Keys.M -> jogo.getSom().alternar();
                    case Input.Keys.ESCAPE -> voltarAoMenu();
                    default -> {
                        return false;
                    }
                }
                return true;
            }
        };
        return new InputMultiplexer(teclado, controlador);
    }

    private void jogar(Jogada jogada) {
        limparDica();
        boolean valida;
        try {
            valida = partida.realizarJogada(jogada);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Gdx.app.error("Jogo", "Jogada rejeitada: " + jogada, e);
            return;
        }
        animacao.receber(partida.getEventos());
        if (!valida) {
            jogo.getSom().tocar(GerenciadorSom.Som.INVALIDA);
            return;
        }
        int cascata = partida.getMaiorCascataUltimaJogada();
        if (cascata > 1) {
            hud.mostrarMensagem("Cascata x" + cascata + "!");
        } else {
            hud.mostrarMensagem("+" + partida.getPontosUltimaJogada());
        }
    }

    private void aoIniciarEtapa(EventoTabuleiro.Tipo tipo, int rodadaRemocao) {
        GerenciadorSom som = jogo.getSom();
        switch (tipo) {
            case TROCA -> som.tocar(GerenciadorSom.Som.TROCA);
            case REMOCAO -> som.tocar(GerenciadorSom.Som.COMBINACAO, 1f + (rodadaRemocao - 1) * TOM_EXTRA_POR_CASCATA);
            case EMBARALHAMENTO -> {
                som.tocar(GerenciadorSom.Som.EMBARALHAR);
                hud.mostrarMensagem("Reorganizando!");
            }
            default -> {
            }
        }
    }

    private void desfazer() {
        if (animacao.estaAnimando() || !partida.podeDesfazer()) {
            return;
        }
        if (partida.desfazerJogada()) {
            limparDica();
            controlador.limparSelecao();
            animacao.sincronizar();
            hud.sincronizarPontos(partida.getPontos());
            jogo.getSom().tocar(GerenciadorSom.Som.DESFAZER);
        }
    }

    private void mostrarDica() {
        if (animacao.estaAnimando() || partida.estaFinalizada()) {
            return;
        }
        Optional<Jogada> dica = partida.obterDica();
        if (dica.isPresent()) {
            dicaAtual = dica.get();
            tempoDica = DURACAO_DICA;
            jogo.getSom().tocar(GerenciadorSom.Som.DICA);
        }
    }

    private void limparDica() {
        dicaAtual = null;
        tempoDica = 0f;
    }

    private void voltarAoMenu() {
        salvarSeEmAndamento();
        jogo.irPara(new TelaMenu(jogo));
    }

    /** Partida em andamento é salva para o botão "Continuar"; partida finalizada não deixa salvamento. */
    private void salvarSeEmAndamento() {
        try {
            if (partida.estaFinalizada()) {
                jogo.getSalvamento().apagar();
            } else {
                jogo.getSalvamento().salvar(partida.capturarEstado());
            }
        } catch (IllegalStateException e) {
            Gdx.app.error("Jogo", "Falha ao atualizar o salvamento", e);
        }
    }

    @Override
    public void pause() {
        // No desktop, fechar a janela chama pause() antes de dispose(): a partida não se perde.
        salvarSeEmAndamento();
    }

    @Override
    protected void atualizar(float delta) {
        tempo += delta;
        animacao.atualizar(delta);
        hud.atualizar(delta, partida.getPontos());
        if (tempoDica > 0f) {
            tempoDica -= delta;
            if (tempoDica <= 0f) {
                limparDica();
            }
        }
        boolean livre = !animacao.estaAnimando() && !partida.estaFinalizada();
        botaoDesfazer.setHabilitado(livre && partida.podeDesfazer());
        botaoDica.setHabilitado(livre);

        if (partida.estaFinalizada() && !animacao.estaAnimando()) {
            atualizarFimDeJogo(delta);
        }
    }

    private void atualizarFimDeJogo(float delta) {
        if (!fimAnunciado) {
            fimAnunciado = true;
            salvarSeEmAndamento();
            jogo.getSom().tocar(partida.venceu() ? GerenciadorSom.Som.VITORIA : GerenciadorSom.Som.DERROTA);
            hud.mostrarMensagem(partida.venceu() ? "Meta atingida!" : "Sem jogadas!");
        }
        tempoAposFim += delta;
        if (tempoAposFim >= ESPERA_FIM_DE_JOGO) {
            jogo.irPara(new TelaFimDeJogo(jogo, partida.getPontos(), partida.venceu(), partida.getNivel().getNumero()));
        }
    }

    @Override
    protected void desenharFormas(ShapeRenderer formas) {
        renderizador.desenharFundo(formas);
        if (controlador.getSelecionada() != null && !animacao.estaAnimando()) {
            float pulso = 0.25f + 0.1f * MathUtils.sin(tempo * 8f);
            renderizador.desenharDestaque(formas, controlador.getSelecionada(), COR_SELECAO, pulso);
        }
        if (dicaAtual != null) {
            float pulso = 0.2f + 0.25f * (0.5f + 0.5f * MathUtils.sin(tempo * 7f));
            renderizador.desenharDestaque(formas, dicaAtual.origem(), COR_DICA, pulso);
            renderizador.desenharDestaque(formas, dicaAtual.destino(), COR_DICA, pulso);
        }
        animacao.desenhar(formas, renderizador);
        // O painel vem depois das gemas para cobrir as gemas novas que caem de cima do tabuleiro.
        hud.desenharFormas(formas, partida);
    }

    @Override
    protected void desenharTextos(SpriteBatch lote) {
        hud.desenharTextos(lote, jogo.getFontes(), partida);
        hud.desenharMensagem(lote, jogo.getFontes(), LARGURA_MUNDO / 2f,
                renderizador.getTopo() / 2f + ORIGEM_TABULEIRO_Y / 2f + 40f);
    }
}
