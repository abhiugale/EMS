package com.ems.modules.machine.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.common.exception.ValidationException;
import com.ems.common.util.DateTimeUtil;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import com.ems.modules.machine.dto.*;
import com.ems.modules.machine.entity.Machine;
import com.ems.modules.machine.repository.MachineRepository;
import com.ems.modules.energy.repository.EnergyReadingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineService {

    private final MachineRepository machineRepository;
    private final FactoryRepository factoryRepository;
    private final EnergyReadingRepository energyReadingRepository;

    @Transactional
    public MachineDto createMachine(UUID factoryId, CreateMachineRequest request) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        if (machineRepository.existsByFactoryIdAndName(factoryId, request.getName())) {
            throw new ValidationException("Machine with this name already exists in the factory");
        }

        Machine machine = Machine.builder()
                .factory(factory)
                .name(request.getName())
                .department(request.getDepartment())
                .machineType(request.getMachineType())
                .baselineKwh(request.getBaselineKwh())
                .isActive(true)
                .build();

        Machine savedMachine = machineRepository.save(machine);
        return mapToDto(savedMachine);
    }

    @Transactional(readOnly = true)
    public List<MachineDto> getMachinesByFactory(UUID factoryId) {
        if (!factoryRepository.existsById(factoryId)) {
            throw new ResourceNotFoundException("Factory not found");
        }
        return machineRepository.findByFactoryId(factoryId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MachineDto getMachineById(UUID id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));
        return mapToDto(machine);
    }

    @Transactional
    public MachineDto updateMachine(UUID id, CreateMachineRequest request) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));

        if (!machine.getName().equals(request.getName()) &&
                machineRepository.existsByFactoryIdAndName(machine.getFactory().getId(), request.getName())) {
            throw new ValidationException("Machine with this name already exists in this factory");
        }

        machine.setName(request.getName());
        machine.setDepartment(request.getDepartment());
        machine.setMachineType(request.getMachineType());
        machine.setBaselineKwh(request.getBaselineKwh());

        Machine updatedMachine = machineRepository.save(machine);
        return mapToDto(updatedMachine);
    }

    @Transactional
    public void deactivateMachine(UUID id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine not found"));
        machine.setActive(false);
        machineRepository.save(machine);
    }

    @Transactional(readOnly = true)
    public List<MachineCardDto> getMachineCards(UUID factoryId) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        List<Machine> machines = machineRepository.findByFactoryId(factoryId);

        // Fetch latest readings per machine
        List<Object[]> latestReadings = energyReadingRepository.findLatestReadingPerMachine(factoryId);
        Map<UUID, Object[]> latestMap = new HashMap<>();
        for (Object[] row : latestReadings) {
            latestMap.put((UUID) row[0], row);
        }

        // Fetch today's metrics
        String tz = factory.getTimezone();
        Instant start = DateTimeUtil.getStartOfDay(LocalDate.now(), tz);
        Instant end = DateTimeUtil.getEndOfDay(LocalDate.now(), tz);
        List<Object[]> todayMetrics = energyReadingRepository.findTodayMetricsPerMachine(factoryId, start, end);
        Map<UUID, Object[]> todayMetricsMap = new HashMap<>();
        for (Object[] row : todayMetrics) {
            todayMetricsMap.put((UUID) row[0], row);
        }

        List<MachineCardDto> cards = new ArrayList<>();
        Instant fifteenMinsAgo = Instant.now().minusSeconds(900);

        for (Machine machine : machines) {
            Object[] latest = latestMap.get(machine.getId());
            Object[] todayMet = todayMetricsMap.get(machine.getId());

            BigDecimal todayKwh = BigDecimal.ZERO;
            Integer partsToday = 0;
            if (todayMet != null) {
                todayKwh = (BigDecimal) todayMet[1];
                partsToday = ((Number) todayMet[2]).intValue();
            }

            BigDecimal activeKw = BigDecimal.ZERO;
            Instant lastSeen = null;
            if (latest != null) {
                lastSeen = (Instant) latest[1];
                activeKw = (BigDecimal) latest[2];
            }

            // Derive machine status
            String status = "OFF";
            if (machine.isActive() && lastSeen != null && lastSeen.isAfter(fifteenMinsAgo)) {
                if (activeKw.compareTo(BigDecimal.valueOf(5.0)) > 0) {
                    status = "RUNNING";
                } else {
                    status = "IDLE";
                }
            }

            // Efficiency Pct calculation based on baseline kWh vs actual kWh per part
            BigDecimal efficiencyPct = BigDecimal.valueOf(100.0);
            BigDecimal kwhPerPart = BigDecimal.ZERO;
            if (partsToday > 0 && todayKwh.compareTo(BigDecimal.ZERO) > 0) {
                kwhPerPart = todayKwh.divide(BigDecimal.valueOf(partsToday), 3, RoundingMode.HALF_UP);
                if (machine.getBaselineKwh().compareTo(BigDecimal.ZERO) > 0) {
                    // if actual kwhPerPart is lower than baseline, efficiency is > 100%
                    efficiencyPct = machine.getBaselineKwh()
                            .divide(kwhPerPart, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100.0))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            cards.add(MachineCardDto.builder()
                    .id(machine.getId())
                    .name(machine.getName())
                    .department(machine.getDepartment())
                    .machineType(machine.getMachineType())
                    .status(status)
                    .todayKwh(todayKwh)
                    .kwhPerPart(kwhPerPart)
                    .partsToday(partsToday)
                    .efficiencyPct(efficiencyPct)
                    .anomalyFlag(false) // will be updated by ml/anomaly checks
                    .lastSeen(lastSeen)
                    .build());
        }

        return cards;
    }

    private MachineDto mapToDto(Machine machine) {
        return MachineDto.builder()
                .id(machine.getId())
                .factoryId(machine.getFactory().getId())
                .name(machine.getName())
                .department(machine.getDepartment())
                .machineType(machine.getMachineType())
                .baselineKwh(machine.getBaselineKwh())
                .isActive(machine.isActive())
                .createdAt(machine.getCreatedAt())
                .updatedAt(machine.getUpdatedAt())
                .build();
    }
}
