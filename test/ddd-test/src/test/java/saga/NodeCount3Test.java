package saga;

import apros.codeart.ddd.launcher.DomainServer;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.*;
import subsystem.saga.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class NodeCount3Test {

    private static final DomainServer _server1 = new DomainServer("server1");

    private static final DomainServer _server2 = new DomainServer("server2");


    static {
        SAGAConfig.specifiedEvents(RegisterUserEvent.Name);
        _server1.domainEvents(OpenAccountEvent.Name);
        _server2.domainEvents(OpenWalletEvent.Name);
    }


    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        _server1.open();
        _server2.open();
    }

    @AfterAll
    public static void clean() {
        _server1.close();
        _server2.close();
        TestLauncher.stop();
    }

    @BeforeEach
    void setUp() {
        Common.init();
    }

    @Test
    void success() {
        System.out.println("---------- success ----------");
        var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.SUCCESS, NodeStatus.SUCCESS);
        Assertions.assertTrue(Common.isRegistered(user));
        Assertions.assertTrue(Common.isOpenAccount(user));
        Assertions.assertTrue(Common.isOpenWallet(user));
        Assertions.assertTrue(Common.isCheckEmail(user));
    }


}
