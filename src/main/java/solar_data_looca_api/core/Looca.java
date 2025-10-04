package solar_data_looca_api.core;


import solar_data_looca_api.group.memoria.Memoria;
import solar_data_looca_api.group.processador.Processador;
import solar_data_looca_api.group.processos.ProcessoGrupo;
import oshi.SystemInfo;

public class Looca {

    private final Memoria memoria;
    private final Processador processador;
    private final ProcessoGrupo grupoDeProcessos;

    public Looca() {
        SystemInfo systemInfo = new SystemInfo();

        this.memoria = new Memoria();
        this.processador = new Processador();
        this.grupoDeProcessos = new ProcessoGrupo();

    }

    /**
     * Retorna um <b>Objeto de Memoria</b> que contém métodos relacionados a coleta de informações da <b>Memória RAM</b>.
     *
     * @return Objeto Memoria.
     */
    public Memoria getMemoria() {
        return memoria;
    }

    /**
     * Retorna um <b>Objeto de Processador</b> que contém métodos relacionados a coleta de informações do <b>Processador</b>.
     *
     * @return Objeto Processador.
     */
    public Processador getProcessador() {
        return processador;
    }


    /**
     * Retorna um <b>Objeto de ProcessoGrupo</b> que contém métodos relacionados a coleta de informações do <b>Grupo de Processos</b>.
     *
     * @return Objeto ProcessoGrupo.
     */
    public ProcessoGrupo getGrupoDeProcessos() {
        return grupoDeProcessos;
    }

}
