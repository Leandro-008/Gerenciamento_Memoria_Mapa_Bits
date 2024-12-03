import java.util.*;

public class GerenciamentoMemoria {
    static final int TAMANHO_MEMORIA = 32; 
    static int[] memoria = new int[TAMANHO_MEMORIA];
    static Map<String, int[]> alocacoes = new HashMap<>(); 
    static Map<Integer, List<Integer>> quickFitMap = new HashMap<>();

    public static void main(String[] args) {
        List<Processo> processos = List.of(
            new Processo("P1", 5),
            new Processo("P2", 4),
            new Processo("P3", 2),
            new Processo("P4", 5),
            new Processo("P5", 8),
            new Processo("P6", 3),
            new Processo("P7", 5),
            new Processo("P8", 8),
            new Processo("P9", 2),
            new Processo("P10", 6)
        );

        executarSimulacao("First Fit", processos, GerenciamentoMemoria::firstFit);
        executarSimulacao("Next Fit", processos, GerenciamentoMemoria::nextFit);
        executarSimulacao("Best Fit", processos, GerenciamentoMemoria::bestFit);
        executarSimulacao("Quick Fit", processos, GerenciamentoMemoria::quickFit);
        executarSimulacao("Worst Fit", processos, GerenciamentoMemoria::worstFit);
    }

    static class Processo {
        String id;
        int tamanho;

        public Processo(String id, int tamanho) {
            this.id = id;
            this.tamanho = tamanho;
        }
    }

    static int firstFit(Processo processo) {
        int tamanho = processo.tamanho;
        for (int i = 0; i <= TAMANHO_MEMORIA - tamanho; i++) {
            boolean livre = true;
            for (int j = 0; j < tamanho; j++) {
                if (memoria[i + j] == 1) {
                    livre = false;
                    break;
                }
            }
            if (livre) {
                alocarMemoria(i, tamanho, processo.id);
                return i;
            }
        }
        return -1;
    }

    static int ultimaPosicao = 0;

    static int nextFit(Processo processo) {
        int tamanho = processo.tamanho;
        for (int i = 0; i < TAMANHO_MEMORIA; i++) {
            int inicio = (ultimaPosicao + i) % TAMANHO_MEMORIA;
            boolean livre = true;
            for (int j = 0; j < tamanho; j++) {
                if (inicio + j >= TAMANHO_MEMORIA || memoria[inicio + j] == 1) {
                    livre = false;
                    break;
                }
            }
            if (livre) {
                alocarMemoria(inicio, tamanho, processo.id);
                ultimaPosicao = inicio + tamanho;
                return inicio;
            }
        }
        return -1;
    }

    static int bestFit(Processo processo) {
        int tamanho = processo.tamanho;
        int melhorInicio = -1;
        int menorEspaco = TAMANHO_MEMORIA + 1;

        for (int i = 0; i < TAMANHO_MEMORIA; i++) {
            if (memoria[i] == 0) {
                int inicio = i;
                while (i < TAMANHO_MEMORIA && memoria[i] == 0) i++;
                int espaco = i - inicio;
                if (espaco >= tamanho && espaco < menorEspaco) {
                    melhorInicio = inicio;
                    menorEspaco = espaco;
                }
            }
        }

        if (melhorInicio != -1) {
            alocarMemoria(melhorInicio, tamanho, processo.id);
            return melhorInicio;
        }
        return -1;
    }

    static int quickFit(Processo processo) {
        int tamanho = processo.tamanho;

        if (quickFitMap.containsKey(tamanho)) {
            List<Integer> blocos = quickFitMap.get(tamanho);
            if (!blocos.isEmpty()) {
                int inicio = blocos.remove(0); 
                alocarMemoria(inicio, tamanho, processo.id);
                return inicio;
            }
        }
        return firstFit(processo);
    }

    static int worstFit(Processo processo) {
        int tamanho = processo.tamanho;
        int piorInicio = -1;
        int maiorEspaco = -1;

        for (int i = 0; i < TAMANHO_MEMORIA; i++) {
            if (memoria[i] == 0) {
                int inicio = i;
                while (i < TAMANHO_MEMORIA && memoria[i] == 0) i++;
                int espaco = i - inicio;
                if (espaco >= tamanho && espaco > maiorEspaco) {
                    piorInicio = inicio;
                    maiorEspaco = espaco;
                }
            }
        }

        if (piorInicio != -1) {
            alocarMemoria(piorInicio, tamanho, processo.id);
            return piorInicio;
        }
        return -1;
    }

    static void alocarMemoria(int inicio, int tamanho, String id) {
        for (int i = inicio; i < inicio + tamanho; i++) {
            memoria[i] = 1;
        }
        alocacoes.put(id, new int[] { inicio, tamanho });
    }

    static boolean desalocarMemoria(String id) {
        if (alocacoes.containsKey(id)) {
            int[] alocacao = alocacoes.get(id);
            int inicio = alocacao[0];
            int tamanho = alocacao[1];

            for (int i = inicio; i < inicio + tamanho; i++) {
                memoria[i] = 0;
            }

            quickFitMap.computeIfAbsent(tamanho, k -> new ArrayList<>()).add(inicio);

            alocacoes.remove(id);
            return true;
        }
        return false;
    }

    static void executarSimulacao(String algoritmoNome, List<Processo> processos, Algoritmo algoritmo) {
        Arrays.fill(memoria, 0); 
        alocacoes.clear();
        quickFitMap.clear(); 

        System.out.println("\nSimulação: " + algoritmoNome);

        Random random = new Random();
        for (int i = 0; i < 30; i++) {
            Processo processo = processos.get(random.nextInt(processos.size()));

            if (alocacoes.containsKey(processo.id)) {
                desalocarMemoria(processo.id);
                System.out.println("Desalocado: " + processo.id + " | Memória: " + Arrays.toString(memoria));
            } else {
                int resultado = algoritmo.alocar(processo);
                if (resultado != -1) {
                    System.out.println("Alocado: " + processo.id + " no bloco " + resultado + " | Memória: " + Arrays.toString(memoria));
                } else {
                    System.out.println("Erro: Sem espaço para " + processo.id + " | Memória: " + Arrays.toString(memoria));
                }
            }
        }
    }

    @FunctionalInterface
    interface Algoritmo {
        int alocar(Processo processo);
    }
}
