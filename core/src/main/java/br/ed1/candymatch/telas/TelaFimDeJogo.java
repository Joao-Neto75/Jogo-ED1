package br.ed1.candymatch.telas;

import br.ed1.candymatch.JogoPrincipal;
import br.ed1.candymatch.arquivos.GerenciadorArquivoRanking;
import br.ed1.candymatch.logica.Partida;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.RegistroPontuacao;
import br.ed1.candymatch.visual.Botao;
import br.ed1.candymatch.visual.CampoTexto;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Exibe vitória ou derrota, pede o nome do jogador e registra a pontuação no ranking.
 * Requisitos demonstrados: Arquivos (gravação do ranking), Ordenação e Lista Encadeada (via Ranking).
 */
public class TelaFimDeJogo extends TelaBase {

    private static final int TAMANHO_MAXIMO_NOME = 14;
    private static final float LARGURA_CAMPO = 440f;
    private static final float LARGURA_BOTAO = 300f;
    private static final float ALTURA_BOTAO = 64f;
    private static final Color COR_PRINCIPAL = new Color(0.95f, 0.3f, 0.55f, 1f);
    private static final Color COR_SECUNDARIA = new Color(0.4f, 0.3f, 0.75f, 1f);

    private final int pontos;
    private final boolean venceu;
    private final int numeroNivel;
    private final CampoTexto campoNome;
    private final Botao botaoSalvar;
    private String resultado = "";
    private boolean registrado;

    public TelaFimDeJogo(JogoPrincipal jogo, int pontos, boolean venceu, int numeroNivel) {
        super(jogo);
        if (pontos < 0) {
            throw new IllegalArgumentException("pontuação não pode ser negativa: " + pontos);
        }
        this.pontos = pontos;
        this.venceu = venceu;
        this.numeroNivel = numeroNivel;

        float centro = LARGURA_MUNDO / 2f;
        campoNome = new CampoTexto(centro - LARGURA_CAMPO / 2f - 80f, 470f, LARGURA_CAMPO, 64f,
                TAMANHO_MAXIMO_NOME, "Digite seu nome");
        botaoSalvar = botoes.adicionar(new Botao("Salvar", centro + LARGURA_CAMPO / 2f - 60f, 470f, 140f, 64f,
                COR_PRINCIPAL, this::registrar));

        boolean temProximo = venceu && jogo.getCarregadorNivel().existeNivel(numeroNivel + 1);
        float x = centro - LARGURA_BOTAO / 2f;
        float y = 340f;
        if (temProximo) {
            botoes.adicionar(new Botao("Próximo nível", x, y, LARGURA_BOTAO, ALTURA_BOTAO, COR_PRINCIPAL,
                    () -> iniciarNivel(numeroNivel + 1)));
            y -= 84f;
        }
        botoes.adicionar(new Botao("Jogar de novo", x, y, LARGURA_BOTAO, ALTURA_BOTAO, COR_SECUNDARIA,
                () -> iniciarNivel(numeroNivel)));
        y -= 84f;
        botoes.adicionar(new Botao("Ranking", x, y, LARGURA_BOTAO, ALTURA_BOTAO, COR_SECUNDARIA,
                () -> jogo.irPara(new TelaRanking(jogo))));
        y -= 84f;
        botoes.adicionar(new Botao("Menu", x, y, LARGURA_BOTAO, ALTURA_BOTAO, COR_SECUNDARIA,
                () -> jogo.irPara(new TelaMenu(jogo))));
    }

    @Override
    protected InputProcessor criarEntradaExtra() {
        return new InputAdapter() {
            @Override
            public boolean keyTyped(char caractere) {
                if (caractere == '\r' || caractere == '\n') {
                    registrar();
                    return true;
                }
                return campoNome.digitar(caractere);
            }

            @Override
            public boolean keyDown(int tecla) {
                if (tecla == Input.Keys.ESCAPE) {
                    jogo.irPara(new TelaMenu(jogo));
                    return true;
                }
                return false;
            }
        };
    }

    private void registrar() {
        if (registrado) {
            return;
        }
        String nome = GerenciadorArquivoRanking.sanitizarNome(campoNome.getTexto());
        if (nome.isEmpty()) {
            resultado = "Digite um nome para salvar.";
            return;
        }
        if (pontos == 0) {
            resultado = "Sem pontos para registrar.";
            return;
        }
        try {
            boolean entrou = jogo.getArquivoRanking().registrar(new RegistroPontuacao(nome, pontos));
            int posicao = jogo.getArquivoRanking().carregarRanking().posicaoDe(nome);
            resultado = entrou ? "Salvo! Você está em " + posicao + "º lugar no ranking."
                    : "Não superou o recorde/top " + GerenciadorArquivoRanking.MAXIMO_REGISTROS + ".";
            registrado = true;
            campoNome.setAtivo(false);
            botaoSalvar.setHabilitado(false);
        } catch (IllegalStateException | IllegalArgumentException e) {
            Gdx.app.error("Ranking", "Falha ao registrar pontuação", e);
            resultado = "Erro ao salvar o ranking.";
        }
    }

    private void iniciarNivel(int numero) {
        try {
            Nivel nivel = jogo.getCarregadorNivel().carregar(numero);
            jogo.irPara(new TelaJogo(jogo, Partida.criar(nivel, jogo.getAleatorio())));
        } catch (IllegalArgumentException | IllegalStateException e) {
            Gdx.app.error("Fim de jogo", "Falha ao iniciar o nível " + numero, e);
            resultado = "Não foi possível abrir o nível " + numero + ".";
        }
    }

    @Override
    protected void atualizar(float delta) {
        campoNome.atualizar(delta);
    }

    @Override
    protected void desenharFormas(ShapeRenderer formas) {
        campoNome.desenharForma(formas);
    }

    @Override
    protected void desenharTextos(SpriteBatch lote) {
        float centro = LARGURA_MUNDO / 2f;
        textoCentralizado(lote, jogo.getFontes().getTitulo(), venceu ? "Vitória!" : "Fim de jogo", centro, 800f);
        textoCentralizado(lote, jogo.getFontes().getMedia(), "Nível " + numeroNivel, centro, 690f);
        textoCentralizado(lote, jogo.getFontes().getGrande(), pontos + " pontos", centro, 640f);
        campoNome.desenharTexto(lote, jogo.getFontes().getMedia());
        if (!resultado.isEmpty()) {
            jogo.getFontes().getPequena().setColor(1f, 0.9f, 0.5f, 1f);
            textoCentralizado(lote, jogo.getFontes().getPequena(), resultado, centro, 445f);
            jogo.getFontes().getPequena().setColor(Color.WHITE);
        }
    }
}
