package saga;

import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.launcher.DomainServer;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.*;
import subsystem.saga.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class NodeCount2Test {

    private static final DomainServer _server1 = new DomainServer("server1");

    static {
        SAGAConfig.specifiedEvents(new String[]{RegisterUserEvent.Name});
        _server1.domainEvents(new String[]{OpenAccountEvent.Name});
    }


    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        _server1.open();
    }

    @AfterAll
    public static void clean() {
        _server1.close();
        TestLauncher.stop();
    }

    @BeforeEach
    void setUp() {
        Common.init();
    }

    @Test
    void success() {
        System.out.println("---------- success ----------");
        var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.SUCCESS);
        Assertions.assertTrue(Common.isRegistered(user));
        Assertions.assertTrue(Common.isOpenAccount(user));
    }

    @Test
    void node_1_errorBefore() {
        System.out.println("---------- node_1_errorBefore ----------");
        try {
            var user = Common.exec(NodeStatus.ERROR_BEFORE, NodeStatus.SUCCESS);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }

    @Test
    void node_1_errorAfter() {
        System.out.println("---------- node_1_errorAfter ----------");
        try {
            var user = Common.exec(NodeStatus.ERROR_AFTER, NodeStatus.SUCCESS);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }


    @Test
    void node_2_errorBefore() {
        System.out.println("---------- node_2_errorBefore ----------");
        try {
            var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.ERROR_BEFORE);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }

    @Test
    void node_2_errorAfter() {
        System.out.println("---------- node_2_errorAfter ----------");
        try {
            var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.ERROR_AFTER);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }

}
