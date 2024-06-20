package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.TransactionStatus;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.util.concurrent.LatchSignal;
import subsystem.account.AuthPlatform;
import subsystem.account.IAuthPlatformRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import apros.codeart.ddd.launcher.TestLauncher;

import java.util.concurrent.TimeUnit;

public class DataContextLockTest {

    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        initData();
    }

    private static void initData() {
        DataContext.using(() -> {
            create("系统控制", "admin");
            create("用户博客", "blog");
        });
    }

    private static void create(String name, String en) {
        var id = DataPortal.getIdentity(AuthPlatform.class);
        var platform = new AuthPlatform(id, name, en);
        Repository.add(platform);
    }

    @AfterAll
    public static void clean() {
        TestLauncher.stop();
    }

    @Test
    void nolockQuery() {

        assertTrue(ObjectMetaLoader.exists("AuthPlatform"));

        DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin", QueryLevel.NONE);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.None, ctx.transactionStatus());
        });
    }


    @Test
    void insert() {
        DataContext.using(() -> {
            create("testInsert", "testInsert");

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.Delay, ctx.transactionStatus());
        });
    }

    @Test
    void holdQuery() {
        DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin", QueryLevel.HOLD);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.Timely, ctx.transactionStatus());
        });
    }


    @Test
    void concurrency_hold_none() {

        var signal = new LatchSignal<Boolean>();


        Thread taskThread = asyncRun(() -> {
            DataContext.using(() -> {
                IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
                var obj = repository.findByEN("admin", QueryLevel.HOLD);

                signal.wait(10, TimeUnit.SECONDS);

            });
        });

        try {
            taskThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public Thread asyncRun(Runnable action) {
        Thread taskThread = new Thread(action);

        // 启动线程
        taskThread.start();

        return taskThread;
    }

}
