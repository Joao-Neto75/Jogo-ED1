package br.ed1.candymatch.arquivos;

import br.ed1.candymatch.modelo.EstadoPartida;
import br.ed1.candymatch.modelo.Gema;
import br.ed1.candymatch.modelo.Posicao;
import br.ed1.candymatch.modelo.Tabuleiro;
import br.ed1.candymatch.modelo.TipoGema;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Salva e carrega uma partida em andamento (nível, pontos, jogadas restantes e a matriz).
 * Requisitos: Arquivos, Vetor ou Matriz (o tabuleiro é gravado linha a linha).
 *
 * <p>Formato: primeira linha {@code nivel;pontos;jogadas}, depois uma linha por linha da matriz
 * com o símbolo de cada gema ({@link TipoGema#getSimbolo()}), '.' para vazio. Ex.:
 * <pre>
 * 1;340;17
 * VAEMRVAE
 * ...
 * </pre>
 *
 * <p>O arquivo deve vir de {@code Gdx.files.local}: {@code Gdx.files.internal} é somente leitura.
 */
public class GerenciadorSalvamento {

    private static final String SEPARADOR = ";";
    private static final char VAZIO = '.';
    private static final int CAMPOS_CABECALHO = 3;

    private static final Logger LOG = Logger.getLogger(GerenciadorSalvamento.class.getName());

    private final FileHandle arquivo;

    public GerenciadorSalvamento(FileHandle arquivo) {
        this.arquivo = Objects.requireNonNull(arquivo, "arquivo de salvamento não pode ser nulo");
    }

    public void salvar(EstadoPartida estado) {
        Objects.requireNonNull(estado, "estado a salvar não pode ser nulo");
        Tabuleiro tabuleiro = estado.tabuleiro();
        StringBuilder sb = new StringBuilder();
        sb.append(estado.numeroNivel()).append(SEPARADOR)
                .append(estado.pontos()).append(SEPARADOR)
                .append(estado.jogadasRestantes()).append('\n');
        for (int linha = 0; linha < tabuleiro.getLinhas(); linha++) {
            for (int coluna = 0; coluna < tabuleiro.getColunas(); coluna++) {
                Gema gema = tabuleiro.obterGema(new Posicao(linha, coluna));
                sb.append(gema == null ? VAZIO : gema.getTipo().getSimbolo());
            }
            sb.append('\n');
        }
        try {
            arquivo.writeString(sb.toString(), false, "UTF-8");
        } catch (GdxRuntimeException e) {
            throw new IllegalStateException("Não foi possível gravar o salvamento em " + arquivo.path(), e);
        }
    }

    /** Retorna vazio se não houver salvamento ou se o arquivo estiver corrompido (com log). */
    public Optional<EstadoPartida> carregar() {
        if (!arquivo.exists()) {
            return Optional.empty();
        }
        try {
            return Optional.of(interpretar(arquivo.readString("UTF-8")));
        } catch (GdxRuntimeException | IllegalArgumentException e) {
            LOG.warning("Salvamento ignorado (" + arquivo.path() + "): " + e.getMessage());
            return Optional.empty();
        }
    }

    public boolean existeSalvamento() {
        return arquivo.exists();
    }

    public void apagar() {
        if (arquivo.exists() && !arquivo.delete()) {
            throw new IllegalStateException("Não foi possível apagar o salvamento: " + arquivo.path());
        }
    }

    private static EstadoPartida interpretar(String conteudo) {
        String[] linhas = conteudo.trim().split("\\R");
        if (linhas.length < 2) {
            throw new IllegalArgumentException("arquivo sem tabuleiro");
        }
        String[] cabecalho = linhas[0].split(SEPARADOR);
        if (cabecalho.length != CAMPOS_CABECALHO) {
            throw new IllegalArgumentException("cabeçalho deve ter " + CAMPOS_CABECALHO + " campos: " + linhas[0]);
        }
        int nivel = Integer.parseInt(cabecalho[0].trim());
        int pontos = Integer.parseInt(cabecalho[1].trim());
        int jogadas = Integer.parseInt(cabecalho[2].trim());

        int quantidadeLinhas = linhas.length - 1;
        int quantidadeColunas = linhas[1].trim().length();
        Tabuleiro tabuleiro = new Tabuleiro(quantidadeLinhas, quantidadeColunas);
        for (int linha = 0; linha < quantidadeLinhas; linha++) {
            String texto = linhas[linha + 1].trim();
            if (texto.length() != quantidadeColunas) {
                throw new IllegalArgumentException("linha " + (linha + 2) + " com tamanho diferente das demais");
            }
            for (int coluna = 0; coluna < quantidadeColunas; coluna++) {
                char simbolo = texto.charAt(coluna);
                if (simbolo != VAZIO) {
                    tabuleiro.definirGema(new Posicao(linha, coluna), new Gema(TipoGema.deSimbolo(simbolo)));
                }
            }
        }
        return new EstadoPartida(tabuleiro, nivel, pontos, jogadas);
    }
}
