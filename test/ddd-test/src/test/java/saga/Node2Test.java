package saga;

import apros.codeart.ddd.command.EventCallable;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.launcher.DomainServer;
import apros.codeart.dto.DTObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import subsystem.saga.Accumulator;

import java.io.IOException;

public class Node2Test {

    private static final DomainServer _server1 = new DomainServer("server1");

    static {
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
        Accumulator.Instance.setValue(0);
    }

    @Test
    void success() {
//        var arg = DTObject.editable();
//        arg.setInt("value", 3);
//        var result = EventCallable.execute("SetValue2Event", arg);
//
//        var value = result.getInt("value");
//        assertEquals(3, value);
    }

}
