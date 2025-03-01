package saga;

import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.*;
import subsystem.saga.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

public class NodeCount2Test {


    static {

    }


    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        Servers.open(1);
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
        var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.SUCCESS);
        Assertions.assertTrue(Common.isRegistered(user));
        Assertions.assertTrue(Common.isOpenAccount(user));
    }

    @Test
    void node_0_errorBefore() {
        System.out.println("---------- node_0_errorBefore ----------");
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
    void node_0_errorAfter() {
        System.out.println("---------- node_0_errorAfter ----------");
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
    void node_1_errorBefore() {
        System.out.println("---------- node_1_errorBefore ----------");
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
    void node_1_errorAfter() {
        System.out.println("---------- node_1_errorAfter ----------");
        try {
            var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.ERROR_AFTER);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }


    @Test
    void node_1_timeoutBefore() {
        System.out.println("---------- node_1_errorBefore ----------");
        try {
            var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.TIMEOUT_BEFORE);
            fail();
        } catch (Exception e) {

        } finally {
            try {
                Thread.sleep(10000);   //要给时间让整个流程走完，免得提早结束没有暴露出超时节点的问题
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }

    @Test
    void node_1_timeoutAfter() {
        System.out.println("---------- node_1_errorBefore ----------");
        try {
            var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.TIMEOUT_AFTER);
            fail();
        } catch (Exception e) {

        } finally {
            try {
                Thread.sleep(10000);   //要给时间让整个流程走完，免得提早结束没有暴露出超时节点的问题
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }

}
