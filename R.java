import java.util.*;

public class R // Receptor
{
    public static String PORTA_PADRAO = "12345";
    
    public static void main (String[] args)
    {
        if (args.length > 1) {
            System.err.println ("Uso esperado: java R [PORTA]\n");
            return;
        }

        String porta = R.PORTA_PADRAO;
        
        if (args.length == 1)
            porta = args[0];

        ArrayList<Parceiro> distribuidores = new ArrayList<Parceiro>();

        AceitadoraDeConexao aceitadoraDeConexao = null;
        try {
            aceitadoraDeConexao = new AceitadoraDeConexao(porta, distribuidores);
            aceitadoraDeConexao.start();
            System.out.println("[R] Servidor Receptor ouvindo na porta " + porta + "...");
        } catch (Exception erro) {
            System.err.println ("Escolha uma porta apropriada e liberada para uso!\n");
            return;
        }

        // Loop para desligamento geral do servidor
        for(;;)
        {
            System.out.println ("O servidor esta ativo! Para desativa-lo,");
            System.out.println ("use o comando \"desativar\"\n");
            System.out.print   ("> ");

            String comando = null;
            try {
                comando = Teclado.getUmString();
            } catch (Exception erro) {}

            if (comando.toLowerCase().equals("desativar"))
            {
                synchronized (distribuidores)
                {
                    ComunicadoEncerramento comunicadoDeEncerramento =
                    new ComunicadoEncerramento ();
                    
                    for (Parceiro distribuidor : distribuidores) {
                        try {
                            distribuidor.receba (comunicadoDeEncerramento);
                            distribuidor.adeus  ();
                        } catch (Exception erro) {}
                    }
                }

                System.out.println ("O servidor [R] foi desativado!\n");
                System.exit(0);
            }
            else
                System.err.println ("Comando invalido!\n");
        }
    }
}