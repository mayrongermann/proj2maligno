import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ThreadDoCliente extends Thread {
    private String ip;
    private int porta;
    private byte[] meuVetor;
    
    private Parceiro servidor;
    private byte[] vetorOrdenado; // Guarda o resultado final

    public ThreadDoCliente(String ip, int porta, byte[] meuVetor) {
        this.ip = ip;
        this.porta = porta;
        this.meuVetor = meuVetor;
        this.vetorOrdenado = new byte[0];
    }

    public void run() {
        try {
            Socket conexao = new Socket(this.ip, this.porta);
            ObjectOutputStream transmissor = new ObjectOutputStream(conexao.getOutputStream());
            ObjectInputStream receptor = new ObjectInputStream(conexao.getInputStream());
            this.servidor = new Parceiro(conexao, receptor, transmissor);

            // Envia o vetor desordenado
            Pedido pedido = new Pedido(this.meuVetor);
            this.servidor.receba(pedido);

            // Espera a resposta
            Comunicado c;
            do {
                c = this.servidor.espie();
            } while (!(c instanceof Resposta));

            Resposta r = (Resposta) this.servidor.envie();
            this.vetorOrdenado = r.getVetor(); // Pega o vetor ordenado

        } catch (Exception e) {
            System.err.println("Erro na thread " + ip + ": " + e.getMessage());
        }
    }

    public byte[] getVetorOrdenado() {
        return this.vetorOrdenado;
    }

    public void enviarEncerramento() {
        try {
            if (this.servidor != null) {
                this.servidor.receba(new ComunicadoEncerramento());
                this.servidor.adeus();
            }
        } catch (Exception e) {}
    }
}