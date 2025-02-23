package saga;

import apros.codeart.ddd.command.EventCallable;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.dto.DTObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import subsystem.saga.Accumulator;

import static org.junit.jupiter.api.Assertions.*;

public class NodeCount1Test {

    @BeforeAll
    public static void setup() {
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
        var value = Common.exec(new Common.Config(3));
        assertEquals(3, value);
    }

    @Test
    void execBeforeThrowError() {
        try {
            Common.exec(new Common.Config(3, true, false));
            fail();
        } catch (Exception e) {

        } finally {
            assertEquals(0, Accumulator.Instance.value());
        }
    }

    @Test
    void error_exec_after() {
        try {
            Common.exec(new Common.Config(3, false, true));
            fail();
        } catch (Exception e) {

        } finally {
            assertEquals(0, Accumulator.Instance.value());
        }
    }

}
