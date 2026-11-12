package br.ed1.candymatch.algoritmos;

import br.ed1.candymatch.modelo.RegistroPontuacao;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdenadorBuscadorTest {

    @Test
    void ordenaInteirosAleatoriosIgualAoJavaUtil() {
        Random aleatorio = new Random(3);
        for (int tamanho = 0; tamanho < 60; tamanho++) {
            Integer[] vetor = new Integer[tamanho];
            for (int i = 0; i < tamanho; i++) {
                vetor[i] = aleatorio.nextInt(50) - 25;
            }
            Integer[] esperado = vetor.clone();
            Arrays.sort(esperado);
            Ordenador.ordenar(vetor);
            assertArrayEquals(esperado, vetor);
        }
    }

    @Test
    void ordenaComComparadorDecrescente() {
        String[] vetor = {"b", "d", "a", "c"};
        Ordenador.ordenar(vetor, Comparator.reverseOrder());
        assertArrayEquals(new String[]{"d", "c", "b", "a"}, vetor);
    }

    @Test
    void ordenacaoEhEstavelParaPontuacoesIguais() {
        RegistroPontuacao ana = new RegistroPontuacao("Ana", 100);
        RegistroPontuacao bia = new RegistroPontuacao("Bia", 300);
        RegistroPontuacao caio = new RegistroPontuacao("Caio", 100);
        RegistroPontuacao[] vetor = {ana, bia, caio};
        Ordenador.ordenar(vetor, RegistroPontuacao.POR_PONTOS_DECRESCENTE);
        assertSame(bia, vetor[0]);
        assertSame(ana, vetor[1]);
        assertSame(caio, vetor[2]);
    }

    @Test
    void buscaBinariaEncontraTodosEAusentes() {
        Integer[] vetor = {1, 3, 5, 7, 9, 11};
        for (int i = 0; i < vetor.length; i++) {
            assertEquals(i, Buscador.buscaBinaria(vetor, vetor[i], Comparator.naturalOrder()));
        }
        assertEquals(Buscador.NAO_ENCONTRADO, Buscador.buscaBinaria(vetor, 4, Comparator.naturalOrder()));
        assertEquals(Buscador.NAO_ENCONTRADO, Buscador.buscaBinaria(vetor, 0, Comparator.naturalOrder()));
        assertEquals(Buscador.NAO_ENCONTRADO, Buscador.buscaBinaria(vetor, 12, Comparator.naturalOrder()));
        assertEquals(Buscador.NAO_ENCONTRADO,
                Buscador.buscaBinaria(new Integer[0], 1, Comparator.naturalOrder()));
    }

    @Test
    void buscaSequencialRetornaPrimeiraOcorrencia() {
        String[] vetor = {"x", "ab", "cd", "ef"};
        assertEquals(1, Buscador.buscaSequencial(vetor, s -> s.length() == 2));
        assertEquals(Buscador.NAO_ENCONTRADO, Buscador.buscaSequencial(vetor, s -> s.isEmpty()));
    }

    @Test
    void argumentosNulosLancamExcecao() {
        assertThrows(NullPointerException.class, () -> Ordenador.ordenar(null, Comparator.<Integer>naturalOrder()));
        assertThrows(NullPointerException.class,
                () -> Buscador.buscaBinaria(new Integer[]{1}, null, Comparator.naturalOrder()));
    }
}
