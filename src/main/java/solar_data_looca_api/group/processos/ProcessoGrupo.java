package solar_data_looca_api.group.processos;

import java.util.List;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;

import java.util.stream.Collectors;

import oshi.software.os.OperatingSystem;
import solar_data_looca_api.group.memoria.Memoria;

public class ProcessoGrupo {

  private final OperatingSystem os = new SystemInfo().getOperatingSystem();

  /**
   * Retorna o número de <b>threads em execução</b>.
   *
   * @return Número de threads em execução.
   */
  public Integer getTotalThreads() {
    return this.os.getThreadCount();
  }

  /**
   * Retorna o número de <b>processos em execução</b>.
   *
   * @return Número de processos em execução.
   */
  public Integer getTotalProcessos() {
    return this.os.getProcessCount();
  }

  /**
   * <p>Retorna os <b>processos em execução no momento</b>. Nenhum pedido é garantido.</p>
   * <br>
   * <p>Sendo uma lista de objetos <code>OSProcess</code> para o número especificado (ou todos) de processos atualmente em execução,
   * classificados conforme especificado.
   * A lista pode conter elementos nulos ou processos com estado <code>OSProcess.State.INVALID</code> se um processo for encerrado durante a iteração.</p>
   *
   * @return Lista com os processos em execução no momento. Nenhum pedido é garantido.
   */
  public List<Processo> getProcessos() {
    return this.os.getProcesses()
        .stream()
        .map(ProcessoGrupo::of)
        .collect(Collectors.toList());
  }

  private static Processo of(OSProcess processo) {
    if (processo == null) {
      return null;
    }
    Memoria memoria = new Memoria();
    return new Processo(processo, memoria);
  }

  /**
   * Retorna uma <code>String</code> com todas as informações relacionadas ao <b>Grupo de Processos</b>.
   * @return <code>String</code> com todas as informações relacionadas ao <b>Grupo de Processos</b>.
   */
  @Override
  public String toString() {
    return String.format("\nLista de processos\nTotal de processos ativos: %d\nTotal de threads: %d\nProcessos: %s",
        this.getTotalProcessos(), this.getTotalThreads(), this.getProcessos());
  }
      /**
     * Retorna apenas processos do usuário, filtrando processos do sistema operacional Linux
     */
    public List<Processo> getProcessosUsuario() {
        return this.os.getProcesses()
            .stream()
            .map(ProcessoGrupo::of)
            .filter(processo -> processo != null && 
                    ProcessoFiltro.isProcessoUsuario(processo.getNome(), processo.getPid()))
            .collect(Collectors.toList());
    }

    /**
     * Retorna apenas processos relevantes (usuário + uso significativo de recursos)
     */
    public List<Processo> getProcessosRelevantes() {
        return this.os.getProcesses()
            .stream()
            .map(ProcessoGrupo::of)
            .filter(processo -> ProcessoFiltro.isProcessoRelevante(processo, 0.1, 0.1)) // 0.1% CPU e 0.1% RAM
            .collect(Collectors.toList());
    }

    /**
     * Retorna apenas processos do sistema operacional Linux
     */
    public List<Processo> getProcessosSistema() {
        return this.os.getProcesses()
            .stream()
            .map(ProcessoGrupo::of)
            .filter(processo -> processo != null && 
                    ProcessoFiltro.isProcessoSistema(processo.getNome(), processo.getPid()))
            .collect(Collectors.toList());
    }
}




