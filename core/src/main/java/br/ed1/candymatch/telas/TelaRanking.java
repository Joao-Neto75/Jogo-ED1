package br.ed1.candymatch.telas;

import br.ed1.candymatch.JogoPrincipal;
import br.ed1.candymatch.logica.Ranking;
import br.ed1.candymatch.modelo.RegistroPontuacao;
import br.ed1.candymatch.visual.Botao;
import br.ed1.candymatch.visual.CampoTexto;
import br.ed1.candymatch.visual.Formas;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Optional;

/**
 * Mostra o top 10 (ordenado por pontos) e permite consultar o recorde de um jogador pelo nome.
 * Requisitos demonstrados aqui: Arquivos (leitura), Lista Encadeada, Ordenação (MergeSort) e Busca binária.
 */
public class TelaRanking extends TelaBase {

    private static final float X_TABELA = 110f;
    private static final float LARGURA_TABELA = 580f;
    private static final float TOPO_TABELA = 720f;
    private static final float ALTURA_LINHA = 40f;
    private static final Color COR_LINHA = new Color(1f, 1f, 1f, 0.07f);
    private static final Color COR_DESTAQUE = new Color(1f, 0.85f, 0.3f, 0.3f);
    private static final Color[] CORES_PODIO = {
            new Color(1f, 0.84f, 0.2f, 1f), new Color(0.82f, 0.85f, 0.9f, 1f), new Color(0.85f, 0.55f, 0.3f, 1f)};

    private final Ranking ranking;
    private final RegistroPontuacao[] porPontos;
    private final CampoTexto campoBusca;
    private final GlyphLayout layout = new GlyphLayout();
    private String resultadoBusca = "";
    private RegistroPontuacao encontrado;

    public TelaRanking(JogoPrincipal jogo) {
        super(jogo);
        ranking = jogo.getArquivoRanking().carregarRanking();
        porPontos = ranking.porPontos();
        campoBusca = new CampoTexto(X_TABELA, 160f, 400f, 60f, 14, "Buscar jogador");
        botoes.adicionar(new Botao("Buscar", X_TABELA + 420f, 160f, 160f, 60f,
                new Color(0.95f, 0.3f, 0.55f, 1f), this::buscar));
        botoes.adicionar(new Botao("Voltar", (LARGURA_MUNDO - 240f) / 2f, 40f, 240f, 64f,
                new Color(0.4f, 0.3f, 0.75f, 1f), () -> jogo.irPara(new TelaMenu(jogo))));
    }

    @Override
    protected InputProcessor criarEntradaExtra() {
        return new InputAdapter() {
            @Override
            public boolean keyTyped(char caractere) {
                if (caractere == '\r' || caractere == '\n') {
                    buscar();
                    return true;
                }
                return campoBusca.digitar(caractere);
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

    /** Busca binária sobre o ranking ordenado por nome (ver {@link Ranking#buscarRecorde}). */
    private void buscar() {
        String nome = campoBusca.getTexto();
        if (nome.isEmpty()) {
            resultadoBusca = "Digite um nome.";
            encontrado = null;
            return;
        }
        Optional<RegistroPontuacao> registro = ranking.buscarRecorde(nome);
        encontrado = registro.orElse(null);
        resultadoBusca = registro
                .map(r -> r.getNome() + ": " + r.getPontos() + " pontos (" + ranking.posicaoDe(r.getNome()) + "º lugar)")
                .orElse("\"" + nome + "\" não está no ranking.");
    }

    @Override
    protected void atualizar(float delta) {
        campoBusca.atualizar(delta);
    }

    @Override
    protected void desenharFormas(ShapeRenderer formas) {
        formas.setColor(0.08f, 0.04f, 0.14f, 0.7f);
        Formas.retanguloArredondado(formas, X_TABELA - 20f, TOPO_TABELA - 10 * ALTURA_LINHA - 20f,
                LARGURA_TABELA + 40f, 10 * ALTURA_LINHA + 70f, 20f);
        for (int i = 0; i < porPontos.length; i++) {
            float y = TOPO_TABELA - (i + 1) * ALTURA_LINHA;
            if (porPontos[i] == encontrado) {
                formas.setColor(COR_DESTAQUE);
            } else if (i % 2 == 0) {
                formas.setColor(COR_LINHA);
            } else {
                continue;
            }
            Formas.retanguloArredondado(formas, X_TABELA - 8f, y - 4f, LARGURA_TABELA + 16f, ALTURA_LINHA - 2f, 8f);
        }
        campoBusca.desenharForma(formas);
    }

    @Override
    protected void desenharTextos(SpriteBatch lote) {
        textoCentralizado(lote, jogo.getFontes().getTitulo(), "Ranking", LARGURA_MUNDO / 2f, 860f);
        BitmapFont media = jogo.getFontes().getMedia();
        BitmapFont pequena = jogo.getFontes().getPequena();

        pequena.setColor(0.85f, 0.8f, 0.95f, 1f);
        pequena.draw(lote, "#", X_TABELA, TOPO_TABELA + 30f);
        pequena.draw(lote, "JOGADOR", X_TABELA + 70f, TOPO_TABELA + 30f);
        alinharDireita(lote, pequena, "PONTOS", X_TABELA + LARGURA_TABELA, TOPO_TABELA + 30f);
        pequena.setColor(Color.WHITE);

        if (porPontos.length == 0) {
            textoCentralizado(lote, media, "Nenhuma pontuação ainda.", LARGURA_MUNDO / 2f, TOPO_TABELA - 150f);
        }
        for (int i = 0; i < porPontos.length; i++) {
            float topo = TOPO_TABELA - i * ALTURA_LINHA - 6f;
            media.setColor(i < CORES_PODIO.length ? CORES_PODIO[i] : Color.WHITE);
            media.draw(lote, (i + 1) + "º", X_TABELA, topo);
            media.setColor(Color.WHITE);
            media.draw(lote, porPontos[i].getNome(), X_TABELA + 70f, topo);
            alinharDireita(lote, media, String.valueOf(porPontos[i].getPontos()), X_TABELA + LARGURA_TABELA, topo);
        }

        campoBusca.desenharTexto(lote, media);
        if (!resultadoBusca.isEmpty()) {
            pequena.setColor(1f, 0.9f, 0.5f, 1f);
            pequena.draw(lote, resultadoBusca, X_TABELA, 140f);
            pequena.setColor(Color.WHITE);
        }
        pequena.setColor(1f, 1f, 1f, 0.5f);
        textoCentralizado(lote, pequena, "Ordenado por MergeSort · busca binária por nome", LARGURA_MUNDO / 2f, 255f);
        pequena.setColor(Color.WHITE);
    }

    private void alinharDireita(SpriteBatch lote, BitmapFont fonte, String texto, float direita, float topo) {
        layout.setText(fonte, texto);
        fonte.draw(lote, layout, direita - layout.width, topo);
    }
}
