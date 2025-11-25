package solar_data_looca_api.main;

import solar_data_looca_api.core.Looca;
import solar_data_looca_api.group.memoria.Memoria;
import solar_data_looca_api.group.processador.Processador;
import solar_data_looca_api.group.processos.Processo;
import solar_data_looca_api.group.processos.ProcessoGrupo;

import java.sql.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    // Variaveis globais para evitar recriacao
    private static String hostname;
    private static int hostNumerico;
    private static String macAddress;
    private static String ip;
    private static int idEmpresa;
    private static boolean initialized = false;
    private static final AtomicInteger executionCount = new AtomicInteger(0);

    public static void main(String[] args) {

        while (true) {
            try {
                executarColeta();

                Thread.sleep(15000);

            } catch (Exception e) {
                System.err.println("Erro na execucao: " + e.getMessage());
                e.printStackTrace();

                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private static void executarColeta() throws SQLException, UnknownHostException, SocketException {
        String Banco = "jdbc:mysql://34.198.76.254/solarData01";
        String user = "solardata";
        String password = "Solar@Data01";

        try (Connection connection = DriverManager.getConnection(Banco, user, password)) {
            int execucaoAtual = executionCount.incrementAndGet();
            System.out.println("\n-- Execução " + execucaoAtual + " --");
            System.out.println("Conectado ao banco de dados");

            if (!initialized) {
                inicializarInformacoesMaquina();
                idEmpresa = obterOuCriarEmpresa(connection);
                inserirMaquinaSeNecessario(connection);
                initialized = true;
            }

            Looca looca = new Looca();
            Processador proc1 = new Processador();
            Memoria mem1 = new Memoria();
            ProcessoGrupo processoGrupo = new ProcessoGrupo();
            List<Processo> processos = processoGrupo.getProcessosRelevantes();          

            ordenarProcessos(processos);
            inserirProcessos(connection, processos, proc1, mem1);

            System.out.println("Execucao " + execucaoAtual + " completada");

 	    exibirEstatisticasProcessos(processoGrupo);
        }
    }

    private static void exibirEstatisticasProcessos(ProcessoGrupo processoGrupo) {
        List<Processo> processosSistema = processoGrupo.getProcessosSistema();
        List<Processo> processosUsuario = processoGrupo.getProcessosUsuario();
        List<Processo> processosRelevantes = processoGrupo.getProcessosRelevantes();

        System.out.println("Estatísticas de Processos Linux");
        System.out.println("- Processos do SO: " + processosSistema.size());
        System.out.println("- Processos do usuário: " + processosUsuario.size());
        System.out.println("- Processos relevantes: " + processosRelevantes.size());
        System.out.println("- Total de processos: " + processoGrupo.getTotalProcessos());

        if (!processosSistema.isEmpty()) {
            System.out.println("\nPrincipais processos do sistema detectados:");
            processosSistema.stream()
                .limit(5)
                .forEach(p -> System.out.println("  - " + p.getNome() + " (PID: " + p.getPid() + ")"));
        }
    }

    private static void inicializarInformacoesMaquina() throws UnknownHostException, SocketException {
        hostname = InetAddress.getLocalHost().getHostName();
        hostNumerico = hostname.hashCode() & 0x7FFFFFFF;
        macAddress = obterMacAddress();
        ip = obterIp();

        System.out.println("Maquina: " + hostname + " (ID: " + hostNumerico + ")");
        System.out.println("MAC: " + macAddress + ", IP: " + ip);
    }

    private static int obterOuCriarEmpresa(Connection connection) throws SQLException {

        String verificarEmpresaSql = "SELECT idEmpresa FROM Empresa LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(verificarEmpresaSql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int empresaId = rs.getInt("idEmpresa");
                System.out.println("Empresa encontrada: ID " + empresaId);
                return empresaId;
            } else {
                System.out.println("Criando empresa padrao...");
                return criarEmpresaAutomatica(connection);
            }
        }
    }

    private static int criarEmpresaAutomatica(Connection connection) throws SQLException {

        int idEndereco = criarEnderecoPadrao(connection);


        String sql = "INSERT INTO Empresa (fkEndereco, razaoSocial, nomeFantasia, cnpj) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String nomeEmpresa = "Empresa - " + hostname;
            String cnpj = gerarCnpjUnico();

            stmt.setInt(1, idEndereco);
            stmt.setString(2, nomeEmpresa);
            stmt.setString(3, nomeEmpresa);
            stmt.setString(4, cnpj);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int empresaId = rs.getInt(1);
                    System.out.println("Empresa criada: " + nomeEmpresa + " (ID: " + empresaId + ")");
                    return empresaId;
                }
            }
        }
        throw new SQLException("Falha ao criar empresa automatica");
    }

    private static int criarEnderecoPadrao(Connection connection) throws SQLException {
        String sql = "INSERT INTO Endereco (cep, logradouro, numero, bairro, cidade, estado, complemento) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "00000000");
            stmt.setString(2, "Endereco Automatico");
            stmt.setString(3, "0");
            stmt.setString(4, "Centro");
            stmt.setString(5, "Sao Paulo");
            stmt.setString(6, "SP");
            stmt.setString(7, "Criado automaticamente");
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Falha ao criar endereco padrao");
    }

    private static String gerarCnpjUnico() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%014d", (timestamp + random) % 100000000000000L);
    }

    private static void inserirMaquinaSeNecessario(Connection connection) throws SQLException {
        String verificarSql = "SELECT COUNT(*) FROM Maquina WHERE hostName = ?";
        try (PreparedStatement verificarStmt = connection.prepareStatement(verificarSql)) {
            verificarStmt.setInt(1, hostNumerico);
            try (ResultSet rs = verificarStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String inserirSql = "INSERT INTO Maquina (hostName, identificador, fkEmpresa, macAdress, ip) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement inserirStmt = connection.prepareStatement(inserirSql)) {
                        inserirStmt.setInt(1, hostNumerico);
                        inserirStmt.setString(2, hostname);
                        inserirStmt.setInt(3, idEmpresa);
                        inserirStmt.setString(4, macAddress);
                        inserirStmt.setString(5, ip);
                        inserirStmt.executeUpdate();
                        System.out.println("Maquina inserida no banco");
                    }
                } else {
                    System.out.println("Maquina ja existe no banco");
                }
            }
        }
    }

    private static String obterMacAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            byte[] mac = networkInterface.getHardwareAddress();
            if (mac != null && !networkInterface.isLoopback() && networkInterface.isUp()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < mac.length; i++) {
                    sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                }
                return sb.toString();
            }
        }
        return "00:00:00:00:00:00";
    }

    private static String obterIp() throws UnknownHostException, SocketException {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
            return "127.0.0.1";
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
        System.out.println("Coletando dados de " + processos.size() + " processos...");

        int processosInseridos = 0;
        for (int i = 0; i < Math.min(100, processos.size()); i++) {
            Processo p = processos.get(i);

            String sql = "INSERT INTO Processo (pid, nome, cpuPorcentagem, ramPorcentagem, fkMaquina, tipo) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                Integer totalNucleos = proc1.getNumeroCpusLogicas();
                double memoriaGB = mem1.getTotal() / (1024.0 * 1024.0 * 1024.0);
                double ramGB = p.getUsoMemoria();
                double porcentagemRAM = p.getUsoMemoria();
                double porcentagemCPU = p.getUsoCpu() / totalNucleos;

                String tipo = (porcentagemCPU > 1. || porcentagemRAM > 5.) ? "QUENTE" : "FRIO";

                stmt.setInt(1, p.getPid());
                stmt.setString(2, p.getNome());
                stmt.setDouble(3, porcentagemCPU);
                stmt.setDouble(4, porcentagemRAM);
                stmt.setInt(5, hostNumerico);
                stmt.setString(6, tipo);

                stmt.executeUpdate();
                processosInseridos++;
            }
        }
        System.out.println(processosInseridos + " processos inseridos");
    }
}
