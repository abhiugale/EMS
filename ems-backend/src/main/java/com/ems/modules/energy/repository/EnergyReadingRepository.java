package com.ems.modules.energy.repository;

import com.ems.modules.energy.entity.EnergyReading;
import com.ems.modules.energy.entity.EnergyReadingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EnergyReadingRepository extends JpaRepository<EnergyReading, EnergyReadingId> {

    @Query(value = "SELECT COALESCE(SUM(er.energy_kwh), 0) " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end",
            nativeQuery = true)
    BigDecimal findTodayTotalKwh(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query(value = "SELECT COALESCE(SUM(er.energy_kwh), 0) * :tariff " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end",
            nativeQuery = true)
    BigDecimal findTodayTotalCost(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("tariff") BigDecimal tariff);

    @Query(value = "SELECT m.department, COALESCE(SUM(er.energy_kwh), 0) AS total_kwh, COALESCE(SUM(er.energy_kwh), 0) * :tariff AS total_cost " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :from AND er.recorded_at <= :to " +
            "GROUP BY m.department",
            nativeQuery = true)
    List<Object[]> findBreakdownByDepartment(
            @Param("factoryId") UUID factoryId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("tariff") BigDecimal tariff);

    @Query(value = "SELECT m.name, COALESCE(SUM(er.energy_kwh), 0) AS total_kwh, COALESCE(SUM(er.energy_kwh), 0) * :tariff AS total_cost " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :from AND er.recorded_at <= :to " +
            "GROUP BY m.name",
            nativeQuery = true)
    List<Object[]> findBreakdownByMachine(
            @Param("factoryId") UUID factoryId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("tariff") BigDecimal tariff);

    @Query(value = "SELECT " +
            "  CASE " +
            "    WHEN EXTRACT(HOUR FROM er.recorded_at AT TIME ZONE :tz) BETWEEN 6 AND 13 THEN 'Shift A (06:00 - 14:00)' " +
            "    WHEN EXTRACT(HOUR FROM er.recorded_at AT TIME ZONE :tz) BETWEEN 14 AND 21 THEN 'Shift B (14:00 - 22:00)' " +
            "    ELSE 'Shift C (22:00 - 06:00)' " +
            "  END AS shift, " +
            "  COALESCE(SUM(er.energy_kwh), 0) AS total_kwh, " +
            "  COALESCE(SUM(er.energy_kwh), 0) * :tariff AS total_cost " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end " +
            "GROUP BY shift",
            nativeQuery = true)
    List<Object[]> findBreakdownByShift(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("tariff") BigDecimal tariff,
            @Param("tz") String timezone);

    @Query(value = "SELECT " +
            "  EXTRACT(HOUR FROM er.recorded_at AT TIME ZONE :tz) AS hour_val, " +
            "  COALESCE(MAX(er.active_kw), 0) AS max_kw, " +
            "  COALESCE(AVG(er.active_kw), 0) AS avg_kw " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end " +
            "GROUP BY hour_val " +
            "ORDER BY hour_val",
            nativeQuery = true)
    List<Object[]> findHourlyLoadCurve(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            @Param("tz") String timezone);

    @Query(value = "SELECT " +
            "  er.recorded_at AS peak_time, " +
            "  COALESCE(SUM(er.active_kw), 0) AS total_kw " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end " +
            "GROUP BY er.recorded_at " +
            "ORDER BY total_kw DESC " +
            "LIMIT 1",
            nativeQuery = true)
    List<Object[]> findPeakDemandToday(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query(value = "SELECT DISTINCT ON (er.machine_id) " +
            "  er.machine_id, er.recorded_at, er.active_kw, er.power_factor, er.energy_kwh, er.voltage_r, er.current_r, er.frequency, er.apparent_kva, er.parts_produced " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId " +
            "ORDER BY er.machine_id, er.recorded_at DESC",
            nativeQuery = true)
    List<Object[]> findLatestReadingPerMachine(@Param("factoryId") UUID factoryId);

    @Query(value = "SELECT er.machine_id, COALESCE(SUM(er.energy_kwh), 0) AS total_kwh, COALESCE(SUM(er.parts_produced), 0) AS total_parts " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end " +
            "GROUP BY er.machine_id",
            nativeQuery = true)
    List<Object[]> findTodayMetricsPerMachine(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query(value = "SELECT COALESCE(AVG(er.power_factor), 1.0) " +
            "FROM energy_readings er " +
            "JOIN machines m ON er.machine_id = m.id " +
            "WHERE m.factory_id = :factoryId AND er.recorded_at >= :start AND er.recorded_at < :end",
            nativeQuery = true)
    BigDecimal findAveragePowerFactor(
            @Param("factoryId") UUID factoryId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
