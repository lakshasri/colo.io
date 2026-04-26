package com.coloio.srms.pattern.command;

public interface ServerCommand {
    void execute();
    void undo();
    String getDescription();
    String getCommandType();
}
