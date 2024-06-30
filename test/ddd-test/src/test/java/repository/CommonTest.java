package repository;

import apros.codeart.ddd.DomainCollection;
import apros.codeart.ddd.DomainObject;
import apros.codeart.ddd.DomainProperty;
import apros.codeart.ddd.IDomainCollection;
import apros.codeart.ddd.launcher.TestLauncher;
import apros.codeart.ddd.metadata.PropertyMeta;
import apros.codeart.ddd.repository.DataContext;
import apros.codeart.ddd.repository.access.DataPortal;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import subsystem.account.AuthPlatform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import static apros.codeart.runtime.Util.propagate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CommonTest {

    @BeforeAll
    public static void setup() {
        TestLauncher.start();
    }

    @AfterAll
    public static void clean() {
        TestLauncher.stop();
    }

    @Test
    void collection() {

        var ap = new AuthPlatform(1, "test", "test");

        var strings = createList(ap, String.class, AuthPlatform.NameProperty);

        DomainCollection<Long> temp = (DomainCollection<Long>) strings;

        temp.add(1L);

        Assert.assertEquals(1, temp.size());

    }

    private Collection createList(DomainObject parent, Class<?> elementType, DomainProperty tip) {
        try {
//            var constructor = DomainCollection.class.getConstructor(Class.class, DomainProperty.class);
//            var collection = (IDomainCollection) constructor.newInstance(elementType,
//                    tip);
            var collection = new DomainCollection<>(elementType, tip);
            collection.setParent(parent);
            return (Collection) collection;


        } catch (Exception ex) {
            throw propagate(ex);
        }
    }

}
