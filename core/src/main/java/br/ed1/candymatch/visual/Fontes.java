package br.ed1.candymatch.visual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Fontes do jogo geradas a partir de um TTF (FreeType), em tamanhos fixos.
 * O TTF faz parte dos assets: se faltar, o jogo falha logo na abertura com mensagem clara.
 */
public class Fontes implements Disposable {

    private static final String CAMINHO_TTF = "fontes/NotoSans-Bold.ttf";
    private static final String CARACTERES_EXTRAS = "ÁÉÍÓÚÂÊÔÃÕÇÀáéíóúâêôãõçà·×–—•";

    public static final int TAMANHO_TITULO = 72;
    public static final int TAMANHO_GRANDE = 40;
    public static final int TAMANHO_MEDIO = 28;
    public static final int TAMANHO_PEQUENO = 20;

    private final BitmapFont titulo;
    private final BitmapFont grande;
    private final BitmapFont media;
    private final BitmapFont pequena;

    public Fontes() {
        FileHandle ttf = Gdx.files.internal(CAMINHO_TTF);
        FreeTypeFontGenerator gerador = null;
        try {
            gerador = new FreeTypeFontGenerator(ttf);
            titulo = gerar(gerador, TAMANHO_TITULO, 3);
            grande = gerar(gerador, TAMANHO_GRANDE, 2);
            media = gerar(gerador, TAMANHO_MEDIO, 0);
            pequena = gerar(gerador, TAMANHO_PEQUENO, 0);
        } catch (GdxRuntimeException e) {
            throw new IllegalStateException("Não foi possível carregar a fonte " + CAMINHO_TTF, e);
        } finally {
            if (gerador != null) {
                gerador.dispose();
            }
        }
    }

    private static BitmapFont gerar(FreeTypeFontGenerator gerador, int tamanho, int sombra) {
        FreeTypeFontGenerator.FreeTypeFontParameter parametros = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parametros.size = tamanho;
        parametros.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CARACTERES_EXTRAS;
        parametros.minFilter = Texture.TextureFilter.Linear;
        parametros.magFilter = Texture.TextureFilter.Linear;
        if (sombra > 0) {
            parametros.shadowOffsetX = sombra;
            parametros.shadowOffsetY = sombra;
            parametros.shadowColor = new com.badlogic.gdx.graphics.Color(0f, 0f, 0f, 0.45f);
        }
        return gerador.generateFont(parametros);
    }

    public BitmapFont getTitulo() {
        return titulo;
    }

    public BitmapFont getGrande() {
        return grande;
    }

    public BitmapFont getMedia() {
        return media;
    }

    public BitmapFont getPequena() {
        return pequena;
    }

    @Override
    public void dispose() {
        titulo.dispose();
        grande.dispose();
        media.dispose();
        pequena.dispose();
    }
}
