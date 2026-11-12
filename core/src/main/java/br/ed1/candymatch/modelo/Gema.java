package br.ed1.candymatch.modelo;

import java.util.Objects;

/**
 * Gema do tabuleiro. Imutável: isso permite que {@link Tabuleiro#copiar()} copie apenas as
 * referências, sem risco de o snapshot do desfazer ser alterado depois.
 */
public final class Gema {

    private final TipoGema tipo;

    public Gema(TipoGema tipo) {
        this.tipo = Objects.requireNonNull(tipo, "tipo da gema não pode ser nulo");
    }

    public TipoGema getTipo() {
        return tipo;
    }

    public boolean mesmoTipo(Gema outra) {
        return outra != null && outra.tipo == tipo;
    }

    @Override
    public String toString() {
        return tipo.name();
    }
}
