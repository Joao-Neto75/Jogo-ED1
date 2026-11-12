package br.ed1.candymatch.lwjgl3;

import br.ed1.candymatch.JogoPrincipal;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Ponto de entrada da versão desktop. */
public final class Lwjgl3Launcher {

    private static final int LARGURA_JANELA = 800;
    private static final int ALTURA_JANELA = 900;
    private static final int FPS = 60;

    private Lwjgl3Launcher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("CandyMatch");
        config.setWindowedMode(LARGURA_JANELA, ALTURA_JANELA);
        config.useVsync(true);
        config.setForegroundFPS(FPS);
        new Lwjgl3Application(new JogoPrincipal(), config);
    }
}
