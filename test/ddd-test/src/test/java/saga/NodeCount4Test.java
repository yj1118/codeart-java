package saga;

import apros.codeart.ddd.launcher.DomainServer;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.*;
import subsystem.saga.*;


public class NodeCount4Test {

    private static final DomainServer _server1 = new DomainServer("server1");

    private static final DomainServer _server2 = new DomainServer("server2");

    private static final DomainServer _server3 = new DomainServer("server3");

    private static final DomainServer _server4 = new DomainServer("server4");
    static {
        SAGAConfig.specifiedEvents(RegisterUserEvent.Name);
        _server1.domainEvents(OpenAccountEvent.Name);
        _server2.domainEvents(OpenWalletEvent.Name);
        _server3.domainEvents(CheckEmailEvent.Name);
        _server4.domainEvents(CompletedEvent.Name);
    }

    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        _server1.open();
        _server2.open();
        _server3.open();
        _server4.open();
    }

    @AfterAll
    public static void clean() {
        _server1.close();
        _server2.close();
        _server3.close();
        _server4.close();
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
