package com.coloio.srms.domain.rack;

public interface RackObserver {
    void onRackEvent(RackEvent event);
}
