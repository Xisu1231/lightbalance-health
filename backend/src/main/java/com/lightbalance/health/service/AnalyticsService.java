package com.lightbalance.health.service;

import com.lightbalance.health.dto.AppDtos;
import com.lightbalance.health.dto.SeedData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final ResourceDataLoader resourceDataLoader;

    public AnalyticsService(ResourceDataLoader resourceDataLoader) {
        this.resourceDataLoader = resourceDataLoader;
    }

    public AppDtos.AnalyticsResponse getAnalytics() {
        SeedData.ModelReport report = resourceDataLoader.getModelReport();
        SeedData.ModelMetricSeed selectedModel = report.models()
            .stream()
            .filter(model -> model.model().equals(report.selectedModel()))
            .findFirst()
            .orElse(report.models().get(0));

        return new AppDtos.AnalyticsResponse(
            new AppDtos.DatasetSummary(
                report.dataset().sample_count(),
                report.dataset().high_risk_count(),
                report.dataset().avg_sleep_hours(),
                report.dataset().avg_steps(),
                report.dataset().avg_bmi(),
                report.dataset().avg_stress_score()
            ),
            report.models().stream()
                .map(model -> new AppDtos.ModelMetric(
                    model.model(),
                    model.accuracy(),
                    model.precision(),
                    model.recall(),
                    model.f1()
                ))
                .toList(),
            report.selectedModel(),
            report.bmiBands().stream()
                .map(band -> new AppDtos.BmiBand(band.label(), band.highRiskRate(), band.count()))
                .toList(),
            resourceDataLoader.getDatasetRows().stream()
                .limit(90)
                .map(row -> new AppDtos.ScatterPoint(
                    row.id(),
                    row.sleepHours(),
                    row.stressScore(),
                    row.bmi(),
                    row.steps(),
                    row.waterMl(),
                    row.riskLabel()
                ))
                .toList(),
            List.of(
                new AppDtos.ConfusionCell("高风险判对", selectedModel.confusionMatrix().tp()),
                new AppDtos.ConfusionCell("低风险误判高", selectedModel.confusionMatrix().fp()),
                new AppDtos.ConfusionCell("低风险判对", selectedModel.confusionMatrix().tn()),
                new AppDtos.ConfusionCell("高风险漏判", selectedModel.confusionMatrix().fn())
            ),
            report.narrative()
        );
    }
}
