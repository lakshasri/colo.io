package com.coloio.srms.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "checklist_items")
public class ChecklistItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private MaintenanceTicketEntity ticket;

    @Column(nullable = false, length = 200)
    private String description;

    private boolean completed = false;

    @Column(length = 100)
    private String completedBy;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public MaintenanceTicketEntity getTicket() { return ticket; }
    public void setTicket(MaintenanceTicketEntity ticket) { this.ticket = ticket; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCompletedBy() { return completedBy; }
    public void setCompletedBy(String completedBy) { this.completedBy = completedBy; }
}
