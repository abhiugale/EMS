package com.ems.modules.upload.service;

import com.ems.modules.energy.dto.EnergyReadingDto;

import java.util.List;
import java.util.UUID;

public interface EnergyReadingIngester {
    void ingest(List<EnergyReadingDto> readings, UUID uploadId);
}
