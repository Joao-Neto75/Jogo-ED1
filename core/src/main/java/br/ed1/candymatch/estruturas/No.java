package br.ed1.candymatch.estruturas;

/**
 * Nó de encadeamento simples, base de {@link ListaEncadeada}, {@link Pilha} e {@link Fila}.
 * Requisito: Generic.
 */
public final class No<T> {

    private final T dado;
    private No<T> proximo;

    public No(T dado) {
        this.dado = dado;
    }

    public T getDado() {
        return dado;
    }

    public No<T> getProximo() {
        return proximo;
    }

    public void setProximo(No<T> proximo) {
        this.proximo = proximo;
    }
}
