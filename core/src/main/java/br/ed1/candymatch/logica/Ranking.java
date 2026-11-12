package br.ed1.candymatch.logica;

import br.ed1.candymatch.algoritmos.Buscador;
import br.ed1.candymatch.algoritmos.Ordenador;
import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.modelo.RegistroPontuacao;

import java.util.Objects;
import java.util.Optional;

/**
 * Regras do ranking: um registro por jogador (o seu recorde), top N ordenado por pontos
 * e consulta de recorde por nome.
 * Requisitos: Lista Encadeada (armazenamento), Ordenação (MergeSort por pontos e por nome),
 * Busca (sequencial para atualizar recorde, binária para consultar por nome), Vetor, Generic.
 */
public class Ranking {

    private final int capacidade;
    private ListaEncadeada<RegistroPontuacao> registros;

    /** @param registros lista carregada do arquivo (pode estar fora de ordem) */
    public Ranking(ListaEncadeada<RegistroPontuacao> registros, int capacidade) {
        Objects.requireNonNull(registros, "lista de registros não pode ser nula");
        if (capacidade <= 0) {
            throw new IllegalArgumentException("Capacidade do ranking deve ser positiva: " + capacidade);
        }
        this.capacidade = capacidade;
        this.registros = registros;
        reorganizar();
    }

    /**
     * Registra a pontuação: se o jogador já existe, mantém o maior valor; depois reordena por pontos
     * e corta para a capacidade.
     *
     * @return true se a pontuação entrou no ranking (novo jogador no top ou recorde pessoal batido)
     */
    public boolean registrar(RegistroPontuacao novo) {
        Objects.requireNonNull(novo, "registro não pode ser nulo");
        RegistroPontuacao[] vetor = registros.paraVetor(RegistroPontuacao[]::new);
        int indiceExistente = Buscador.buscaSequencial(vetor, r -> r.getNome().equalsIgnoreCase(novo.getNome()));
        if (indiceExistente != Buscador.NAO_ENCONTRADO) {
            if (vetor[indiceExistente].getPontos() >= novo.getPontos()) {
                return false;
            }
            vetor[indiceExistente] = novo;
        } else {
            registros.adicionar(novo);
            vetor = registros.paraVetor(RegistroPontuacao[]::new);
        }
        registros = ListaEncadeada.deVetor(vetor);
        reorganizar();
        return registros.contem(novo);
    }

    /** Vetor ordenado por pontos (decrescente), pronto para exibição. */
    public RegistroPontuacao[] porPontos() {
        return registros.paraVetor(RegistroPontuacao[]::new);
    }

    /**
     * Consulta o recorde de um jogador por busca binária.
     * A busca binária exige ordem pelo mesmo critério da comparação, por isso ordena uma cópia por nome.
     */
    public Optional<RegistroPontuacao> buscarRecorde(String nome) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        if (nome.isBlank()) {
            return Optional.empty();
        }
        RegistroPontuacao[] porNome = registros.paraVetor(RegistroPontuacao[]::new);
        Ordenador.ordenar(porNome, RegistroPontuacao.POR_NOME);
        RegistroPontuacao alvo = new RegistroPontuacao(nome, 0);
        int indice = Buscador.buscaBinaria(porNome, alvo, RegistroPontuacao.POR_NOME);
        return indice == Buscador.NAO_ENCONTRADO ? Optional.empty() : Optional.of(porNome[indice]);
    }

    /** Posição (1 = primeiro) do jogador no ranking por pontos, ou 0 se não estiver nele. */
    public int posicaoDe(String nome) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        int indice = Buscador.buscaSequencial(porPontos(), r -> r.getNome().equalsIgnoreCase(nome.trim()));
        return indice + 1;
    }

    /** Lista interna ordenada por pontos; usada para gravar em arquivo. */
    public ListaEncadeada<RegistroPontuacao> getRegistros() {
        return registros;
    }

    public int tamanho() {
        return registros.tamanho();
    }

    /** Ordena por pontos (MergeSort) e descarta o que passar da capacidade. */
    private void reorganizar() {
        RegistroPontuacao[] vetor = registros.paraVetor(RegistroPontuacao[]::new);
        Ordenador.ordenar(vetor, RegistroPontuacao.POR_PONTOS_DECRESCENTE);
        ListaEncadeada<RegistroPontuacao> cortada = new ListaEncadeada<>();
        for (int i = 0; i < vetor.length && i < capacidade; i++) {
            cortada.adicionar(vetor[i]);
        }
        registros = cortada;
    }
}
