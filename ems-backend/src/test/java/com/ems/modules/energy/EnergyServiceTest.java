package com.ems.modules.energy;

import com.ems.modules.alert.repository.AlertRepository;
import com.ems.modules.energy.dto.SummaryDto;
import com.ems.modules.energy.repository.EnergyReadingRepository;
import com.ems.modules.energy.service.EnergyService;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class EnergyServiceTest {

    @Mock
    private EnergyReadingRepository energyReadingRepository;

    @Mock
    private FactoryRepository factoryRepository;

    @Mock
    private AlertRepository alertRepository;

    @InjectMocks
    private EnergyService energyService;

    private UUID factoryId;
    private Factory factory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        factoryId = UUID.randomUUID();
        factory = Factory.builder()
                .id(factoryId)
                .name("Mumbai Plant")
                .timezone("Asia/Kolkata")
                .contractDemandKw(BigDecimal.valueOf(1000))
                .tariffInrPerKwh(BigDecimal.valueOf(8.0))
                .build();
    }

    @Test
    public void testGetSummary() {
        when(factoryRepository.findById(factoryId)).thenReturn(Optional.of(factory));
        when(energyReadingRepository.findTodayTotalKwh(eq(factoryId), any(Instant.class), any(Instant.class)))
                .thenReturn(BigDecimal.valueOf(5000));
        when(energyReadingRepository.findTodayTotalCost(eq(factoryId), any(Instant.class), any(Instant.class), any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(40000));
        when(energyReadingRepository.findAveragePowerFactor(eq(factoryId), any(Instant.class), any(Instant.class)))
                .thenReturn(BigDecimal.valueOf(0.95));
        when(energyReadingRepository.findHourlyLoadCurve(eq(factoryId), any(Instant.class), any(Instant.class), any(String.class)))
                .thenReturn(Collections.emptyList());

        SummaryDto dto = energyService.getSummary(factoryId, LocalDate.now());

        assertNotNull(dto);
        assertEquals(BigDecimal.valueOf(5000), dto.getTotalKwh());
        assertEquals(BigDecimal.valueOf(40000), dto.getTotalCost());
        assertEquals(BigDecimal.valueOf(0.95), dto.getAveragePowerFactor());
    }
}
