package saga;

import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.saga.SAGAConfig;
import apros.codeart.util.thread.ThreadUtil;
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
        // 注意，这个很重要，本地服务器在后续服务器超时后，本地服务器会中断执行，
        // 导致后续进程模拟的服务器也强制中断，结果消息都没处理完就残留在队列里了
        // 所以要多等一会
        ThreadUtil.sleepSeconds(3);
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
            ThreadUtil.sleepSeconds(2);
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }

        // 注意，这个很重要，本地服务器在后续服务器超时后，本地服务器会中断执行，
        // 导致后续进程模拟的服务器也强制中断，结果消息都没处理完就残留在队列里了
        // 所以要多等一会
        ThreadUtil.sleepSeconds(2);
    }

    @Test
    void node_1_timeoutAfter() {
        System.out.println("---------- node_1_errorBefore ----------");
        try {
            var user = Common.exec(NodeStatus.SUCCESS, NodeStatus.TIMEOUT_AFTER);
            fail();
        } catch (Exception e) {

        } finally {
            ThreadUtil.sleepSeconds(2);
            assertFalse(Common.isRegistered());
            assertFalse(Common.isOpenAccount());
        }
    }

}
