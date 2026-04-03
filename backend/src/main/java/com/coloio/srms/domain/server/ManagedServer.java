package com.coloio.srms.domain.server;

import java.time.LocalDate;

public class ManagedServer extends ServerDecorator {

    private LocalDate warrantyExpiry;
    private int maintenanceCount;

    public ManagedServer(ServerComponent wrapped) {
        super(wrapped);
    }

    public ManagedServer(ServerComponent wrapped, LocalDate warrantyExpiry) {
        super(wrapped);
        this.warrantyExpiry = warrantyExpiry;
    }

    public boolean isUnderWarranty() {
        return warrantyExpiry != null && LocalDate.now().isBefore(warrantyExpiry);
    }

    public long getDaysUntilWarrantyExpiry() {
        if (warrantyExpiry == null) return -1;
        return LocalDate.now().until(warrantyExpiry).getDays();
    }

    public void recordMaintenance() {
        this.maintenanceCount++;
    }

    @Override
    public String getDescription() {
        return "Managed[" + wrapped.getDescription() + "]";
    }

    public LocalDate getWarrantyExpiry() { return warrantyExpiry; }
    public void setWarrantyExpiry(LocalDate warrantyExpiry) { this.warrantyExpiry = warrantyExpiry; }
    public int getMaintenanceCount() { return maintenanceCount; }
}
