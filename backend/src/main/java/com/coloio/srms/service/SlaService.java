package com.coloio.srms.service;

import com.coloio.srms.entity.SlaEntity;
import com.coloio.srms.entity.UserEntity;
import com.coloio.srms.repository.SlaRepository;
import com.coloio.srms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class SlaService {

    private final SlaRepository slaRepository;
    private final UserRepository userRepository;

    public SlaService(SlaRepository slaRepository, UserRepository userRepository) {
        this.slaRepository = slaRepository;
        this.userRepository = userRepository;
    }

    public SlaEntity create(Long customerId, String name, String description,
                             double uptimePct, int responseMinutes, int resolutionHours,
                             LocalDate startDate, LocalDate endDate) {
        UserEntity customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));
        SlaEntity sla = new SlaEntity();
        sla.setCustomer(customer);
        sla.setName(name);
        sla.setDescription(description);
        sla.setUptimeGuaranteePct(uptimePct);
        sla.setResponseTimeMinutes(responseMinutes);
        sla.setResolutionTimeHours(resolutionHours);
        sla.setStartDate(startDate);
        sla.setEndDate(endDate);
        sla.setStatus("ACTIVE");
        return slaRepository.save(sla);
    }

    @Transactional(readOnly = true)
    public List<SlaEntity> getAll() {
        return slaRepository.findAllByOrderByStartDateDesc();
    }

    @Transactional(readOnly = true)
    public SlaEntity getById(Long id) {
        return slaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("SLA not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SlaEntity> getByCustomer(Long customerId) {
        return slaRepository.findByCustomer_UserIdOrderByStartDateDesc(customerId);
    }

    public SlaEntity updateStatus(Long id, String status) {
        SlaEntity sla = getById(id);
        sla.setStatus(status);
        // auto-expire if end date passed
        if (sla.getEndDate() != null && LocalDate.now().isAfter(sla.getEndDate())) {
            sla.setStatus("EXPIRED");
        }
        return slaRepository.save(sla);
    }

    public void delete(Long id) {
        slaRepository.deleteById(id);
    }
}
