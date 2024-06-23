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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class GetIdentityTest {

    @BeforeAll
    public static void setup() {
        TestLauncher.start();
    }

    @AfterAll
    public static void clean() {
        TestLauncher.stop();
    }

    @Test
    void concurrent() {

        ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<>();

        Thread thread1 = asyncRun(() -> {
            for(var i=0;i<1000;i++){
                DataContext.using(() -> {
                    var id = DataPortal.getIdentity(AuthPlatform.class);
                    queue.add(id);
                });
            }
        });

        Thread thread2 = asyncRun(() -> {
            for(var i=0;i<1000;i++){
                DataContext.using(() -> {
                    var id = DataPortal.getIdentity(AuthPlatform.class);
                    queue.add(id);
                });
            }
        });

        Thread thread3 = asyncRun(() -> {
            for(var i=0;i<1000;i++){
                DataContext.using(() -> {
                    var id = DataPortal.getIdentity(AuthPlatform.class);
                    queue.add(id);
                });
            }
        });


        try {
            thread1.join();
            thread2.join();
            thread3.join();

            ArrayList<Long> list = new ArrayList<>(queue);

            // 对临时列表进行排序
            Collections.sort(list);

            assertEquals(3000, list.size());

            assertEquals(1, list.getFirst());
            assertEquals(3000, list.getLast());

            var set = new LinkedHashSet<>(queue);

            // 将去重后的元素复制到一个列表中
            var t = new ArrayList<>(set);
            assertEquals(3000, t.size());


        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public static Thread asyncRun(Runnable action) {
        Thread thread = new Thread(action);

        // 启动线程
        thread.start();

        return thread;
    }

}
