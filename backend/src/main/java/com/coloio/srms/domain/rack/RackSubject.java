package com.coloio.srms.domain.rack;

public interface RackSubject {
    void addObserver(RackObserver observer);
    void removeObserver(RackObserver observer);
    void notifyObservers(RackEvent event);
}
