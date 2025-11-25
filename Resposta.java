import java.io.Serializable;

public class Resposta extends Comunicado implements Serializable {
    private byte[] vetorOrdenado; // Agora devolvemos o vetor inteiro

    public Resposta(byte[] vetorOrdenado) {
        this.vetorOrdenado = vetorOrdenado;
    }

    public byte[] getVetor() {
        return this.vetorOrdenado;
    }
}