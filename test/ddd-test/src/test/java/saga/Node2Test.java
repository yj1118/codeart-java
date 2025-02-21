package saga;

import apros.codeart.ddd.command.EventCallable;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.dto.DTObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import subsystem.saga.Accumulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Node2Test {

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
        var result = EventCallable.execute("SetValue2Event", arg);

        var value = result.getInt("value");
        assertEquals(3, value);
    }

}
