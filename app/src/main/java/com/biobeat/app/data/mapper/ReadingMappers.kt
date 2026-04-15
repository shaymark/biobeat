package com.biobeat.app.data.mapper

import com.biobeat.app.data.local.entity.EcgSessionEntity
import com.biobeat.app.data.local.entity.HeartRateEntity
import com.biobeat.app.domain.model.EcgSession
import com.biobeat.app.domain.model.HeartRateRecord

fun HeartRateEntity.toDomain(): HeartRateRecord = HeartRateRecord(
    id = id,
    bpm = bpm,
    sensorContact = sensorContact,
    rrIntervals = rrIntervals.split(",").mapNotNull { it.toIntOrNull() },
    deviceMac = deviceMac,
    timestampMs = timestampMs,
)

fun HeartRateRecord.toEntity(): HeartRateEntity = HeartRateEntity(
    id = id,
    bpm = bpm,
    sensorContact = sensorContact,
    rrIntervals = rrIntervals.joinToString(","),
    deviceMac = deviceMac,
    timestampMs = timestampMs,
)

fun EcgSessionEntity.toDomain(): EcgSession = EcgSession(
    id = id,
    deviceMac = deviceMac,
    startTimeMs = startTimeMs,
    endTimeMs = endTimeMs,
    sampleCount = sampleCount,
    sampleRateHz = sampleRateHz,
)
