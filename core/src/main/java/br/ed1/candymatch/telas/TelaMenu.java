package br.ed1.candymatch.telas;

import br.ed1.candymatch.JogoPrincipal;
import br.ed1.candymatch.logica.Partida;
import br.ed1.candymatch.modelo.EstadoPartida;
import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.TipoGema;
import br.ed1.candymatch.visual.Botao;
import br.ed1.candymatch.visual.DesenhoTabuleiro;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

import java.util.Optional;

/** Menu principal: Jogar (seleção de nível), Continuar (salvamento), Ranking, Som e Sair. */
public class TelaMenu extends TelaBase {

    private static final float LARGURA_BOTAO = 360f;
    private static final float ALTURA_BOTAO = 70f;
    private static final float ESPACO_BOTOES = 88f;
    private static final float TOPO_BOTOES = 520f;
    private static final Color COR_PRINCIPAL = new Color(0.95f, 0.3f, 0.55f, 1f);
    private static final Color COR_SECUNDARIA = new Color(0.4f, 0.3f, 0.75f, 1f);

    private final DesenhoTabuleiro decoracao = new DesenhoTabuleiro(0, 0, 70f, 1, 1);
    private final Botao botaoSom;
    private String erro = "";
    private float tempo;

    public TelaMenu(JogoPrincipal jogo) {
        super(jogo);
        float x = (LARGURA_MUNDO - LARGURA_BOTAO) / 2f;
        botoes.adicionar(new Botao("Jogar", x, TOPO_BOTOES, LARGURA_BOTAO, ALTURA_BOTAO, COR_PRINCIPAL,
                () -> jogo.irPara(new TelaSelecaoNivel(jogo))));
        Botao continuar = botoes.adicionar(new Botao("Continuar", x, TOPO_BOTOES - ESPACO_BOTOES, LARGURA_BOTAO,
                ALTURA_BOTAO, COR_SECUNDARIA, this::continuarPartida));
        continuar.setHabilitado(jogo.getArquivoSalvamento().existeSalvamento());
        botoes.adicionar(new Botao("Ranking", x, TOPO_BOTOES - 2 * ESPACO_BOTOES, LARGURA_BOTAO, ALTURA_BOTAO,
                COR_SECUNDARIA, () -> jogo.irPara(new TelaRanking(jogo))));
        botaoSom = botoes.adicionar(new Botao(textoSom(), x, TOPO_BOTOES - 3 * ESPACO_BOTOES, LARGURA_BOTAO,
                ALTURA_BOTAO, COR_SECUNDARIA, this::alternarSom));
        botoes.adicionar(new Botao("Sair", x, TOPO_BOTOES - 4 * ESPACO_BOTOES, LARGURA_BOTAO, ALTURA_BOTAO,
                COR_SECUNDARIA, () -> Gdx.app.exit()));
    }

    private void continuarPartida() {
        Optional<EstadoPartida> estado = jogo.getArquivoSalvamento().carregar();
        if (estado.isEmpty()) {
            erro = "Salvamento inválido ou ausente.";
            return;
        }
        try {
            Nivel nivel = jogo.getArquivoNivel().carregar(estado.get().numeroNivel());
            Partida partida = Partida.restaurar(nivel, estado.get(), jogo.getAleatorio());
            jogo.irPara(new TelaJogo(jogo, partida));
        } catch (IllegalArgumentException | IllegalStateException e) {
            Gdx.app.error("Menu", "Falha ao continuar partida salva", e);
            erro = "Não foi possível continuar: " + e.getMessage();
        }
    }

    private void alternarSom() {
        jogo.getSom().alternar();
        botaoSom.setTexto(textoSom());
    }

    private String textoSom() {
        return jogo.getSom().isAtivo() ? "Som: ligado" : "Som: desligado";
    }

    @Override
    protected void atualizar(float delta) {
        tempo += delta;
    }

    @Override
    protected void desenharFormas(ShapeRenderer formas) {
        TipoGema[] tipos = TipoGema.values();
        float espaco = LARGURA_MUNDO / (tipos.length + 1);
        for (int i = 0; i < tipos.length; i++) {
            float flutuar = MathUtils.sin(tempo * 2.2f + i * 0.9f) * 10f;
            decoracao.desenharGema(formas, tipos[i], espaco * (i + 1), 640f + flutuar, 0.85f, 1f);
        }
    }

    @Override
    protected void desenharTextos(SpriteBatch lote) {
        textoCentralizado(lote, jogo.getFontes().getTitulo(), "CandyMatch", LARGURA_MUNDO / 2f, 800f);
        jogo.getFontes().getPequena().setColor(0.85f, 0.8f, 0.95f, 1f);
        textoCentralizado(lote, jogo.getFontes().getPequena(), "Combine 3 ou mais gemas iguais",
                LARGURA_MUNDO / 2f, 712f);
        if (!erro.isEmpty()) {
            jogo.getFontes().getPequena().setColor(1f, 0.5f, 0.5f, 1f);
            textoCentralizado(lote, jogo.getFontes().getPequena(), erro, LARGURA_MUNDO / 2f, 70f);
        }
        jogo.getFontes().getPequena().setColor(Color.WHITE);
    }
}
