package com.coloio.srms.service;

import com.coloio.srms.entity.SlaEntity;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.repository.SlaRepository;
import com.coloio.srms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock SlaRepository slaRepository;
    @Mock UserRepository userRepository;
    @InjectMocks SlaService slaService;

    private UserEntity customer() {
        UserEntity u = new UserEntity();
        u.setUserId(1L);
        u.setUsername("customer1");
        return u;
    }

    private SlaEntity sla() {
        SlaEntity s = new SlaEntity();
        s.setSlaId(10L);
        s.setName("Gold SLA");
        s.setStatus("ACTIVE");
        s.setStartDate(LocalDate.now().minusDays(30));
        s.setEndDate(LocalDate.now().plusDays(335));
        s.setUptimeGuaranteePct(99.9);
        s.setResponseTimeMinutes(15);
        s.setResolutionTimeHours(4);
        s.setCustomer(customer());
        return s;
    }

    @Test
    void create_persistsSlaWithActiveStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer()));
        when(slaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SlaEntity result = slaService.create(1L, "Gold SLA", "desc",
                99.9, 15, 4, LocalDate.now(), LocalDate.now().plusYears(1));

        assertEquals("ACTIVE", result.getStatus());
        assertEquals("Gold SLA", result.getName());
        assertEquals(99.9, result.getUptimeGuaranteePct());
        verify(slaRepository).save(any());
    }

    @Test
    void create_throwsWhenCustomerNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> slaService.create(99L, "Test", "", 99.0, 10, 2,
                        LocalDate.now(), null));
    }

    @Test
    void getAll_delegatesToRepository() {
        when(slaRepository.findAllByOrderByStartDateDesc()).thenReturn(List.of(sla()));
        List<SlaEntity> result = slaService.getAll();
        assertEquals(1, result.size());
        assertEquals("Gold SLA", result.get(0).getName());
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(slaRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> slaService.getById(999L));
    }

    @Test
    void updateStatus_setsStatusOnActiveSla() {
        SlaEntity existing = sla();
        when(slaRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(slaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SlaEntity result = slaService.updateStatus(10L, "SUSPENDED");
        assertEquals("SUSPENDED", result.getStatus());
    }

    @Test
    void updateStatus_autoExpiresWhenEndDatePast() {
        SlaEntity expired = sla();
        expired.setEndDate(LocalDate.now().minusDays(1)); // end date in past
        when(slaRepository.findById(10L)).thenReturn(Optional.of(expired));
        when(slaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SlaEntity result = slaService.updateStatus(10L, "ACTIVE");
        assertEquals("EXPIRED", result.getStatus());
    }

    @Test
    void delete_callsRepository() {
        doNothing().when(slaRepository).deleteById(10L);
        slaService.delete(10L);
        verify(slaRepository).deleteById(10L);
    }
}
