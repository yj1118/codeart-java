package saga;

import apros.codeart.ddd.command.EventCallable;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.metadata.internal.ObjectMetaLoader;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.dto.DTObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import subsystem.account.AuthPlatform;
import subsystem.saga.Accumulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Node1Test {

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
        Accumulator.Instance.setValue(0);
    }

    @Test
    void success() {
        var arg = DTObject.editable();
        arg.setInt("value", 3);
        var result = EventCallable.execute("SetValue1Event", arg);

        var value = result.getInt("value");
        assertEquals(3, value);
    }

    @Test
    void error_exec_before() {
        var arg = DTObject.editable();
        arg.setInt("value", 3);
        arg.setBoolean("before_error", true);

        try {
            var result = EventCallable.execute("SetValue1Event", arg);
            result.getInt("value");
            assertTrue(false);
        } catch (Exception e) {

        } finally {
            assertEquals(0, Accumulator.Instance.value());
        }
    }

    @Test
    void error_exec_after() {

        var arg = DTObject.editable();
        arg.setInt("value", 3);
        arg.setBoolean("after_error", true);

        try {
            var result = EventCallable.execute("SetValue1Event", arg);
            result.getInt("value");
            assertTrue(false);
        } catch (Exception e) {

        } finally {
            assertEquals(0, Accumulator.Instance.value());
        }
    }

}
