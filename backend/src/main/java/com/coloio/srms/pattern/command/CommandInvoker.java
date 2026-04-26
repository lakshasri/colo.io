package com.coloio.srms.pattern.command;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Component
public class CommandInvoker {

    private final Deque<ServerCommand> history = new ArrayDeque<>();
    private final Deque<ServerCommand> redoStack = new ArrayDeque<>();

    public void execute(ServerCommand command) {
        command.execute();
        history.push(command);
        redoStack.clear();
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        ServerCommand cmd = history.pop();
        cmd.undo();
        redoStack.push(cmd);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;
        ServerCommand cmd = redoStack.pop();
        cmd.execute();
        history.push(cmd);
        return true;
    }

    public List<String> getHistory() {
        List<String> list = new ArrayList<>();
        for (ServerCommand cmd : history) {
            list.add(cmd.getCommandType() + ": " + cmd.getDescription());
        }
        return list;
    }

    public int historySize() { return history.size(); }
    public boolean canUndo() { return !history.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
}
