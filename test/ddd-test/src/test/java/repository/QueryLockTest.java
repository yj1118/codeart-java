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

/**
 * 我们提供对单元测试里实现的锁关系承诺
 */
public class QueryLockTest {

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
    void insert_transactionStatus() {
        DataContext.using(() -> {
            create("testInsert", "testInsert");

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.Delay, ctx.transactionStatus());
        });
    }

    @Test
    void holdQuery_transactionStatus() {
        DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin", QueryLevel.HOLD);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.Timely, ctx.transactionStatus());
        });
    }

    @Test
    void noneQuery_transactionStatus() {
        DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin", QueryLevel.NONE);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.None, ctx.transactionStatus());
        });
    }

    @Test
    void shareQuery_transactionStatus() {
        DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin", QueryLevel.SHARE);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.Share, ctx.transactionStatus());
        });
    }

    @Test
    void singleQuery_transactionStatus() {
        DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin", QueryLevel.SINGLE);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.Timely, ctx.transactionStatus());
        });
    }


    /**
     * hold不阻塞none查询
     */
    @Test
    void hold_none_hasData_notConcurrency() {

        //hold查询不会阻塞none查询
        var concurrency = first_concurrency_second_hasData(QueryLevel.HOLD, QueryLevel.NONE);
        assertFalse(concurrency);

    }

    /**
     * hold不阻塞none查询
     */
    @Test
    void hold_none_notData_notConcurrency() {

        var concurrency = first_concurrency_second_noData(QueryLevel.HOLD, QueryLevel.NONE);
        assertFalse(concurrency);
    }

    /**
     * single和single在有数据的时候，查询互斥
     */
    @Test
    void single_single_hasData_concurrency() {

        //有数据时single查询会阻塞single查询
        var concurrency = first_concurrency_second_hasData(QueryLevel.SINGLE, QueryLevel.SINGLE);
        assertTrue(concurrency);

    }

    /**
     * single和single在没有数据的时候，查询不互斥
     */
    @Test
    void single_single_notData_notConcurrency() {

        //没有数据时，single查询不会阻塞single查询
        var concurrency = first_concurrency_second_noData(QueryLevel.SINGLE, QueryLevel.SINGLE);
        assertFalse(concurrency);
    }

    @Test
    void single_share_hasData_concurrency() {

        //没有数据时，single查询不会阻塞single查询
        var concurrency = first_concurrency_second_hasData(QueryLevel.SINGLE, QueryLevel.SHARE);
        assertTrue(concurrency);
    }

    @Test
    void single_share_notData_notConcurrency() {

        //没有数据时，single查询不会阻塞single查询
        var concurrency = first_concurrency_second_noData(QueryLevel.SINGLE, QueryLevel.SHARE);
        assertFalse(concurrency);
    }

    /**
     * hold和single在有数据的情况下，互斥
     */
    @Test
    void hold_single_hasData_concurrency() {

        //hold查询会阻塞single查询
        var concurrency = first_concurrency_second_hasData(QueryLevel.HOLD, QueryLevel.SINGLE);
        assertTrue(concurrency);
    }

    /**
     * hold和single在没有数据的情况下，不互斥
     */
    @Test
    void hold_single_notData_noPromise() {

        // 对于没有数据情况下的  hold和single，框架不提供承诺
        // 也就是说，程序不应该在hold和single在没有数据的前提下做出任何逻辑
        // sqlserver的hold和single在没有数据时互斥，而postgresql则不互斥
        // 实际上也没有需求需要要用到这种处理
    }

    /**
     *
     */
    @Test
    void hold_share_hasData_concurrency() {

        var concurrency = first_concurrency_second_hasData(QueryLevel.HOLD, QueryLevel.SHARE);
        assertTrue(concurrency);

    }

    @Test
    void hold_share_noData_noPromise() {
        // 对于没有数据情况下的  hold和share，框架不提供承诺
        // 也就是说，程序不应该在hold和share在没有数据的前提下做出任何逻辑
        // sqlserver的hold和share在没有数据时互斥，而postgresql则不互斥
        // 实际上也没有需求需要要用到这种处理
    }


    /**
     * 共享查询之间不会阻塞
     */
    @Test
    void share_share() {
        var concurrency = first_concurrency_second_hasData(QueryLevel.SHARE, QueryLevel.SHARE);
        assertFalse(concurrency);
    }

    @Test
    void hold_hold_hasData_concurrency() {

        //不论是否有数据，hold之间都互斥
        var concurrency = first_concurrency_second_hasData(QueryLevel.HOLD, QueryLevel.HOLD);
        assertTrue(concurrency);

    }

    @Test
    void hold_hold_noData_concurrency() {

        //不论是否有数据，hold之间都互斥
        var concurrency = first_concurrency_second_noData(QueryLevel.HOLD, QueryLevel.HOLD);
        assertTrue(concurrency);
    }


    /**
     * 第一个查询阻塞第二个查询的测试
     *
     * @param first
     * @param second
     * @return
     */
    private static boolean first_concurrency_second_hasData(QueryLevel first, QueryLevel second) {

        return first_concurrency_second(() -> {
            queryAdmin(first);
        }, () -> {
            queryAdmin(second);
        });

    }


    private static boolean first_concurrency_second_noData(QueryLevel first, QueryLevel second) {
        return first_concurrency_second(() -> {
            queryCommon(first);
        }, () -> {
            queryCommon(second);
        });
    }


    @Test
    void update_none_notConcurrency() {

        //不论是否有数据，hold之间都互斥
        var concurrency = first_concurrency_second_noData(QueryLevel.HOLD, QueryLevel.HOLD);
        assertTrue(concurrency);
    }


    /**
     * firstAction 会阻塞 secondAction
     *
     * @param firstAction
     * @param secondAction
     * @return
     */
    private static boolean first_concurrency_second(Runnable firstAction, Runnable secondAction) {
        WrapperBoolean concurrency = new WrapperBoolean(false);

        var signal = new LatchSignal<Boolean>();

        Thread threadHold = asyncRun(() -> {
            DataContext.using(() -> {
                firstAction.run();

                try {
                    signal.wait(1, TimeUnit.SECONDS);
                    concurrency.setValue(false);
                } catch (Throwable ex) {
                    //由于会阻塞single，所以会超时
                    concurrency.setValue(true);
                }

            });
        });

        waitLimited();

        // single会被阻塞
        Thread threadNone = asyncRun(() -> {
            DataContext.using(() -> {
                secondAction.run();
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
     * secondAction 会阻塞 firstAction
     *
     * @param firstAction
     * @param secondAction
     * @return
     */
    private static boolean second_concurrency_first(Runnable firstAction, Runnable secondAction) {
        return first_concurrency_second(secondAction, firstAction);
    }

    private static void waitLimited() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void queryAdmin(QueryLevel level) {
        IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
        var obj = repository.findByEN("admin", level);
    }

    /**
     * 注意，没有common数据
     *
     * @param level
     */
    private static void queryCommon(QueryLevel level) {
        IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
        var obj = repository.findByEN("common", level);
    }

    public static Thread asyncRun(Runnable action) {
        Thread thread = new Thread(action);

        // 启动线程
        thread.start();

        return thread;
    }

}
