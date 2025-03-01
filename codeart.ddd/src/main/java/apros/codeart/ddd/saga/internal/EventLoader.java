package apros.codeart.ddd.saga.internal;

import static apros.codeart.i18n.Language.strings;

import java.util.ArrayList;

import apros.codeart.ddd.saga.SAGAConfig;
import apros.codeart.ddd.saga.SAGAEvents;
import apros.codeart.echo.rpc.RPCEvents;
import apros.codeart.runtime.TypeUtil;
import apros.codeart.util.ListUtil;
import com.google.common.collect.Iterables;

import apros.codeart.App;
import apros.codeart.ddd.saga.DomainEvent;
import apros.codeart.runtime.Activator;
import apros.codeart.util.SafeAccessImpl;

public final class EventLoader {
    private EventLoader() {
    }

    private static Iterable<DomainEvent> _events;

    public static Iterable<DomainEvent> events() {
        return _events;
    }

    public static DomainEvent find(String name, boolean throwError) {
        var event = ListUtil.find(_events, (e) -> {
            return e.name().equals(name);
        });

        if (event == null && throwError) {
            throw new IllegalStateException(strings("apros.codeart.ddd", "NoFoundDomainEvent", name));
        }
        return event;
    }

    /**
     * 加载所有领域对象的元数据
     */
    public static void load() {
        _events = findEvents();
    }

    private static Iterable<DomainEvent> findEvents() {
        var specified = SAGAConfig.specifiedEvents() != null && Iterables.size(SAGAConfig.specifiedEvents()) > 0;

        var findedTypes = Activator.getSubTypesOf(DomainEvent.class, App.archives());
        ArrayList<DomainEvent> events = new ArrayList<>(Iterables.size(findedTypes));
        for (var findedType : findedTypes) {
            if (TypeUtil.isAbstract(findedType)) continue;

            var event = SafeAccessImpl.createSingleton(findedType);

            if (specified) {
                // 服务器只加载指定名称的领域事件
                if (ListUtil.contains(SAGAConfig.specifiedEvents(), (eventName) -> eventName.equals(event.name()))) {
                    events.add(event);
                    SAGAEvents.raiseEventRegistered(event, new SAGAEvents.RegisteredEventArgs(event.name()));
                }
            } else {
                events.add(event);
                SAGAEvents.raiseEventRegistered(event, new SAGAEvents.RegisteredEventArgs(event.name()));
            }
        }
        return events;
    }

}
