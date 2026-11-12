package br.ed1.candymatch.telas;

import br.ed1.candymatch.JogoPrincipal;
import br.ed1.candymatch.logica.Partida;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.visual.Botao;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Lista os níveis disponíveis (lidos pelo CarregadorNivel) e inicia a TelaJogo.
 * Requisitos demonstrados: Arquivos (níveis lidos de texto), Vetor (níveis carregados).
 */
public class TelaSelecaoNivel extends TelaBase {

    private static final int COLUNAS = 2;
    private static final float LARGURA_BOTAO = 345f;
    private static final float ALTURA_BOTAO = 120f;
    private static final float ESPACO = 26f;
    private static final float TOPO = 620f;
    private static final float DESLOCAMENTO_TITULO = 14f;
    private static final Color[] CORES = {
            new Color(0.95f, 0.3f, 0.55f, 1f), new Color(0.3f, 0.55f, 0.95f, 1f),
            new Color(0.3f, 0.75f, 0.45f, 1f), new Color(0.95f, 0.6f, 0.2f, 1f),
            new Color(0.65f, 0.35f, 0.9f, 1f), new Color(0.9f, 0.3f, 0.3f, 1f)};

    private final Nivel[] niveis;
    private String erro = "";

    public TelaSelecaoNivel(JogoPrincipal jogo) {
        super(jogo);
        niveis = carregarNiveis();
        float larguraTotal = COLUNAS * LARGURA_BOTAO + (COLUNAS - 1) * ESPACO;
        float inicioX = (LARGURA_MUNDO - larguraTotal) / 2f;
        for (int i = 0; i < niveis.length; i++) {
            if (niveis[i] == null) {
                continue;
            }
            Nivel nivel = niveis[i];
            float x = inicioX + (i % COLUNAS) * (LARGURA_BOTAO + ESPACO);
            float y = TOPO - (i / COLUNAS) * (ALTURA_BOTAO + ESPACO);
            Botao botao = botoes.adicionar(new Botao("Nível " + nivel.getNumero(), x, y, LARGURA_BOTAO, ALTURA_BOTAO,
                    CORES[i % CORES.length], () -> iniciar(nivel)));
            botao.setDeslocamentoTexto(DESLOCAMENTO_TITULO);
        }
        botoes.adicionar(new Botao("Voltar", (LARGURA_MUNDO - 240f) / 2f, 40f, 240f, 64f,
                new Color(0.4f, 0.3f, 0.75f, 1f), () -> jogo.irPara(new TelaMenu(jogo))));
    }

    /** Nível com arquivo inválido vira null no vetor (e não ganha botão), sem impedir os demais. */
    private Nivel[] carregarNiveis() {
        int quantidade = jogo.getCarregadorNivel().contarNiveis();
        Nivel[] carregados = new Nivel[quantidade];
        for (int i = 0; i < quantidade; i++) {
            try {
                carregados[i] = jogo.getCarregadorNivel().carregar(i + 1);
            } catch (IllegalArgumentException e) {
                Gdx.app.error("Níveis", "Nível " + (i + 1) + " ignorado: " + e.getMessage());
                erro = "Algum nível tem arquivo inválido (veja o log).";
            }
        }
        if (quantidade == 0) {
            erro = "Nenhum nível encontrado em assets/niveis.";
        }
        return carregados;
    }

    private void iniciar(Nivel nivel) {
        try {
            jogo.irPara(new TelaJogo(jogo, Partida.criar(nivel, jogo.getAleatorio())));
        } catch (IllegalStateException e) {
            Gdx.app.error("Níveis", "Falha ao criar partida do nível " + nivel.getNumero(), e);
            erro = "Não foi possível iniciar o nível " + nivel.getNumero();
        }
    }

    @Override
    protected void atualizar(float delta) {
    }

    @Override
    protected void desenharFormas(ShapeRenderer formas) {
    }

    @Override
    protected void desenharTextos(SpriteBatch lote) {
        textoCentralizado(lote, jogo.getFontes().getGrande(), "Escolha o nível", LARGURA_MUNDO / 2f, 830f);
        float larguraTotal = COLUNAS * LARGURA_BOTAO + (COLUNAS - 1) * ESPACO;
        float inicioX = (LARGURA_MUNDO - larguraTotal) / 2f;
        jogo.getFontes().getPequena().setColor(1f, 1f, 1f, 0.85f);
        for (int i = 0; i < niveis.length; i++) {
            if (niveis[i] == null) {
                continue;
            }
            float x = inicioX + (i % COLUNAS) * (LARGURA_BOTAO + ESPACO);
            float y = TOPO - (i / COLUNAS) * (ALTURA_BOTAO + ESPACO);
            textoCentralizado(lote, jogo.getFontes().getPequena(), "Meta " + niveis[i].getMetaPontos() + " · "
                            + niveis[i].getLimiteJogadas() + " jogadas · " + niveis[i].getTiposDisponiveis().length + " cores",
                    x + LARGURA_BOTAO / 2f, y + 34f);
        }
        if (!erro.isEmpty()) {
            jogo.getFontes().getPequena().setColor(1f, 0.5f, 0.5f, 1f);
            textoCentralizado(lote, jogo.getFontes().getPequena(), erro, LARGURA_MUNDO / 2f, 140f);
        }
        jogo.getFontes().getPequena().setColor(Color.WHITE);
    }
}
