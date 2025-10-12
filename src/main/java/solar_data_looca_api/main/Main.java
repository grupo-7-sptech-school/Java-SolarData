package solar_data_looca_api.main;

import solar_data_looca_api.core.Looca;
import solar_data_looca_api.group.memoria.Memoria;
import solar_data_looca_api.group.processador.Processador;
import solar_data_looca_api.group.processos.Processo;
import solar_data_looca_api.group.processos.ProcessoGrupo;
import java.sql.*;
import java.net.InetAddress;
import java.net.UnknownHostException;


import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException, UnknownHostException {

        String Banco = "jdbc:mysql://localhost:3306/solarData01";
        String user = "solardata";
        String password = "Solar@Data01";


                try (Connection connection = DriverManager.getConnection(Banco, user, password)) {
                System.out.println("Conectado ao banco de dados");

                Looca looca = new Looca();
                Processador proc1 = new Processador();
                Memoria mem1 = new Memoria();
                ProcessoGrupo processoGrupo = new ProcessoGrupo();
                List<Processo> processos = processoGrupo.getProcessos();

                ordenarProcessos(processos);
                inserirProcessos(connection, processos, proc1, mem1);
            }

    }



    private static void ordenarProcessos(List<Processo> processos) {

        for (int i = 0; i < processos.size() - 1; i++) {
            int maiorValor = i;


            for (int j = i + 1; j < processos.size(); j++) {
                if (processos.get(j).getUsoCpu() > processos.get(maiorValor).getUsoCpu()) {
                    maiorValor = j;
                }
            }
            if (maiorValor != i) {
                Processo temp = processos.get(i);
                processos.set(i, processos.get(maiorValor));
                processos.set(maiorValor, temp);
            }
        }
    }

    private static void inserirProcessos(Connection connection, List<Processo> processos, Processador proc1, Memoria mem1) throws SQLException, UnknownHostException {

        String hostname = InetAddress.getLocalHost().getHostName();
        int hostNumerico = hostname.hashCode() & 0x7FFFFFFF;
        System.out.println(hostNumerico);
        for (int i = 0; i < Math.min(100, processos.size()); i++) {
            Processo p = processos.get(i);

            PreparedStatement conexaoBD = connection.prepareStatement(
                    "INSERT INTO Processo (pid, nome, cpuPorcentagem, ramPorcentagem, fkMaquina, tipo) VALUES (?, ?, ?, ?, ?, ?)");

            Integer totalNucleos = proc1.getNumeroCpusLogicas();
            double memoriaGB = mem1.getTotal() / (1024.0 * 1024.0 * 1024.0);
            double ramGB = p.getUsoMemoria();
            double porcentagemRAM = (ramGB / memoriaGB) * 100.0;
            double porcentagemCPU = p.getUsoCpu() / totalNucleos;

            if (porcentagemCPU > 1. || porcentagemRAM > 5.){
                conexaoBD.setString(6, "QUENTE");
            }else {
                conexaoBD.setString(6, "FRIO");
            }

            conexaoBD.setInt(1, p.getPid());
            conexaoBD.setString(2, p.getNome());
            conexaoBD.setDouble(3, porcentagemCPU);
            conexaoBD.setDouble(4, porcentagemRAM);
            conexaoBD.setInt(5, hostNumerico);

            conexaoBD.executeUpdate();
            conexaoBD.close();

            if (i == 0){
                System.out.println("Inserindo registros, aguarde...");
            }
        }
        System.out.println("Registros inseridos com sucesso!");
    }
}