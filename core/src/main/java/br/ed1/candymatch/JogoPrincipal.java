package br.ed1.candymatch;

import br.ed1.candymatch.arquivos.CarregadorNivel;
import br.ed1.candymatch.arquivos.GerenciadorArquivoRanking;
import br.ed1.candymatch.arquivos.GerenciadorSalvamento;
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
    private CarregadorNivel carregadorNivel;
    private GerenciadorArquivoRanking arquivoRanking;
    private GerenciadorSalvamento salvamento;
    private Random aleatorio;

    @Override
    public void create() {
        lote = new SpriteBatch();
        formas = new ShapeRenderer();
        fontes = new Fontes();
        som = new GerenciadorSom();
        // internal = somente leitura (assets); local = gravável (ranking e salvamento).
        carregadorNivel = new CarregadorNivel(Gdx.files.internal(PASTA_NIVEIS));
        arquivoRanking = new GerenciadorArquivoRanking(Gdx.files.local(ARQUIVO_RANKING));
        salvamento = new GerenciadorSalvamento(Gdx.files.local(ARQUIVO_SALVAMENTO));
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

    public CarregadorNivel getCarregadorNivel() {
        return carregadorNivel;
    }

    public GerenciadorArquivoRanking getArquivoRanking() {
        return arquivoRanking;
    }

    public GerenciadorSalvamento getSalvamento() {
        return salvamento;
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
