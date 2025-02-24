package apros.codeart.ddd.launcher;

import apros.codeart.TestSupport;
import apros.codeart.dto.DTObject;
import apros.codeart.io.IOUtil;
import apros.codeart.util.concurrent.LatchSignal;
import apros.codeart.util.thread.ThreadUtil;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static apros.codeart.runtime.Util.propagate;

@TestSupport
public class DomainServer {

    private final String _name;

    public String name() {
        return _name;
    }

    private Process _process;

    private BufferedWriter _writer;

    private LatchSignal<Boolean> _runningSignal;

    private LatchSignal<Boolean> _stopSignal;

    private String[] _domainEvents;

    public String[] domainEvents() {
        return _domainEvents;
    }

    public void domainEvents(String[] value) {
        _domainEvents = value;
    }

    public DomainServer(String name) {
        _name = name;
    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw propagate(e);
        }
    }


    public void open() {
        process_start();
        waitRunStarted();
    }


    private void waitRunStarted() {
        if (_runningSignal == null) _runningSignal = new LatchSignal<>();
        _runningSignal.forever(); // 等待启动完毕
    }

    private void runStarted() {
        _runningSignal.set(true);
        _runningSignal = null;
    }


    private void waitStop() {
        if (_stopSignal == null) _stopSignal = new LatchSignal<>();
        _stopSignal.forever(); // 等待启动完毕
    }


    private void stopped() {
        _stopSignal.set(true);
        _stopSignal = null;
    }


    public void close() {
        process_stop();
    }


    private void process_start() {

        try {
            // 获取当前 Java 进程的执行路径
            String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            String currentClass = "apros.codeart.ddd.launcher.DomainContainer";

            // 获取当前 JVM 选项
            List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();

            // 创建 ProcessBuilder
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(javaBin);
            builder.command().addAll(jvmArgs); // 继承 JVM 选项
            builder.command().add("-cp");
            builder.command().add(CLASS_PATH);
            builder.command().add(currentClass);

            builder.command().add(this.name());      //第一个参数为服务器名称
            fillDomainEventsConfig(builder);

            builder.environment().putAll(System.getenv()); // 继承环境变量
            builder.directory(new File(System.getProperty("user.dir"))); // 继承工作目录
//            builder.inheritIO(); // 继承标准输入输出 不能设置这个，父进程不能获取子进程的输出了
            builder.redirectErrorStream(true); // 合并标准输出和标准错误

            // 启动子进程
            _process = builder.start();

            addProcess(_process.pid());

            _writer = new BufferedWriter(new OutputStreamWriter(_process.getOutputStream()));

            // 异步读取子进程输出
            CompletableFuture.runAsync(() -> readStream(_process.getInputStream()));

        } catch (Exception e) {
            System.out.printf("%s%n", e.getMessage());
            throw propagate(e);
        }
    }

    private void fillDomainEventsConfig(ProcessBuilder process) {
        if (_domainEvents == null || _domainEvents.length == 0) return;
        process.command().add(String.format("-de %s", String.join(",", _domainEvents)));
    }

    private void readStream(InputStream inputStream) {

        String started = String.format("[%s]started", this.name());
        String stopped = String.format("[%s]stopped", this.name());

        try (Scanner scanner = new Scanner(new InputStreamReader(inputStream, Charset.defaultCharset().displayName()))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (started.equals(line)) {
                    runStarted();
//                    System.out.printf("[%s]started%n", this.name());
                    continue;
                }

                if (stopped.equals(line)) {
                    delay(200); //等待200毫秒，让close的逻辑能进入到waitStop()
                    stopped();
//                    System.out.printf("[%s]stopped%n", this.name());
                    break;  // 注意，关闭了就退出
                }

                System.out.printf("[%s]%s%n", this.name(), line);
            }
        } catch (Exception e) {
            System.out.printf("%s%n", e.getMessage());
            throw propagate(e);
        }
    }

    private void send(String message) {
        // 父进程向子进程发送数据
        try {
            _writer.write(message + "\n");
            _writer.flush();
        } catch (Exception e) {
            System.out.printf("%s%n", e.getMessage());
            throw propagate(e);
        }
    }


    private void process_stop() {
        if (_process != null) {
            try {
                send(String.format("[%s]stop", this.name()));
                waitStop();
                _writer.close();
                _process.getOutputStream().close();
                _process.destroyForcibly();
            } catch (IOException e) {
                throw propagate(e);
            }
        }

    }


    //region 获取类路径

    private final static String CLASS_PATH;

    static {
        try {
            CLASS_PATH = getClassPath();
        } catch (Exception e) {
            throw propagate(e);
        }
    }


    private static String getClassPath() throws IOException, InterruptedException {
        // 启动 Maven 命令
        ProcessBuilder mvnProcess = new ProcessBuilder("mvn.cmd", "-U", "dependency:build-classpath");
        mvnProcess.redirectErrorStream(true); // 合并标准输出和错误输出
        Process mvn = mvnProcess.start();

        // 读取 Maven 进程的完整输出
        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mvn.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n")); // 读取所有输出
        }

        // 等待进程执行完毕
        mvn.waitFor();

        return extractClasspath(output);
    }

    // 提取 "Dependencies classpath:" 之后的内容
    private static String extractClasspath(String output) {
        int startIndex = output.indexOf("Dependencies classpath:");
        if (startIndex == -1) {
            throw new RuntimeException("未找到 'Dependencies classpath:' 行");
        }

        // 取出 `Dependencies classpath:` 之后的内容
        String classpath = output.substring(startIndex + "Dependencies classpath:".length()).trim();

        // 删除多余的 `\n[INFO]` 以及后面的日志
        int endIndex = classpath.indexOf("\n[INFO]");
        if (endIndex != -1) {
            classpath = classpath.substring(0, endIndex).trim();
        }

        return classpath;
    }

    //endregion

//    public DTObject getLog() {
//        return DomainContainer.getLog(_name);
//    }

    // 进程的移除

    private static void addProcess(long id) {
        String fileName = IOUtil.createTempFile("domain-server-processes", false);
        var dto = DTObject.load(fileName);
        dto.pushLong("rows", id);
        dto.save(fileName);
    }

    private static void killProcesses() {
        String fileName = IOUtil.getTempFile("domain-server-processes");
        var dto = DTObject.load(fileName);
        var ids = dto.getLongs("rows", false);
        if (ids != null) {
            for (long id : ids) {
                ThreadUtil.killProcess(id);
            }
        }
    }

    static {
        killProcesses();
    }

}
