package com.coloio.srms.repository;

import com.coloio.srms.entity.ChecklistItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistItemRepository extends JpaRepository<ChecklistItemEntity, Long> {
    List<ChecklistItemEntity> findByTicket_TicketIdOrderByItemIdAsc(Long ticketId);
    long countByTicket_TicketIdAndCompletedTrue(Long ticketId);
    long countByTicket_TicketId(Long ticketId);
}
