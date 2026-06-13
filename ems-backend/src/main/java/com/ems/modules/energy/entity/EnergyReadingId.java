package com.ems.modules.energy.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class EnergyReadingId implements Serializable {
    private Long id;
    private Instant recordedAt;

    public EnergyReadingId() {}

    public EnergyReadingId(Long id, Instant recordedAt) {
        this.id = id;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnergyReadingId that = (EnergyReadingId) o;
        return Objects.equals(id, that.id) && Objects.equals(recordedAt, that.recordedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, recordedAt);
    }
}
