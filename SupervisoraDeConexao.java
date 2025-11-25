import java.io.*;
import java.net.*;
import java.util.*;

public class SupervisoraDeConexao extends Thread {
    private Parceiro usuario;
    private Socket conexao;
    private ArrayList<Parceiro> usuarios;

    public SupervisoraDeConexao(Socket conexao, ArrayList<Parceiro> usuarios) throws Exception {
        if (conexao == null) throw new Exception("Conexao ausente");
        if (usuarios == null) throw new Exception("Usuarios ausentes");
        this.conexao = conexao;
        this.usuarios = usuarios;
    }

    public void run() {
        ObjectOutputStream transmissor;
        try { transmissor = new ObjectOutputStream(this.conexao.getOutputStream()); }
        catch (Exception e) { return; }

        ObjectInputStream receptor;
        try { receptor = new ObjectInputStream(this.conexao.getInputStream()); }
        catch (Exception e) { return; }

        try { this.usuario = new Parceiro(this.conexao, receptor, transmissor); }
        catch (Exception e) {}

        try {
            synchronized (this.usuarios) { this.usuarios.add(this.usuario); }
            // LOG: Cliente conectou
            String ipCliente = this.conexao.getInetAddress().getHostAddress();
            System.out.println("[R] Cliente conectado: " + ipCliente);

            for (;;) {
                Comunicado comunicado = this.usuario.envie();

                if (comunicado == null) return;
                
                else if (comunicado instanceof Pedido) {
                    Pedido pedido = (Pedido) comunicado;
                    
                    // LOG: Recebeu pedido
                    System.out.println("[R] Pedido recebido de " + ipCliente + ". Iniciando ordenação...");
                    long inicio = System.currentTimeMillis();

                    // Ordena
                    byte[] ordenado = pedido.ordenar();
                    
                    long fim = System.currentTimeMillis();
                    // LOG: Terminou
                    System.out.println("[R] Ordenação concluída em " + (fim - inicio) + "ms. Enviando resposta...");

                    this.usuario.receba(new Resposta(ordenado));
                } 
                else if (comunicado instanceof ComunicadoEncerramento) {
                    // LOG: Encerramento
                    System.out.println("[R] Cliente " + ipCliente + " enviou encerramento. Desconectando.");
                    synchronized (this.usuarios) { this.usuarios.remove(this.usuario); }
                    this.usuario.adeus();
                    return;
                }
            }
        } catch (Exception e) {
            try { transmissor.close(); receptor.close(); } catch (Exception f) {}
        }
    }
}