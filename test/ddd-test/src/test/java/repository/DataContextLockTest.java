package repository;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.TransactionStatus;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.util.WrapperBoolean;
import apros.codeart.util.concurrent.LatchSignal;
import subsystem.account.AuthPlatform;
import subsystem.account.IAuthPlatformRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import apros.codeart.ddd.launcher.TestLauncher;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


    /**
     * hold查询不会影响none查询
     */
    @Test
    void concurrency_hold_none() {

        var concurrency = first_concurrency_second(QueryLevel.HOLD, QueryLevel.NONE);
        assertFalse(concurrency);
    }

    /**
     * hold查询会阻塞single查询
     */
    @Test
    void concurrency_hold_single() {
        var concurrency = first_concurrency_second(QueryLevel.HOLD, QueryLevel.SINGLE);
        assertTrue(concurrency);
    }


    private static boolean first_concurrency_second(QueryLevel first,QueryLevel second){
        WrapperBoolean concurrency = new WrapperBoolean(false);

        var signal = new LatchSignal<Boolean>();

        Thread threadHold = asyncRun(() -> {
            DataContext.using(() -> {
                queryAdmin(first);

                try{
                    signal.wait(2, TimeUnit.SECONDS);
                    concurrency.setValue(false);
                }
                catch (Exception ex){
                    //由于会阻塞single，所以会超时
                    concurrency.setValue(true);
                }

            });
        });

        waitLimited();

        // single会被阻塞
        Thread threadNone = asyncRun(() -> {
            DataContext.using(() -> {
                queryAdmin(second);
                signal.set(true);
            });
        });

        try {
            threadHold.join();
            threadNone.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return concurrency.getValue();
    }

    /**
     * hold和共享查询不互斥
     */
    @Test
    void concurrency_hold_share() {

        var concurrency = first_concurrency_second(QueryLevel.HOLD,QueryLevel.SHARE);
        assertFalse(concurrency);
    }

    private static void waitLimited(){
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void queryAdmin(QueryLevel level){
        IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
        var obj = repository.findByEN("admin",level);
    }

    public static Thread asyncRun(Runnable action) {
        Thread thread = new Thread(action);

        // 启动线程
        thread.start();

        return thread;
    }

}
