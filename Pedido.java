import java.io.Serializable;
import java.util.Arrays;

public class Pedido extends Comunicado implements Serializable {
    private byte[] numeros; // O vetor desordenado

    public Pedido(byte[] numeros) {
        this.numeros = numeros;
    }

    // O trabalho pede que o R use threads igual ao numero de processadores 
    public byte[] ordenar() {
        int numProcessadores = Runtime.getRuntime().availableProcessors();
        
        // Se o vetor for muito pequeno ou tiver só 1 processador, vai sequencial
        if (numProcessadores <= 1 || this.numeros.length < 1000) {
            byte[] ordenado = this.numeros.clone();
            MergeUtils.mergeSortSequencial(ordenado, 0, ordenado.length - 1);
            return ordenado;
        }

        // Divisão do trabalho para as Threads locais do Receptor
        int tamanhoBloco = this.numeros.length / numProcessadores;
        ThreadOrdenadora[] threads = new ThreadOrdenadora[numProcessadores];
        byte[][] resultadosParciais = new byte[numProcessadores][];

        // 1. Cria e inicia as threads (Cada uma ordena um pedaço)
        for (int i = 0; i < numProcessadores; i++) {
            int inicio = i * tamanhoBloco;
            int fim = (i == numProcessadores - 1) ? this.numeros.length : (i + 1) * tamanhoBloco;
            
            // Copia o pedaço para não dar conflito de memória
            byte[] parte = Arrays.copyOfRange(this.numeros, inicio, fim);
            
            threads[i] = new ThreadOrdenadora(parte);
            threads[i].start();
        }

        // 2. Espera todas terminarem (join) [cite: 131]
        try {
            for (int i = 0; i < numProcessadores; i++) {
                threads[i].join();
                resultadosParciais[i] = threads[i].getVetorOrdenado();
            }
        } catch (InterruptedException e) {
            return null;
        }

        // 3. Junta (Merge) os resultados parciais (Exigência do trabalho: juntar 2 a 2) 
        // Estratégia simples: junta o 0 com 1, o resultado com 2, etc.
        byte[] vetorFinal = resultadosParciais[0];
        for (int i = 1; i < numProcessadores; i++) {
            vetorFinal = MergeUtils.intercalar(vetorFinal, resultadosParciais[i]);
        }

        return vetorFinal;
    }

    // Thread interna para ordenar um pedaço
    private class ThreadOrdenadora extends Thread {
        private byte[] meuVetor;

        public ThreadOrdenadora(byte[] vetor) {
            this.meuVetor = vetor;
        }

        public void run() {
            // Ordena localmente usando MergeSort sequencial
            MergeUtils.mergeSortSequencial(meuVetor, 0, meuVetor.length - 1);
        }

        public byte[] getVetorOrdenado() {
            return meuVetor;
        }
    }
}