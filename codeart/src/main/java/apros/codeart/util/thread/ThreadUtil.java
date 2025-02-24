package apros.codeart.util.thread;

import org.apache.logging.log4j.core.net.Facility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static apros.codeart.runtime.Util.propagate;

public final class ThreadUtil {

    private ThreadUtil() {
    }

    private static boolean isProcessRunningWindows(long pid) {
        try {
            Process process = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(String.valueOf(pid))) {
                    return true; // 进程存在
                }
            }
        } catch (IOException e) {
            throw propagate(e);
        }
        return false; // 进程不存在
    }

    private static boolean isProcessRunningLinux(long pid) {
        try {
            Process process = new ProcessBuilder("ps", "-p", String.valueOf(pid)).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(String.valueOf(pid))) {
                    return true; // 进程存在
                }
            }
        } catch (IOException e) {
            throw propagate(e);
        }
        return false; // 进程不存在
    }

    public static boolean existsProcess(long pid) {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? isProcessRunningWindows(pid) : isProcessRunningLinux(pid);
    }

    public static boolean killProcess(long pid) {

        if (!existsProcess(pid)) return false;

        String os = System.getProperty("os.name").toLowerCase();
        try {
            Process killProcess;
            if (os.contains("win")) {
                killProcess = new ProcessBuilder("taskkill", "/F", "/PID", String.valueOf(pid)).start();
            } else {
                killProcess = new ProcessBuilder("kill", "-9", String.valueOf(pid)).start();
            }
            int exitCode = killProcess.waitFor();
            if (exitCode == 0) return true;
        } catch (Exception e) {
            throw propagate(e);
        }
        return false;
    }
}
