package com.coloio.srms.config;

import com.coloio.srms.domain.enums.ServerStatus;
import com.coloio.srms.domain.enums.UserRole;
import com.coloio.srms.entity.*;
import com.coloio.srms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(
            UserRepository users,
            ZoneRepository zones,
            RackRepository racks,
            ServerRepository servers,
            SlaRepository slas,
            PasswordEncoder encoder
    ) {
        return args -> {
            if (users.count() > 0) return; // already seeded

            // ── Users ────────────────────────────────────────────────
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPasswordHash(encoder.encode("admin123"));
            admin.setEmail("admin@colo.io");
            admin.setRole(UserRole.DC_ADMIN);
            admin.setActive(true);
            users.save(admin);

            UserEntity manager = new UserEntity();
            manager.setUsername("manager");
            manager.setPasswordHash(encoder.encode("admin123"));
            manager.setEmail("manager@colo.io");
            manager.setRole(UserRole.MANAGER);
            manager.setActive(true);
            users.save(manager);

            UserEntity tech = new UserEntity();
            tech.setUsername("tech");
            tech.setPasswordHash(encoder.encode("admin123"));
            tech.setEmail("tech@colo.io");
            tech.setRole(UserRole.TECHNICIAN);
            tech.setActive(true);
            users.save(tech);

            UserEntity customer = new UserEntity();
            customer.setUsername("customer");
            customer.setPasswordHash(encoder.encode("admin123"));
            customer.setEmail("customer@colo.io");
            customer.setRole(UserRole.CUSTOMER);
            customer.setActive(true);
            users.save(customer);

            // ── Zones ────────────────────────────────────────────────
            ZoneEntity zoneA = new ZoneEntity();
            zoneA.setName("Zone A");
            zoneA.setFloor(1);
            zoneA.setPowerBudgetKw(500.0);
            zoneA.setCoolingCapacity(600.0);
            zones.save(zoneA);

            ZoneEntity zoneB = new ZoneEntity();
            zoneB.setName("Zone B");
            zoneB.setFloor(2);
            zoneB.setPowerBudgetKw(400.0);
            zoneB.setCoolingCapacity(480.0);
            zones.save(zoneB);

            // ── Racks ────────────────────────────────────────────────
            RackEntity rack1 = new RackEntity();
            rack1.setName("RACK-A01");
            rack1.setZone(zoneA);
            rack1.setLocation("Row 1, Slot 1");
            rack1.setTotalUSpace(42);
            rack1.setUsedUSpace(12);
            rack1.setMaxPowerKw(20.0);
            rack1.setCurrentPowerKw(6.4);
            racks.save(rack1);

            RackEntity rack2 = new RackEntity();
            rack2.setName("RACK-A02");
            rack2.setZone(zoneA);
            rack2.setLocation("Row 1, Slot 2");
            rack2.setTotalUSpace(42);
            rack2.setUsedUSpace(28);
            rack2.setMaxPowerKw(20.0);
            rack2.setCurrentPowerKw(14.2);
            racks.save(rack2);

            RackEntity rack3 = new RackEntity();
            rack3.setName("RACK-B01");
            rack3.setZone(zoneB);
            rack3.setLocation("Row 1, Slot 1");
            rack3.setTotalUSpace(42);
            rack3.setUsedUSpace(5);
            rack3.setMaxPowerKw(15.0);
            rack3.setCurrentPowerKw(2.1);
            racks.save(rack3);

            // ── Servers ──────────────────────────────────────────────
            ServerEntity s1 = new ServerEntity();
            s1.setHostname("web-prod-01");
            s1.setIpAddress("10.0.1.10");
            s1.setCpuCores(32);
            s1.setRamGb(128);
            s1.setDiskTb(2.0);
            s1.setUSize(2);
            s1.setUPosition(1);
            s1.setStatus(ServerStatus.OPERATIONAL);
            s1.setRack(rack1);
            s1.setCustomer(customer);
            s1.setInstalledDate(LocalDate.now().minusMonths(6));
            servers.save(s1);

            ServerEntity s2 = new ServerEntity();
            s2.setHostname("db-prod-01");
            s2.setIpAddress("10.0.1.11");
            s2.setCpuCores(64);
            s2.setRamGb(512);
            s2.setDiskTb(10.0);
            s2.setUSize(4);
            s2.setUPosition(3);
            s2.setStatus(ServerStatus.OPERATIONAL);
            s2.setRack(rack1);
            s2.setCustomer(customer);
            s2.setInstalledDate(LocalDate.now().minusMonths(6));
            servers.save(s2);

            ServerEntity s3 = new ServerEntity();
            s3.setHostname("cache-01");
            s3.setIpAddress("10.0.1.20");
            s3.setCpuCores(16);
            s3.setRamGb(64);
            s3.setDiskTb(1.0);
            s3.setUSize(1);
            s3.setUPosition(7);
            s3.setStatus(ServerStatus.OPERATIONAL);
            s3.setRack(rack1);
            s3.setInstalledDate(LocalDate.now().minusMonths(3));
            servers.save(s3);

            ServerEntity s4 = new ServerEntity();
            s4.setHostname("compute-01");
            s4.setIpAddress("10.0.2.10");
            s4.setCpuCores(128);
            s4.setRamGb(1024);
            s4.setDiskTb(20.0);
            s4.setUSize(4);
            s4.setUPosition(1);
            s4.setStatus(ServerStatus.MAINTENANCE);
            s4.setRack(rack2);
            s4.setInstalledDate(LocalDate.now().minusMonths(12));
            servers.save(s4);

            ServerEntity s5 = new ServerEntity();
            s5.setHostname("storage-01");
            s5.setIpAddress("10.0.2.20");
            s5.setCpuCores(8);
            s5.setRamGb(32);
            s5.setDiskTb(100.0);
            s5.setUSize(4);
            s5.setUPosition(5);
            s5.setStatus(ServerStatus.OPERATIONAL);
            s5.setRack(rack2);
            s5.setInstalledDate(LocalDate.now().minusMonths(8));
            servers.save(s5);

            ServerEntity s6 = new ServerEntity();
            s6.setHostname("api-staging-01");
            s6.setIpAddress("10.0.3.10");
            s6.setCpuCores(8);
            s6.setRamGb(32);
            s6.setDiskTb(0.5);
            s6.setUSize(1);
            s6.setStatus(ServerStatus.UNALLOCATED);
            s6.setInstalledDate(LocalDate.now().minusDays(10));
            servers.save(s6);

            // ── SLA ──────────────────────────────────────────────────
            SlaEntity sla = new SlaEntity();
            sla.setCustomer(customer);
            sla.setName("Gold SLA");
            sla.setDescription("99.9% uptime, 15-min response, 4-hr resolution");
            sla.setUptimeGuaranteePct(99.9);
            sla.setResponseTimeMinutes(15);
            sla.setResolutionTimeHours(4);
            sla.setStartDate(LocalDate.now().minusMonths(1));
            sla.setEndDate(LocalDate.now().plusYears(1));
            sla.setStatus("ACTIVE");
            slas.save(sla);

            System.out.println("[SEEDER] Demo data loaded — 4 users, 2 zones, 3 racks, 6 servers, 1 SLA");
        };
    }
}
