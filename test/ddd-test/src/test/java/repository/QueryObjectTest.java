package repository;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.TransactionStatus;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.util.WrapperBoolean;
import apros.codeart.util.concurrent.LatchSignal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import subsystem.account.AuthPlatform;
import subsystem.account.IAuthPlatformRepository;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 我们提供对单元测试里实现的锁关系承诺
 *
 */
public class QueryObjectTest {

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
    void query_by_id() {
        long id = DataContext.using(() -> {
            IAuthPlatformRepository repository = Repository.create(IAuthPlatformRepository.class);
            var obj = repository.findByEN("admin",QueryLevel.NONE);
            return obj.id();
        });

        DataContext.using(() -> {
            var obj = Repository.find(AuthPlatform.class,id,QueryLevel.NONE);
            assertEquals("admin",obj.en());
            assertEquals("系统控制",obj.name());
        });

    }



}
