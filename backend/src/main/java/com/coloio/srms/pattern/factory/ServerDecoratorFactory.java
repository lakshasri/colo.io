package com.coloio.srms.pattern.factory;

import com.coloio.srms.domain.server.*;

import java.time.LocalDate;

/**
 * Wraps a BaseServer with the appropriate decorator chain based on allocation state.
 *
 * Allocation states:
 *   BARE        → BaseServer only
 *   ALLOCATED   → MonitoredServer(AlertableServer(BaseServer))
 *   PROVISIONED → ManagedServer(MonitoredServer(AlertableServer(BaseServer)))
 */
public class ServerDecoratorFactory {

    public enum AllocationState { BARE, ALLOCATED, PROVISIONED }

    private ServerDecoratorFactory() {}

    public static ServerComponent wrap(BaseServer base, AllocationState state) {
        return switch (state) {
            case BARE        -> base;
            case ALLOCATED   -> new MonitoredServer(new AlertableServer(base));
            case PROVISIONED -> new ManagedServer(
                                    new MonitoredServer(
                                        new AlertableServer(base)),
                                    LocalDate.now().plusYears(3)  // 3-year default warranty
                                );
        };
    }

    /** Convenience: unwrap to the underlying BaseServer regardless of decorator depth. */
    public static BaseServer unwrap(ServerComponent component) {
        ServerComponent current = component;
        while (current instanceof ServerDecorator decorator) {
            current = decorator.getWrapped();
        }
        return (BaseServer) current;
    }
}
