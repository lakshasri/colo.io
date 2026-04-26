package com.coloio.srms.pattern.command;

import java.time.LocalDateTime;

public abstract class AbstractServerCommand implements ServerCommand {

    private final LocalDateTime createdAt = LocalDateTime.now();
    private boolean executed = false;

    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isExecuted() { return executed; }

    protected void markExecuted() { this.executed = true; }
    protected void markUndone() { this.executed = false; }
}
