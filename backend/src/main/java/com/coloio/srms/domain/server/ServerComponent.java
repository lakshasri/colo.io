package com.coloio.srms.domain.server;

import com.coloio.srms.domain.enums.ServerStatus;

public interface ServerComponent {
    Long getServerId();
    String getHostname();
    String getIpAddress();
    int getUSize();
    int getCpuCores();
    int getRamGb();
    double getDiskTb();
    ServerStatus getStatus();
    ResourceSummary getResourceSummary();
    String getDescription();
}
