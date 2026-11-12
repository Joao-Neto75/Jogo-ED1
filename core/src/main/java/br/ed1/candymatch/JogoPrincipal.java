package br.ed1.candymatch;

import br.ed1.candymatch.arquivos.ArquivoNivel;
import br.ed1.candymatch.arquivos.ArquivoRanking;
import br.ed1.candymatch.arquivos.ArquivoSalvamento;
import br.ed1.candymatch.telas.TelaMenu;
import br.ed1.candymatch.visual.Fontes;
import br.ed1.candymatch.visual.GerenciadorSom;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Random;

/**
 * Classe principal do libGDX: cria os recursos compartilhados (desenho, fontes, sons, arquivos)
 * uma única vez e troca entre as telas.
 */
public class JogoPrincipal extends Game {

    private static final String PASTA_NIVEIS = "niveis";
    private static final String ARQUIVO_RANKING = "ranking.txt";
    private static final String ARQUIVO_SALVAMENTO = "salvamento.txt";

    private SpriteBatch lote;
    private ShapeRenderer formas;
    private Fontes fontes;
    private GerenciadorSom som;
    private ArquivoNivel arquivoNivel;
    private ArquivoRanking arquivoRanking;
    private ArquivoSalvamento arquivoSalvamento;
    private Random aleatorio;

    @Override
    public void create() {
        lote = new SpriteBatch();
        formas = new ShapeRenderer();
        fontes = new Fontes();
        som = new GerenciadorSom();
        // internal = somente leitura (assets); local = gravável (ranking e salvamento).
        arquivoNivel = new ArquivoNivel(Gdx.files.internal(PASTA_NIVEIS));
        arquivoRanking = new ArquivoRanking(Gdx.files.local(ARQUIVO_RANKING));
        arquivoSalvamento = new ArquivoSalvamento(Gdx.files.local(ARQUIVO_SALVAMENTO));
        aleatorio = new Random();
        setScreen(new TelaMenu(this));
    }

    /** Troca de tela liberando os recursos da anterior. */
    public void irPara(Screen novaTela) {
        Screen anterior = getScreen();
        setScreen(novaTela);
        if (anterior != null) {
            anterior.dispose();
        }
    }

    public SpriteBatch getLote() {
        return lote;
    }

    public ShapeRenderer getFormas() {
        return formas;
    }

    public Fontes getFontes() {
        return fontes;
    }

    public GerenciadorSom getSom() {
        return som;
    }

    public ArquivoNivel getArquivoNivel() {
        return arquivoNivel;
    }

    public ArquivoRanking getArquivoRanking() {
        return arquivoRanking;
    }

    public ArquivoSalvamento getArquivoSalvamento() {
        return arquivoSalvamento;
    }

    public Random getAleatorio() {
        return aleatorio;
    }

    @Override
    public void dispose() {
        Screen atual = getScreen();
        if (atual != null) {
            atual.hide();
            atual.dispose();
        }
        lote.dispose();
        formas.dispose();
        fontes.dispose();
        som.dispose();
    }
}
