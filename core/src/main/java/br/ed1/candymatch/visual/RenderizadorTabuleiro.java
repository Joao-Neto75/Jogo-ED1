package br.ed1.candymatch.visual;

import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.TipoGema;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Objects;
import java.util.Optional;

/**
 * Desenha o tabuleiro e as gemas, e converte coordenadas de tela em {@link Posicao}.
 * Concentra a geometria (origem e tamanho da célula) para desenho e clique usarem a mesma conta.
 *
 * <p>Cada tipo tem cor E forma diferentes, para o jogo ser jogável também por daltônicos.
 */
public class RenderizadorTabuleiro {

    private static final float PROPORCAO_GEMA = 0.40f;
    private static final float MARGEM_MOLDURA = 12f;
    private static final float RAIO_MOLDURA = 22f;
    private static final Color COR_MOLDURA = new Color(0.08f, 0.04f, 0.14f, 0.75f);
    private static final Color COR_CELULA_CLARA = new Color(1f, 1f, 1f, 0.10f);
    private static final Color COR_CELULA_ESCURA = new Color(1f, 1f, 1f, 0.04f);
    private static final Color COR_SOMBRA_GEMA = new Color(0f, 0f, 0f, 0.28f);

    private final float origemX;
    private final float origemY;
    private final float tamanhoCelula;
    private final int linhas;
    private final int colunas;
    private final Color auxiliar = new Color();

    /** @param origemX canto inferior esquerdo do tabuleiro, em coordenadas de mundo */
    public RenderizadorTabuleiro(float origemX, float origemY, float tamanhoCelula, int linhas, int colunas) {
        if (tamanhoCelula <= 0) {
            throw new IllegalArgumentException("Tamanho da célula deve ser positivo: " + tamanhoCelula);
        }
        if (linhas <= 0 || colunas <= 0) {
            throw new IllegalArgumentException("Dimensões inválidas: " + linhas + "x" + colunas);
        }
        this.origemX = origemX;
        this.origemY = origemY;
        this.tamanhoCelula = tamanhoCelula;
        this.linhas = linhas;
        this.colunas = colunas;
    }

    public void desenharFundo(ShapeRenderer sr) {
        sr.setColor(COR_MOLDURA);
        Formas.retanguloArredondado(sr, origemX - MARGEM_MOLDURA, origemY - MARGEM_MOLDURA,
                colunas * tamanhoCelula + 2 * MARGEM_MOLDURA, linhas * tamanhoCelula + 2 * MARGEM_MOLDURA,
                RAIO_MOLDURA);
        for (int linha = 0; linha < linhas; linha++) {
            for (int coluna = 0; coluna < colunas; coluna++) {
                sr.setColor((linha + coluna) % 2 == 0 ? COR_CELULA_CLARA : COR_CELULA_ESCURA);
                sr.rect(origemX + coluna * tamanhoCelula + 1, yBase(linha) + 1, tamanhoCelula - 2, tamanhoCelula - 2);
            }
        }
    }

    /** Destaque translúcido em uma célula (seleção, dica). */
    public void desenharDestaque(ShapeRenderer sr, Posicao posicao, Color cor, float alfa) {
        Objects.requireNonNull(posicao, "posição não pode ser nula");
        sr.setColor(cor.r, cor.g, cor.b, alfa);
        Formas.retanguloArredondado(sr, origemX + posicao.coluna() * tamanhoCelula + 3, yBase(posicao.linha()) + 3,
                tamanhoCelula - 6, tamanhoCelula - 6, 12f);
    }

    /**
     * Desenha uma gema centrada em (cx, cy).
     *
     * @param escala 1 = tamanho normal (usado para encolher na remoção)
     * @param alfa   opacidade (usado para gemas surgindo)
     */
    public void desenharGema(ShapeRenderer sr, TipoGema tipo, float cx, float cy, float escala, float alfa) {
        Objects.requireNonNull(tipo, "tipo da gema não pode ser nulo");
        if (escala <= 0f || alfa <= 0f) {
            return;
        }
        float raio = tamanhoCelula * PROPORCAO_GEMA * escala;
        Color base = corDe(tipo);

        sr.setColor(COR_SOMBRA_GEMA.r, COR_SOMBRA_GEMA.g, COR_SOMBRA_GEMA.b, COR_SOMBRA_GEMA.a * alfa);
        desenharForma(sr, tipo, cx + raio * 0.06f, cy - raio * 0.12f, raio);

        auxiliar.set(base).mul(0.72f, 0.72f, 0.72f, 1f);
        sr.setColor(auxiliar.r, auxiliar.g, auxiliar.b, alfa);
        desenharForma(sr, tipo, cx, cy, raio);

        sr.setColor(base.r, base.g, base.b, alfa);
        desenharForma(sr, tipo, cx, cy + raio * 0.05f, raio * 0.86f);

        auxiliar.set(base).lerp(Color.WHITE, 0.35f);
        sr.setColor(auxiliar.r, auxiliar.g, auxiliar.b, alfa);
        desenharForma(sr, tipo, cx - raio * 0.05f, cy + raio * 0.14f, raio * 0.5f);

        sr.setColor(1f, 1f, 1f, 0.75f * alfa);
        sr.circle(cx - raio * 0.32f, cy + raio * 0.36f, raio * 0.13f);
    }

    private static void desenharForma(ShapeRenderer sr, TipoGema tipo, float cx, float cy, float raio) {
        switch (tipo) {
            case VERMELHA -> sr.circle(cx, cy, raio, 32);
            case AZUL -> Formas.retanguloArredondado(sr, cx - raio * 0.88f, cy - raio * 0.88f,
                    raio * 1.76f, raio * 1.76f, raio * 0.4f);
            case VERDE -> Formas.poligono(sr, cx, cy - raio * 0.15f, raio * 1.15f, 3, 90f);
            case AMARELA -> Formas.estrela(sr, cx, cy, raio * 1.1f, raio * 0.52f, 5);
            case ROXA -> Formas.poligono(sr, cx, cy, raio, 6, 0f);
            case LARANJA -> Formas.poligono(sr, cx, cy, raio * 1.12f, 4, 90f);
        }
    }

    public static Color corDe(TipoGema tipo) {
        return switch (tipo) {
            case VERMELHA -> new Color(0.93f, 0.22f, 0.27f, 1f);
            case AZUL -> new Color(0.2f, 0.55f, 0.98f, 1f);
            case VERDE -> new Color(0.25f, 0.82f, 0.36f, 1f);
            case AMARELA -> new Color(1f, 0.84f, 0.18f, 1f);
            case ROXA -> new Color(0.68f, 0.35f, 0.95f, 1f);
            case LARANJA -> new Color(1f, 0.55f, 0.12f, 1f);
        };
    }

    /** Converte um ponto da tela (coordenadas de mundo) na célula correspondente, se houver. */
    public Optional<Posicao> posicaoEm(float x, float y) {
        if (x < origemX || y < origemY) {
            return Optional.empty();
        }
        int coluna = (int) ((x - origemX) / tamanhoCelula);
        int linhaDeBaixo = (int) ((y - origemY) / tamanhoCelula);
        if (coluna >= colunas || linhaDeBaixo >= linhas) {
            return Optional.empty();
        }
        // No libGDX o eixo Y cresce para cima, mas a linha 0 do tabuleiro é o topo.
        return Optional.of(new Posicao(linhas - 1 - linhaDeBaixo, coluna));
    }

    public float centroX(float coluna) {
        return origemX + (coluna + 0.5f) * tamanhoCelula;
    }

    /** Aceita linha fracionária/negativa, usada nas animações de queda vindo de cima do tabuleiro. */
    public float centroY(float linha) {
        return origemY + (linhas - 1 - linha + 0.5f) * tamanhoCelula;
    }

    public float getTamanhoCelula() {
        return tamanhoCelula;
    }

    public float getTopo() {
        return origemY + linhas * tamanhoCelula;
    }

    private float yBase(int linha) {
        return origemY + (linhas - 1 - linha) * tamanhoCelula;
    }
}
