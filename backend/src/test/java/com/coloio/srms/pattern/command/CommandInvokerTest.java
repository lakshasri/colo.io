package com.coloio.srms.pattern.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommandInvokerTest {

    private CommandInvoker invoker;
    private List<String> log;

    // Simple test command that records execute/undo calls
    private ServerCommand makeCommand(String name) {
        return new AbstractServerCommand() {
            @Override
            public void execute() {
                log.add("execute:" + name);
                markExecuted();
            }
            @Override
            public void undo() {
                log.add("undo:" + name);
                markUndone();
            }
            @Override public String getDescription() { return name; }
            @Override public String getCommandType() { return "TEST"; }
        };
    }

    @BeforeEach
    void setUp() {
        invoker = new CommandInvoker();
        log = new ArrayList<>();
    }

    @Test
    void execute_runsCommand() {
        invoker.execute(makeCommand("cmd1"));
        assertEquals(List.of("execute:cmd1"), log);
    }

    @Test
    void undo_revertsLastCommand() {
        invoker.execute(makeCommand("cmd1"));
        invoker.undo();
        assertEquals(List.of("execute:cmd1", "undo:cmd1"), log);
    }

    @Test
    void redo_reExecutesUndoneCommand() {
        invoker.execute(makeCommand("cmd1"));
        invoker.undo();
        invoker.redo();
        assertEquals(List.of("execute:cmd1", "undo:cmd1", "execute:cmd1"), log);
    }

    @Test
    void undo_returnsFalseWhenEmpty() {
        assertFalse(invoker.undo());
    }

    @Test
    void redo_returnsFalseWhenEmpty() {
        assertFalse(invoker.redo());
    }

    @Test
    void execute_clearRedoStack() {
        invoker.execute(makeCommand("cmd1"));
        invoker.undo();
        assertTrue(invoker.canRedo());
        invoker.execute(makeCommand("cmd2")); // new command clears redo
        assertFalse(invoker.canRedo());
    }

    @Test
    void history_containsAllExecutedCommands() {
        invoker.execute(makeCommand("A"));
        invoker.execute(makeCommand("B"));
        invoker.execute(makeCommand("C"));
        assertEquals(3, invoker.historySize());
        List<String> history = invoker.getHistory();
        assertTrue(history.get(0).contains("C")); // newest first (stack order)
    }

    @Test
    void canUndo_trueAfterExecute() {
        assertFalse(invoker.canUndo());
        invoker.execute(makeCommand("cmd1"));
        assertTrue(invoker.canUndo());
    }

    @Test
    void canRedo_trueAfterUndo() {
        invoker.execute(makeCommand("cmd1"));
        assertFalse(invoker.canRedo());
        invoker.undo();
        assertTrue(invoker.canRedo());
    }
}
