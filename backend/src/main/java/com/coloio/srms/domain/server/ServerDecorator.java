package com.coloio.srms.domain.server;

import com.coloio.srms.domain.enums.ServerStatus;

public abstract class ServerDecorator implements ServerComponent {

    protected final ServerComponent wrapped;

    protected ServerDecorator(ServerComponent wrapped) {
        this.wrapped = wrapped;
    }

    @Override public Long getServerId() { return wrapped.getServerId(); }
    @Override public String getHostname() { return wrapped.getHostname(); }
    @Override public String getIpAddress() { return wrapped.getIpAddress(); }
    @Override public int getUSize() { return wrapped.getUSize(); }
    @Override public int getCpuCores() { return wrapped.getCpuCores(); }
    @Override public int getRamGb() { return wrapped.getRamGb(); }
    @Override public double getDiskTb() { return wrapped.getDiskTb(); }
    @Override public ServerStatus getStatus() { return wrapped.getStatus(); }
    @Override public ResourceSummary getResourceSummary() { return wrapped.getResourceSummary(); }
    @Override public String getDescription() { return wrapped.getDescription(); }

    public ServerComponent getWrapped() { return wrapped; }
}
