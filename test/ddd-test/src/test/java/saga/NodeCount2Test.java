package saga;

import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.launcher.DomainServer;
import apros.codeart.ddd.saga.SAGAConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import subsystem.saga.Accumulator;
import subsystem.saga.NodeStatus;
import subsystem.saga.RemoteNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class NodeCount2Test {

    private static final DomainServer _server1 = new DomainServer("server1");

    static {
        SAGAConfig.specifiedEvents(new String[]{"RegisterUserEvent"});
        _server1.domainEvents(new String[]{"AddUpEvent"});
    }


    @BeforeAll
    public static void setup() {
        TestLauncher.start();
        _server1.open();
    }

    @AfterAll
    public static void clean() {
        TestLauncher.stop();
        _server1.close();
    }

    @BeforeEach
    void setUp() {
        Common.init();
    }

    @Test
    void success() {
        var value = Common.exec(new Common.Config(3,
                new RemoteNode[]{RemoteNode.success("AddUpEvent")}));
        assertEquals(4, value);
    }

    @Test
    void errorBefore() {
        try {
            var value = Common.exec(new Common.Config(3, true, false,
                    new RemoteNode[]{RemoteNode.success("AddUpEvent")}));
            fail();
        } catch (Exception e) {

        } finally {
            assertEquals(0, Accumulator.Instance.value());
        }
    }

    @Test
    void errorAfter() {
        try {
            var value = Common.exec(new Common.Config(3, false, true,
                    new RemoteNode[]{RemoteNode.success("AddUpEvent")}));
            fail();
        } catch (Exception e) {

        } finally {
            assertEquals(0, Accumulator.Instance.value());
        }
    }


//    @Test
//    void remoteNode1Error() {
//        try {
//            var value = Common.exec(new Common.Config(3,
//                    new RemoteNode[]{RemoteNode.success("AddUpEvent")}));
//            fail();
//        } catch (Exception e) {
//
//        } finally {
//            assertEquals(0, Accumulator.Instance.value());
//        }
//    }


}
