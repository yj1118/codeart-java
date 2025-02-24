package saga;

import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import subsystem.saga.NodeStatus;
import subsystem.saga.RegisterUserEvent;

import static org.junit.jupiter.api.Assertions.*;

public class NodeCount1Test {

    @BeforeAll
    public static void setup() {
        SAGAConfig.specifiedEvents(RegisterUserEvent.Name);
        TestLauncher.start();
    }

    @AfterAll
    public static void clean() {
        TestLauncher.stop();
    }

    @BeforeEach
    void setUp() {
        Common.init();
    }

    @Test
    void success() {
        System.out.println("---------- success ----------");
        var user = Common.exec(NodeStatus.SUCCESS);
        assertTrue(Common.isRegistered(user));
    }

    @Test
    void execBeforeThrowError() {
        System.out.println("---------- execBeforeThrowError ----------");
        try {
            Common.exec(NodeStatus.ERROR_BEFORE);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
        }
    }

    @Test
    void execAfterThrowError() {
        System.out.println("---------- execAfterThrowError ----------");
        try {
            Common.exec(NodeStatus.ERROR_AFTER);
            fail();
        } catch (Exception e) {

        } finally {
            assertFalse(Common.isRegistered());
        }
    }

}
