package com.syntheaweb.backend.dto;

import com.syntheaweb.backend.database.entity.SummaryStatus;

import java.time.LocalDateTime;

public record AiPatientSummaryDto(
                Long id,
                SummaryStatus status,
                String modelName,
                String summaryText,
                LocalDateTime createdAt) {
}
