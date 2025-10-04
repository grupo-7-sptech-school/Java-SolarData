package com.github.britooo.looca.api.main;

import com.github.britooo.looca.api.core.Looca;
import com.github.britooo.looca.api.group.memoria.Memoria;
import com.github.britooo.looca.api.group.processador.Processador;
import com.github.britooo.looca.api.group.processos.Processo;
import com.github.britooo.looca.api.group.processos.ProcessoGrupo;
import java.sql.*;

import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {

        String Banco = "jdbc:mysql://localhost:3306/meubanco";
        String user = "aluno";
        String password = "sptech";

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
                    "INSERT INTO monitoramento (pid, nome_processo, uso_cpu, uso_ram) VALUES (?, ?, ?, ?)");

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

            System.out.printf("Inserido: PID: %d | Nome: %s | CPU: %.2f%% | RAM: %.2f%%\n",
                    p.getPid(), p.getNome(), porcentagemCPU, porcentagemRAM);
        }
    }
}