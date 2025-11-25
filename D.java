import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class D {
    /* 
    // --- CONFIGURAÇÃO (Troque conforme necessário) ---
    // Modo LOCALHOST (para teste)
    private static final String[] IPS_RECEPTORES = {"localhost", "localhost", "localhost"};
    private static final int[] PORTAS_RECEPTORES = {12345, 12346, 12347};
    private static final int PORTA = -1;
    */
    // Modo REDE (comente o de cima e descomente este)
    
    private static final String[] IPS_RECEPTORES = {"192.168.15.27"};
    private static final int PORTA = 12345;
    private static final int[] PORTAS_RECEPTORES = null;

    public static void main(String[] args) {
        int tamanhoVetor = 0;
        try {
            System.out.print("Tamanho do vetor (ex: 100000): ");
            tamanhoVetor = Teclado.getUmInt();
        } catch (Exception e) {}
        if (tamanhoVetor <= 0) tamanhoVetor = 100_000;

        System.out.println("[D] Gerando vetor com " + String.format("%,d", tamanhoVetor) + " elementos...");
        byte[] vetorzao = new byte[tamanhoVetor];
        Random random = new Random();
        for (int i = 0; i < tamanhoVetor; i++) {
            vetorzao[i] = (byte)(random.nextInt(201) - 100);
        }

        System.out.println("\n[D] Iniciando ORDENAÇÃO DISTRIBUÍDA...");
        long inicio = System.currentTimeMillis();

        ArrayList<ThreadDoCliente> threads = new ArrayList<>();
        int numServidores = IPS_RECEPTORES.length;
        int tamBloco = (int) Math.ceil((double) tamanhoVetor / numServidores);

        try {
            // 1. Dividir e Enviar
            for (int i = 0; i < numServidores; i++) {
                int start = i * tamBloco;
                int end = Math.min(start + tamBloco, tamanhoVetor);
                if (start >= end) break;

                byte[] parte = Arrays.copyOfRange(vetorzao, start, end);
                
                String ip = IPS_RECEPTORES[i];
                int porta = (PORTA == -1) ? PORTAS_RECEPTORES[i] : PORTA;

                System.out.println("[D] Enviando " + parte.length + " números para " + ip + ":" + porta);
                ThreadDoCliente t = new ThreadDoCliente(ip, porta, parte);
                threads.add(t);
                t.start();
            }

            // 2. Esperar (Join) e fazer o MERGE FINAL
            byte[] vetorFinal = new byte[0]; // Começa vazio
            
            for (ThreadDoCliente t : threads) {
                t.join(); // Espera a thread terminar
                byte[] parteOrdenada = t.getVetorOrdenado(); // Pega o resultado
                
                System.out.println("[D] Recebido vetor ordenado de " + parteOrdenada.length + " posições. Intercalando...");
                // Aqui usamos o MergeUtils para juntar o que já tínhamos com a nova parte
                vetorFinal = MergeUtils.intercalar(vetorFinal, parteOrdenada);
            }

            long fim = System.currentTimeMillis();
            System.out.println("\n--- CONCLUÍDO ---");
            System.out.println("Tempo total: " + (fim - inicio) + " ms");
            
            // Verificação rápida (opcional)
            boolean estaOrdenado = true;
            for(int i=0; i<vetorFinal.length-1; i++){
                if(vetorFinal[i] > vetorFinal[i+1]) { estaOrdenado = false; break; }
            }
            System.out.println("Verificação de ordem: " + (estaOrdenado ? "OK (Ordenado)" : "ERRO (Não ordenado)"));

            // 3. Salvar em arquivo (Exigência do PDF)
            System.out.print("Nome do arquivo para salvar (ex: saida.txt): ");
            String nomeArquivo = Teclado.getUmString();
            PrintWriter writer = new PrintWriter(nomeArquivo);
            writer.println(Arrays.toString(vetorFinal));
            writer.close();
            System.out.println("Salvo em " + nomeArquivo);

            // 4. Encerrar Servidores
            for (ThreadDoCliente t : threads) t.enviarEncerramento();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}