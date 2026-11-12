package br.ed1.candymatch.visual;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Carrega e toca os efeitos sonoros. Um som ausente ou falha no áudio não derruba o jogo:
 * o erro é registrado no log e o som é ignorado.
 */
public class GerenciadorSom implements Disposable {

    public enum Som {
        TROCA("troca.wav"),
        COMBINACAO("combinacao.wav"),
        INVALIDA("invalida.wav"),
        CLIQUE("clique.wav"),
        DICA("dica.wav"),
        DESFAZER("desfazer.wav"),
        VITORIA("vitoria.wav"),
        DERROTA("derrota.wav"),
        EMBARALHAR("embaralhar.wav");

        private final String arquivo;

        Som(String arquivo) {
            this.arquivo = arquivo;
        }
    }

    private static final String PASTA = "sons/";
    private static final float VOLUME = 0.6f;

    private final Map<Som, Sound> sons = new EnumMap<>(Som.class);
    private boolean ativo = true;

    public GerenciadorSom() {
        for (Som som : Som.values()) {
            try {
                sons.put(som, Gdx.audio.newSound(Gdx.files.internal(PASTA + som.arquivo)));
            } catch (GdxRuntimeException e) {
                Gdx.app.error("Som", "Não foi possível carregar " + PASTA + som.arquivo + "; som desativado", e);
            }
        }
    }

    public void tocar(Som som) {
        tocar(som, 1f);
    }

    /** @param tom 1 = normal; maior que 1 deixa mais agudo (usado para subir o tom nas cascatas) */
    public void tocar(Som som, float tom) {
        Objects.requireNonNull(som, "som não pode ser nulo");
        Sound sound = sons.get(som);
        if (ativo && sound != null) {
            sound.play(VOLUME, tom, 0f);
        }
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void alternar() {
        ativo = !ativo;
    }

    @Override
    public void dispose() {
        for (Sound sound : sons.values()) {
            sound.dispose();
        }
        sons.clear();
    }
}
