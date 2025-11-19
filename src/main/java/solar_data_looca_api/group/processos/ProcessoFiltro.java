package solar_data_looca_api.group.processos;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProcessoFiltro {

    private static final Set<String> PROCESSOS_SO_LINUX = new HashSet<>(Arrays.asList(
            "systemd", "kthreadd", "ksoftirqd", "rcu_sched", "rcu_bh", "migration",
            "watchdog", "kworker", "kdevtmpfs", "netns", "khungtaskd", "oom_reaper",
            "writeback", "kcompactd", "ksmd", "crypto", "kintegrityd", "bioset",
            "kblockd", "ata_sff", "md", "edac-poller", "devfreq_wq", "watchdogd",
            "kswapd", "ecryptfs-kthrea", "kthrotld", "acpi_thermal_pm", "scsi_eh_",
            "scsi_tmf_", "bioset", "ipv6_addrconf", "kstrp", "zswap-shrink",
            "kworker/u", "systemd-journal", "systemd-udevd", "cron", "dbus-daemon",
            "accounts-daemon", "rsyslogd", "networkd-dispat", "systemd-logind",
            "sshd", "agetty", "systemd-timesyn", "polkitd", "atd", "lvmetad",
            "auditd", "irqbalance", "lm-sensors", "systemd-resolve", "packagekitd",
            "gdm", "gnome-shell", "Xorg", "gsd-", "gvfs-", "dconf-service",
            "evolution-sourc", "tracker-", "goa-", "snapd", "containerd", "dockerd",
            "rpcbind", "rpc.statd", "rpc.idmapd", "nfsd", "lockd", "rpc.mountd",
            "apache2", "nginx", "mysql", "postgres", "redis-server", "mongod",
            "memcached", "beanstalkd", "rabbitmq", "supervisord", "systemd-network",
            "systemd-resolve", "upowerd", "avahi-daemon", "cupsd", "cups-browsed",
            "NetworkManager", "wpa_supplicant", "modemmanager", "boltd", "fwupd",
            "udisksd", "gpg-agent", "pulseaudio", "pipewire", "wireplumber",
            "rtkit-daemon", "upstart", "init", "sudo", "bash", "sh", "dash",
            "login", "su", "passwd", "useradd", "usermod", "groupadd"
    ));

    private static final Set<Integer> PIDS_SISTEMA_LINUX = new HashSet<>(Arrays.asList(
            0, 1, 2
    ));

    private static final Set<String> DIRETORIOS_SISTEMA = new HashSet<>(Arrays.asList(
            "/sbin/", "/bin/", "/usr/sbin/", "/usr/bin/", "/lib/", "/usr/lib/",
            "/var/lib/", "/run/", "/proc/", "/sys/", "/dev/"
    ));

    public static boolean isProcessoSistema(String nomeProcesso, Integer pid) {
        if (nomeProcesso == null || pid == null) return true;

        if (PIDS_SISTEMA_LINUX.contains(pid)) {
            return true;
        }

        if (PROCESSOS_SO_LINUX.contains(nomeProcesso)) {
            return true;
        }

        if (nomeProcesso.startsWith("kworker/") || nomeProcesso.startsWith("kworker.u")) {
            return true;
        }

        if (nomeProcesso.contains("systemd-") ||
                nomeProcesso.contains("kthreadd") ||
                nomeProcesso.contains("rcu_") ||
                nomeProcesso.contains("migration") ||
                nomeProcesso.contains("watchdog")) {
            return true;
        }

        return false;
    }

    public static boolean isProcessoUsuario(String nomeProcesso, Integer pid) {
        return !isProcessoSistema(nomeProcesso, pid);
    }

    /**
     * Filtro mais agressivo - considera apenas processos com uso significativo de recursos
     */
    public static boolean isProcessoRelevante(Processo processo, double limiteMinimoCPU, double limiteMinimoMemoria) {
        if (processo == null) return false;

        if (isProcessoSistema(processo.getNome(), processo.getPid())) {
            return false;
        }

        if (processo.getUsoCpu() < limiteMinimoCPU && processo.getUsoMemoria() < limiteMinimoMemoria) {
            return false;
        }

        return true;
    }
}