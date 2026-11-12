package br.ed1.candymatch.arquivos;

import br.ed1.candymatch.estruturas.ListaEncadeada;
import br.ed1.candymatch.logica.Ranking;
import br.ed1.candymatch.modelo.RegistroPontuacao;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * Lê e grava o ranking em texto, uma linha por jogador no formato {@code nome;pontos}.
 * Requisitos: Arquivos, Lista Encadeada (ranking carregado). A ordenação e o corte do top 10 ficam em {@link Ranking}.
 *
 * <p>O arquivo deve vir de {@code Gdx.files.local}: {@code Gdx.files.internal} é somente leitura.
 */
public class ArquivoRanking {

    public static final int MAXIMO_REGISTROS = 10;
    public static final String SEPARADOR = ";";

    private static final Logger LOG = Logger.getLogger(ArquivoRanking.class.getName());

    private final FileHandle arquivo;

    public ArquivoRanking(FileHandle arquivo) {
        this.arquivo = Objects.requireNonNull(arquivo, "arquivo do ranking não pode ser nulo");
    }

    /** Retorna lista vazia se o arquivo ainda não existir; linhas inválidas são ignoradas com log. */
    public ListaEncadeada<RegistroPontuacao> carregar() {
        ListaEncadeada<RegistroPontuacao> registros = new ListaEncadeada<>();
        if (!arquivo.exists()) {
            return registros;
        }
        String conteudo;
        try {
            conteudo = arquivo.readString("UTF-8");
        } catch (GdxRuntimeException e) {
            LOG.warning("Não foi possível ler o ranking em " + arquivo.path() + ": " + e.getMessage());
            return registros;
        }
        String[] linhas = conteudo.split("\\R");
        for (int i = 0; i < linhas.length; i++) {
            String linha = linhas[i].trim();
            if (linha.isEmpty()) {
                continue;
            }
            RegistroPontuacao registro = interpretarLinha(linha);
            if (registro == null) {
                LOG.warning("Linha " + (i + 1) + " do ranking ignorada (formato inválido): '" + linha + "'");
            } else {
                registros.adicionar(registro);
            }
        }
        return registros;
    }

    /** Sobrescreve o arquivo com uma linha por registro, na ordem da lista. */
    public void salvar(ListaEncadeada<RegistroPontuacao> ranking) {
        Objects.requireNonNull(ranking, "ranking a salvar não pode ser nulo");
        StringBuilder conteudo = new StringBuilder();
        for (RegistroPontuacao registro : ranking) {
            conteudo.append(registro.getNome()).append(SEPARADOR).append(registro.getPontos()).append('\n');
        }
        try {
            arquivo.writeString(conteudo.toString(), false, "UTF-8");
        } catch (GdxRuntimeException e) {
            throw new IllegalStateException("Não foi possível gravar o ranking em " + arquivo.path(), e);
        }
    }

    /** Carrega o ranking do arquivo já ordenado por pontos e limitado ao top {@link #MAXIMO_REGISTROS}. */
    public Ranking carregarRanking() {
        return new Ranking(carregar(), MAXIMO_REGISTROS);
    }

    /**
     * Insere o registro (mantendo só o recorde de cada jogador), ordena por pontos, corta o top 10 e salva.
     *
     * @return true se a pontuação entrou no ranking
     */
    public boolean registrar(RegistroPontuacao registro) {
        Objects.requireNonNull(registro, "registro não pode ser nulo");
        Ranking ranking = carregarRanking();
        boolean entrou = ranking.registrar(registro);
        if (entrou) {
            salvar(ranking.getRegistros());
        }
        return entrou;
    }

    /** O separador é proibido no nome porque quebraria o formato do arquivo. */
    public static String sanitizarNome(String nome) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        return nome.replace(SEPARADOR, "").trim();
    }

    private static RegistroPontuacao interpretarLinha(String linha) {
        int separador = linha.lastIndexOf(SEPARADOR);
        if (separador <= 0) {
            return null;
        }
        try {
            String nome = linha.substring(0, separador).trim();
            int pontos = Integer.parseInt(linha.substring(separador + 1).trim());
            return new RegistroPontuacao(nome, pontos);
        } catch (IllegalArgumentException e) {
            // NumberFormatException também é IllegalArgumentException: nome vazio ou pontos inválidos.
            return null;
        }
    }
}
