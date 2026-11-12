package br.ed1.candymatch.visual;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Primitivas preenchidas que o ShapeRenderer não oferece prontas (retângulo arredondado, polígono, estrela).
 * Todas exigem o ShapeRenderer já iniciado em {@code ShapeType.Filled}.
 */
public final class Formas {

    private Formas() {
    }

    /**
     * Os cantos são setores de 90° (não círculos inteiros) para que nenhuma parte seja pintada duas vezes:
     * com cor translúcida, sobreposições apareceriam como manchas mais escuras.
     */
    public static void retanguloArredondado(ShapeRenderer sr, float x, float y, float largura, float altura,
                                            float raio) {
        float r = Math.min(raio, Math.min(largura, altura) / 2f);
        int segmentos = Math.max(4, (int) (r / 2f));
        sr.rect(x + r, y, largura - 2 * r, altura);
        sr.rect(x, y + r, r, altura - 2 * r);
        sr.rect(x + largura - r, y + r, r, altura - 2 * r);
        sr.arc(x + r, y + r, r, 180f, 90f, segmentos);
        sr.arc(x + largura - r, y + r, r, 270f, 90f, segmentos);
        sr.arc(x + largura - r, y + altura - r, r, 0f, 90f, segmentos);
        sr.arc(x + r, y + altura - r, r, 90f, 90f, segmentos);
    }

    /** Polígono regular desenhado como leque de triângulos a partir do centro. */
    public static void poligono(ShapeRenderer sr, float cx, float cy, float raio, int lados, float rotacaoGraus) {
        float passo = 360f / lados;
        for (int i = 0; i < lados; i++) {
            float a1 = rotacaoGraus + i * passo;
            float a2 = a1 + passo;
            sr.triangle(cx, cy,
                    cx + raio * MathUtils.cosDeg(a1), cy + raio * MathUtils.sinDeg(a1),
                    cx + raio * MathUtils.cosDeg(a2), cy + raio * MathUtils.sinDeg(a2));
        }
    }

    public static void estrela(ShapeRenderer sr, float cx, float cy, float raioExterno, float raioInterno, int pontas) {
        int vertices = pontas * 2;
        float passo = 360f / vertices;
        for (int i = 0; i < vertices; i++) {
            float a1 = 90f + i * passo;
            float a2 = a1 + passo;
            float r1 = i % 2 == 0 ? raioExterno : raioInterno;
            float r2 = i % 2 == 0 ? raioInterno : raioExterno;
            sr.triangle(cx, cy,
                    cx + r1 * MathUtils.cosDeg(a1), cy + r1 * MathUtils.sinDeg(a1),
                    cx + r2 * MathUtils.cosDeg(a2), cy + r2 * MathUtils.sinDeg(a2));
        }
    }
}
