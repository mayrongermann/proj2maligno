public class MergeUtils {
    
    // Método que junta dois vetores ordenados em um só (Subsídio 1 do PDF)
    public static byte[] intercalar(byte[] v1, byte[] v2) {
        byte[] resultado = new byte[v1.length + v2.length];
        int i = 0, j = 0, k = 0;

        // Enquanto houver elementos em ambos
        while (i < v1.length && j < v2.length) {
            if (v1[i] < v2[j]) {
                resultado[k++] = v1[i++];
            } else {
                resultado[k++] = v2[j++];
            }
        }

        // Copiar o que sobrou de v1 (se sobrou)
        while (i < v1.length) {
            resultado[k++] = v1[i++];
        }

        // Copiar o que sobrou de v2 (se sobrou)
        while (j < v2.length) {
            resultado[k++] = v2[j++];
        }

        return resultado;
    }

    // Merge Sort Sequencial (para vetores pequenos ou finalização)
    public static void mergeSortSequencial(byte[] vetor, int inicio, int fim) {
        if (inicio < fim) {
            int meio = (inicio + fim) / 2;
            mergeSortSequencial(vetor, inicio, meio);
            mergeSortSequencial(vetor, meio + 1, fim);
            merge(vetor, inicio, meio, fim);
        }
    }

    // Auxiliar do sequencial
    private static void merge(byte[] vetor, int inicio, int meio, int fim) {
        int n1 = meio - inicio + 1;
        int n2 = fim - meio;
        byte[] L = new byte[n1];
        byte[] R = new byte[n2];

        for (int i = 0; i < n1; ++i) L[i] = vetor[inicio + i];
        for (int j = 0; j < n2; ++j) R[j] = vetor[meio + 1 + j];

        int i = 0, j = 0;
        int k = inicio;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) vetor[k++] = L[i++];
            else vetor[k++] = R[j++];
        }
        while (i < n1) vetor[k++] = L[i++];
        while (j < n2) vetor[k++] = R[j++];
    }
}