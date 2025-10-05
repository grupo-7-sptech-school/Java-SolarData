package solar_data_looca_api.main;

import solar_data_looca_api.core.Looca;
import solar_data_looca_api.group.memoria.Memoria;
import solar_data_looca_api.group.processador.Processador;
import solar_data_looca_api.group.processos.Processo;
import solar_data_looca_api.group.processos.ProcessoGrupo;
import java.sql.*;

import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {

        String Banco = "jdbc:mysql://localhost:3306/solarData";
        String user = "solardata";
        String password = "solar@data";

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

    private static void inserirProcessos(Connection connection, List<Processo> processos, Processador proc1, Memoria mem1) throws SQLException {
        for (int i = 0; i < Math.min(100, processos.size()); i++) {
            Processo p = processos.get(i);

            PreparedStatement conexaoBD = connection.prepareStatement(
                    "INSERT INTO ProcessoFrio (pid, nome, cpuPorcentagem, ramPorcentagem) VALUES (?, ?, ?, ?)");

            PreparedStatement conexaoBDquente = connection.prepareStatement(
                    "INSERT INTO ProcessoQuente (pid, nome, cpuPorcentagem, ramPorcentagem) VALUES (?, ?, ?, ?)");

            Integer totalNucleos = proc1.getNumeroCpusLogicas();
            double memoriaGB = mem1.getTotal() / (1024.0 * 1024.0 * 1024.0);
            double ramGB = p.getUsoMemoria();
            double porcentagemRAM = (ramGB / memoriaGB) * 100.0;
            double porcentagemCPU = p.getUsoCpu() / totalNucleos;

            conexaoBD.setInt(1, p.getPid());
            conexaoBD.setString(2, p.getNome());
            conexaoBD.setDouble(3, porcentagemCPU);
            conexaoBD.setDouble(4, porcentagemRAM);

            conexaoBD.executeUpdate();
            conexaoBD.close();


            if (porcentagemCPU > 1.){
                conexaoBDquente.setInt(1, p.getPid());
                conexaoBDquente.setString(2, p.getNome());
                conexaoBDquente.setDouble(3, porcentagemCPU);
                conexaoBDquente.setDouble(4, porcentagemRAM);

                conexaoBDquente.executeUpdate();
                conexaoBD.close();
            }

            if (i == 0){
                System.out.println("Inserindo registros, aguarde...");
            }
        }
        System.out.println("Registros inseridos com sucesso!");
    }
}