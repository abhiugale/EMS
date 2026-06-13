package com.ems.modules.upload.service;

import com.ems.modules.energy.dto.EnergyReadingDto;
import com.ems.modules.factory.entity.Factory;
import com.ems.modules.machine.entity.Machine;
import com.ems.modules.machine.repository.MachineRepository;
import com.ems.modules.upload.entity.Upload;
import com.ems.modules.upload.repository.UploadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelIngesterImpl implements EnergyReadingIngester {

    private final JdbcTemplate jdbcTemplate;
    private final MachineRepository machineRepository;
    private final UploadRepository uploadRepository;

    @Override
    @Transactional
    public void ingest(List<EnergyReadingDto> readings, UUID uploadId) {
        if (readings.isEmpty()) return;

        Upload upload = uploadRepository.findByIdWithFactory(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload record not found: " + uploadId));


        UUID factoryId = upload.getFactory().getId();

        // Get all machines of this factory to map name -> id
        List<Machine> machines = machineRepository.findByFactoryId(factoryId);
        Map<String, UUID> machineMap = new java.util.HashMap<>(machines.stream()
                .collect(Collectors.toMap(
                        m -> m.getName().toLowerCase().trim(),
                        Machine::getId,
                        (existing, replacement) -> existing
                )));

        Factory factory = upload.getFactory();

        // Pre-create any missing machines
        for (EnergyReadingDto dto : readings) {
            String nameKey = dto.getMachineName().toLowerCase().trim();
            if (!machineMap.containsKey(nameKey)) {
                Machine newMachine = Machine.builder()
                        .factory(factory)
                        .name(dto.getMachineName().trim())
                        .baselineKwh(java.math.BigDecimal.ZERO)
                        .department("Imported")
                        .machineType("Imported")
                        .isActive(true)
                        .build();
                newMachine = machineRepository.save(newMachine);
                machineMap.put(nameKey, newMachine.getId());
            }
        }

        String sql = "INSERT INTO energy_readings (" +
                "machine_id, recorded_at, energy_kwh, active_kw, apparent_kva, reactive_kvar, " +
                "power_factor, frequency, voltage_r, voltage_y, voltage_b, " +
                "current_r, current_y, current_b, parts_produced, upload_id, source" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int batchSize = 500;
        int size = readings.size();

        for (int i = 0; i < size; i += batchSize) {
            final List<EnergyReadingDto> subList = readings.subList(i, Math.min(i + batchSize, size));

            jdbcTemplate.batchUpdate(sql, subList, subList.size(), (PreparedStatement ps, EnergyReadingDto dto) -> {
                UUID machineId = machineMap.get(dto.getMachineName().toLowerCase().trim());
                if (machineId == null) {
                    // Set machine_id as null, though database references will fail if NOT NULL is enforced.
                    // We check null machines in service/parser to avoid database failures, but set it anyway
                    ps.setNull(1, Types.OTHER);
                } else {
                    ps.setObject(1, machineId);
                }

                ps.setTimestamp(2, Timestamp.from(dto.getRecordedAt()));
                setBigDecimalOrNull(ps, 3, dto.getEnergyKwh());
                setBigDecimalOrNull(ps, 4, dto.getActiveKw());
                setBigDecimalOrNull(ps, 5, dto.getApparentKva());
                setBigDecimalOrNull(ps, 6, dto.getReactiveKvar());
                setBigDecimalOrNull(ps, 7, dto.getPowerFactor());
                setBigDecimalOrNull(ps, 8, dto.getFrequency());
                setBigDecimalOrNull(ps, 9, dto.getVoltageR());
                setBigDecimalOrNull(ps, 10, dto.getVoltageY());
                setBigDecimalOrNull(ps, 11, dto.getVoltageB());
                setBigDecimalOrNull(ps, 12, dto.getCurrentR());
                setBigDecimalOrNull(ps, 13, dto.getCurrentY());
                setBigDecimalOrNull(ps, 14, dto.getCurrentB());

                if (dto.getPartsProduced() != null) {
                    ps.setInt(15, dto.getPartsProduced());
                } else {
                    ps.setNull(15, Types.INTEGER);
                }

                ps.setObject(16, uploadId);
                ps.setString(17, dto.getSource());
            });
        }
    }

    private void setBigDecimalOrNull(PreparedStatement ps, int paramIndex, java.math.BigDecimal val) throws SQLException {
        if (val != null) {
            ps.setBigDecimal(paramIndex, val);
        } else {
            ps.setNull(paramIndex, Types.NUMERIC);
        }
    }
}
