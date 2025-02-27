package saga;

import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.*;
import subsystem.saga.*;


public class NodeCount5Test {

    static {
        SAGAConfig.specifiedEvents(RegisterUserEvent.Name);
    }

    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        Servers.open(4);
    }

    @AfterAll
    public static void clean() {
        Servers.close();
        TestLauncher.stop();
    }

    @BeforeEach
    void setUp() {
        Common.init();
    }

    @Test
    void success() {
        System.out.println("---------- success ----------");
        var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.SUCCESS, NodeStatus.SUCCESS, NodeStatus.SUCCESS, NodeStatus.SUCCESS);
        Assertions.assertTrue(Common.isRegistered(user));
        Assertions.assertTrue(Common.isOpenAccount(user));
        Assertions.assertTrue(Common.isOpenWallet(user));
        Assertions.assertTrue(Common.isCheckEmail(user));
        Assertions.assertTrue(Common.isCompleted(user));
    }

}
