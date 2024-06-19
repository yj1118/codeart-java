package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.Repository;
import apros.codeart.ddd.repository.TransactionStatus;
import subsystem.account.IAuthPlatformRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import apros.codeart.ddd.launcher.TestLauncher;

public class DataContextLockLevelTest {

    @BeforeAll
    public static void setup() {
        TestLauncher.start();
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
            var obj = repository.findByEN("admin", QueryLevel.None);

            var ctx = DataContext.getCurrent();
            assertEquals(TransactionStatus.None, ctx.transactionStatus());
        });
    }


}
