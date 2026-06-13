package com.ems.modules.energy.service;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.common.util.DateTimeUtil;
import com.ems.modules.alert.repository.AlertRepository;
import com.ems.modules.dashboard.dto.*;
import com.ems.modules.energy.dto.*;
import com.ems.modules.energy.repository.EnergyReadingRepository;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.factory.repository.FactoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EnergyService {

    private final EnergyReadingRepository energyReadingRepository;
    private final FactoryRepository factoryRepository;
    private final AlertRepository alertRepository;

    @Transactional(readOnly = true)
    public SummaryDto getSummary(UUID factoryId, LocalDate date) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        String tz = factory.getTimezone();
        Instant start = DateTimeUtil.getStartOfDay(date, tz);
        Instant end = DateTimeUtil.getEndOfDay(date, tz);

        BigDecimal totalKwh = energyReadingRepository.findTodayTotalKwh(factoryId, start, end);
        BigDecimal totalCost = energyReadingRepository.findTodayTotalCost(factoryId, start, end, factory.getTariffInrPerKwh());
        BigDecimal avgPf = energyReadingRepository.findAveragePowerFactor(factoryId, start, end);

        List<Object[]> hourlyRows = energyReadingRepository.findHourlyLoadCurve(factoryId, start, end, tz);
        List<SummaryDto.HourlyPoint> curve = new ArrayList<>();
        for (Object[] row : hourlyRows) {
            curve.add(SummaryDto.HourlyPoint.builder()
                    .hour(((Number) row[0]).intValue())
                    .maxKw((BigDecimal) row[1])
                    .avgKw((BigDecimal) row[2])
                    .build());
        }

        return SummaryDto.builder()
                .totalKwh(totalKwh)
                .totalCost(totalCost)
                .averagePowerFactor(avgPf)
                .hourlyCurve(curve)
                .build();
    }

    @Transactional(readOnly = true)
    public BreakdownDto getBreakdown(UUID factoryId, String groupBy, Instant from, Instant to) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        List<Object[]> rows;
        if ("department".equalsIgnoreCase(groupBy)) {
            rows = energyReadingRepository.findBreakdownByDepartment(factoryId, from, to, factory.getTariffInrPerKwh());
        } else {
            rows = energyReadingRepository.findBreakdownByMachine(factoryId, from, to, factory.getTariffInrPerKwh());
        }

        BigDecimal grandTotalKwh = BigDecimal.ZERO;
        List<BreakdownDto.BreakdownItem> items = new ArrayList<>();

        for (Object[] row : rows) {
            BigDecimal kwh = (BigDecimal) row[1];
            grandTotalKwh = grandTotalKwh.add(kwh);
            items.add(BreakdownDto.BreakdownItem.builder()
                    .name((String) row[0])
                    .kwh(kwh)
                    .cost((BigDecimal) row[2])
                    .percentage(BigDecimal.ZERO)
                    .build());
        }

        // Calculate percentages
        if (grandTotalKwh.compareTo(BigDecimal.ZERO) > 0) {
            for (BreakdownDto.BreakdownItem item : items) {
                BigDecimal pct = item.getKwh()
                        .divide(grandTotalKwh, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100.0))
                        .setScale(2, RoundingMode.HALF_UP);
                item.setPercentage(pct);
            }
        }

        return BreakdownDto.builder().items(items).build();
    }

    @Transactional(readOnly = true)
    public CeoSummaryDto getCeoSummary(UUID factoryId) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        String tz = factory.getTimezone();
        LocalDate today = LocalDate.now();
        Instant todayStart = DateTimeUtil.getStartOfDay(today, tz);
        Instant todayEnd = DateTimeUtil.getEndOfDay(today, tz);

        Instant yesterdayStart = DateTimeUtil.getStartOfDay(today.minusDays(1), tz);
        Instant yesterdayEnd = DateTimeUtil.getEndOfDay(today.minusDays(1), tz);

        BigDecimal todayKwh = energyReadingRepository.findTodayTotalKwh(factoryId, todayStart, todayEnd);
        BigDecimal todayCost = energyReadingRepository.findTodayTotalCost(factoryId, todayStart, todayEnd, factory.getTariffInrPerKwh());

        BigDecimal yesterdayKwh = energyReadingRepository.findTodayTotalKwh(factoryId, yesterdayStart, yesterdayEnd);

        // Savings percentage compared to yesterday
        BigDecimal savingsPct = BigDecimal.ZERO;
        if (yesterdayKwh.compareTo(BigDecimal.ZERO) > 0) {
            savingsPct = yesterdayKwh.subtract(todayKwh)
                    .divide(yesterdayKwh, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100.0))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Current metrics from latest readings
        List<Object[]> latestReadings = energyReadingRepository.findLatestReadingPerMachine(factoryId);
        BigDecimal currentDemandKw = BigDecimal.ZERO;
        BigDecimal sumPf = BigDecimal.ZERO;
        int count = 0;

        for (Object[] row : latestReadings) {
            BigDecimal kw = (BigDecimal) row[2];
            BigDecimal pf = (BigDecimal) row[3];
            currentDemandKw = currentDemandKw.add(kw);
            if (pf != null) {
                sumPf = sumPf.add(pf);
                count++;
            }
        }

        BigDecimal avgPf = count > 0 ? sumPf.divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP) : BigDecimal.valueOf(1.0);
        long openAlerts = alertRepository.countByFactoryIdAndStatus(factoryId, "OPEN");

        return CeoSummaryDto.builder()
                .totalKwhToday(todayKwh)
                .totalCostTodayInr(todayCost)
                .savingsVsYesterdayPct(savingsPct)
                .currentDemandKw(currentDemandKw)
                .powerFactor(avgPf)
                .activeAlertCount((int) openAlerts)
                .build();
    }

    @Transactional(readOnly = true)
    public PeakDemandDto getPeakDemand(UUID factoryId, LocalDate date) {
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory not found"));

        String tz = factory.getTimezone();
        Instant start = DateTimeUtil.getStartOfDay(date, tz);
        Instant end = DateTimeUtil.getEndOfDay(date, tz);

        List<Object[]> peakRows = energyReadingRepository.findPeakDemandToday(factoryId, start, end);
        Instant peakTime = null;
        BigDecimal peakDemandKw = BigDecimal.ZERO;

        if (!peakRows.isEmpty()) {
            Object[] row = peakRows.get(0);
            peakTime = (Instant) row[0];
            peakDemandKw = (BigDecimal) row[1];
        }

        BigDecimal contractLimit = factory.getContractDemandKw();
        boolean limitBreached = peakDemandKw.compareTo(contractLimit) > 0;

        return PeakDemandDto.builder()
                .peakTime(peakTime)
                .peakDemandKw(peakDemandKw)
                .contractDemandKw(contractLimit)
                .limitBreached(limitBreached)
                .build();
    }

    @Transactional(readOnly = true)
    public RealtimeDto getRealtime(UUID factoryId) {
        List<Object[]> latestReadings = energyReadingRepository.findLatestReadingPerMachine(factoryId);

        BigDecimal activeKw = BigDecimal.ZERO;
        BigDecimal apparentKva = BigDecimal.ZERO;
        BigDecimal sumPf = BigDecimal.ZERO;
        BigDecimal sumFreq = BigDecimal.ZERO;
        BigDecimal sumVolt = BigDecimal.ZERO;
        BigDecimal sumCurr = BigDecimal.ZERO;
        int count = 0;
        Instant latestTimestamp = null;

        for (Object[] row : latestReadings) {
            Instant time = (Instant) row[1];
            if (latestTimestamp == null || (time != null && time.isAfter(latestTimestamp))) {
                latestTimestamp = time;
            }

            activeKw = activeKw.add((BigDecimal) row[2]);
            apparentKva = apparentKva.add(row[8] != null ? (BigDecimal) row[8] : BigDecimal.ZERO);

            if (row[3] != null) {
                sumPf = sumPf.add((BigDecimal) row[3]);
            }
            if (row[7] != null) {
                sumFreq = sumFreq.add((BigDecimal) row[7]);
            }
            if (row[5] != null) {
                sumVolt = sumVolt.add((BigDecimal) row[5]);
            }
            if (row[6] != null) {
                sumCurr = sumCurr.add((BigDecimal) row[6]);
            }
            count++;
        }

        BigDecimal avgPf = count > 0 ? sumPf.divide(BigDecimal.valueOf(count), 3, RoundingMode.HALF_UP) : BigDecimal.valueOf(1.0);
        BigDecimal avgFreq = count > 0 ? sumFreq.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.valueOf(50.0);
        BigDecimal avgVolt = count > 0 ? sumVolt.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.valueOf(415.0);
        BigDecimal avgCurr = count > 0 ? sumCurr.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return RealtimeDto.builder()
                .timestamp(latestTimestamp != null ? latestTimestamp : Instant.now())
                .activeKw(activeKw)
                .apparentKva(apparentKva)
                .powerFactor(avgPf)
                .frequency(avgFreq)
                .voltage(avgVolt)
                .current(avgCurr)
                .build();
    }
}
