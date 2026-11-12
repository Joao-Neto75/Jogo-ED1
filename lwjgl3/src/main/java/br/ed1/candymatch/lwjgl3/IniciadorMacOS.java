package br.ed1.candymatch.lwjgl3;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * No macOS o GLFW exige que a JVM seja iniciada com {@code -XstartOnFirstThread}; sem isso, um
 * {@code java -jar CandyMatch.jar} trava ou fecha. Esta classe reinicia a JVM com a flag quando necessário,
 * para o jogo abrir igual em Windows, Linux e macOS. Nos demais sistemas não faz nada.
 */
final class IniciadorMacOS {

    /** Variável que a JVM do macOS define quando foi iniciada com -XstartOnFirstThread. */
    private static final String PREFIXO_VARIAVEL = "JAVA_STARTED_ON_FIRST_THREAD_";
    /** Evita laço infinito caso a flag não tenha efeito por algum motivo. */
    private static final String PROPRIEDADE_REINICIADO = "candymatch.reiniciado";

    private IniciadorMacOS() {
    }

    /**
     * @return true se uma nova JVM foi aberta (o processo atual deve apenas terminar);
     *         false se o jogo pode continuar neste processo
     */
    static boolean reiniciarSeNecessario(Class<?> classePrincipal, String[] args) {
        String sistema = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!sistema.contains("mac") || Boolean.getBoolean(PROPRIEDADE_REINICIADO)) {
            return false;
        }
        long pid = ProcessHandle.current().pid();
        if ("1".equals(System.getenv(PREFIXO_VARIAVEL + pid))) {
            return false;
        }

        String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        List<String> comando = new ArrayList<>();
        comando.add(java);
        comando.add("-XstartOnFirstThread");
        comando.add("-D" + PROPRIEDADE_REINICIADO + "=true");
        comando.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
        comando.add("-cp");
        comando.add(System.getProperty("java.class.path"));
        comando.add(classePrincipal.getName());
        comando.addAll(List.of(args));
        try {
            Process processo = new ProcessBuilder(comando).inheritIO().start();
            processo.waitFor();
            return true;
        } catch (IOException e) {
            System.err.println("Não foi possível reiniciar a JVM com -XstartOnFirstThread: " + e.getMessage()
                    + ". Rode manualmente: java -XstartOnFirstThread -jar CandyMatch.jar");
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }
}
