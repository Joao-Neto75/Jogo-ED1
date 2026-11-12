package br.ed1.candymatch.arquivos;

import br.ed1.candymatch.modelo.Nivel;
import br.ed1.candymatch.modelo.TipoGema;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Objects;

/**
 * Lê a configuração de um nível de {@code niveis/nivelN.txt}, no formato chave=valor:
 * <pre>
 * metaPontos=1000
 * limiteJogadas=20
 * tipos=VERMELHA,AZUL,VERDE,AMARELA,ROXA
 * </pre>
 * Linhas vazias e iniciadas por '#' são ignoradas.
 * Requisitos: Arquivos, Vetor (tipos de gema).
 */
public class CarregadorNivel {

    private static final String CHAVE_META = "metaPontos";
    private static final String CHAVE_JOGADAS = "limiteJogadas";
    private static final String CHAVE_TIPOS = "tipos";
    private static final String PREFIXO_COMENTARIO = "#";

    private final FileHandle pastaNiveis;

    /** @param pastaNiveis normalmente {@code Gdx.files.internal("niveis")} */
    public CarregadorNivel(FileHandle pastaNiveis) {
        this.pastaNiveis = Objects.requireNonNull(pastaNiveis, "pasta de níveis não pode ser nula");
    }

    /** @throws IllegalArgumentException se o arquivo não existir ou tiver formato inválido */
    public Nivel carregar(int numero) {
        FileHandle arquivo = arquivoDo(numero);
        if (!arquivo.exists()) {
            throw new IllegalArgumentException("Arquivo do nível " + numero + " não encontrado: " + arquivo.path());
        }
        String conteudo;
        try {
            conteudo = arquivo.readString("UTF-8");
        } catch (GdxRuntimeException e) {
            throw new IllegalArgumentException("Falha ao ler " + arquivo.path() + ": " + e.getMessage(), e);
        }
        return interpretar(numero, conteudo, arquivo.path());
    }

    public boolean existeNivel(int numero) {
        return numero > 0 && arquivoDo(numero).exists();
    }

    /** Quantidade de níveis consecutivos disponíveis a partir do 1. */
    public int contarNiveis() {
        int quantidade = 0;
        while (existeNivel(quantidade + 1)) {
            quantidade++;
        }
        return quantidade;
    }

    private FileHandle arquivoDo(int numero) {
        return pastaNiveis.child("nivel" + numero + ".txt");
    }

    private Nivel interpretar(int numero, String conteudo, String origem) {
        Integer meta = null;
        Integer jogadas = null;
        TipoGema[] tipos = null;
        String[] linhas = conteudo.split("\\R");
        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i].trim();
            if (linha.isEmpty() || linha.startsWith(PREFIXO_COMENTARIO)) {
                continue;
            }
            int separador = linha.indexOf('=');
            if (separador <= 0) {
                throw erro(origem, i, "esperado chave=valor, encontrado '" + linha + "'");
            }
            String chave = linha.substring(0, separador).trim();
            String valor = linha.substring(separador + 1).trim();
            switch (chave) {
                case CHAVE_META -> meta = inteiro(valor, origem, i);
                case CHAVE_JOGADAS -> jogadas = inteiro(valor, origem, i);
                case CHAVE_TIPOS -> tipos = tipos(valor, origem, i);
                default -> throw erro(origem, i, "chave desconhecida '" + chave + "'");
            }
        }
        if (meta == null || jogadas == null || tipos == null) {
            throw new IllegalArgumentException(origem + ": faltam chaves obrigatórias ("
                    + CHAVE_META + ", " + CHAVE_JOGADAS + ", " + CHAVE_TIPOS + ")");
        }
        return new Nivel(numero, meta, jogadas, tipos);
    }

    private static int inteiro(String valor, String origem, int indiceLinha) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw erro(origem, indiceLinha, "número inválido '" + valor + "'");
        }
    }

    private static TipoGema[] tipos(String valor, String origem, int indiceLinha) {
        String[] nomes = valor.split(",");
        TipoGema[] tipos = new TipoGema[nomes.length];
        for (int i = 0; i < nomes.length; i++) {
            try {
                tipos[i] = TipoGema.valueOf(nomes[i].trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw erro(origem, indiceLinha, "tipo de gema desconhecido '" + nomes[i].trim() + "'");
            }
        }
        return tipos;
    }

    private static IllegalArgumentException erro(String origem, int indiceLinha, String mensagem) {
        return new IllegalArgumentException(origem + ", linha " + (indiceLinha + 1) + ": " + mensagem);
    }
}
