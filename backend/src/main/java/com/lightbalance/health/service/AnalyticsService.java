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
        SeedData.PreprocessingAuditSeed audit = resourceDataLoader.getPreprocessingAudit();
        SeedData.ModelMetricSeed selectedModel = report.models()
            .stream()
            .filter(model -> model.model().equals(report.selectedModel()))
            .findFirst()
            .orElse(report.models().get(0));

        return new AppDtos.AnalyticsResponse(
            new AppDtos.ProjectSummary(
                report.project().title(),
                report.project().subtitle(),
                report.project().taskType()
            ),
            new AppDtos.DatasetSummary(
                report.dataset().sample_count(),
                report.dataset().high_risk_count(),
                report.dataset().high_risk_ratio(),
                report.dataset().avg_sleep_hours(),
                report.dataset().avg_steps(),
                report.dataset().avg_bmi(),
                report.dataset().avg_stress_score(),
                report.dataset().feature_count(),
                report.dataset().train_count(),
                report.dataset().test_count(),
                report.dataset().sources().stream()
                    .map(source -> new AppDtos.DataSourceCard(
                        source.name(),
                        source.type(),
                        source.location(),
                        source.sampleCount(),
                        source.note()
                    ))
                    .toList()
            ),
            new AppDtos.PreprocessingSummary(
                report.preprocessing().steps(),
                report.preprocessing().missingSummary().stream()
                    .map(item -> new AppDtos.MissingValueCard(
                        item.dataset(),
                        item.rowsBefore(),
                        item.rowsAfter(),
                        item.missingValuesFound(),
                        item.missingRowsRemoved()
                    ))
                    .toList()
            ),
            new AppDtos.PreprocessingAudit(
                new AppDtos.ToolingInfo(
                    audit.tooling().scriptPath(),
                    audit.tooling().runtime(),
                    audit.tooling().library(),
                    audit.tooling().libraryVersion()
                ),
                mapDatasetAudit(audit.lifestyle()),
                mapDatasetAudit(audit.benchmark())
            ),
            report.models().stream()
                .map(model -> new AppDtos.ModelMetric(
                    model.model(),
                    model.accuracy(),
                    model.precision(),
                    model.recall(),
                    model.f1(),
                    model.auc()
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
            report.featureImportance().stream()
                .map(item -> new AppDtos.FeatureImpact(
                    item.key(),
                    item.label(),
                    item.weight(),
                    item.signedWeight(),
                    item.direction(),
                    item.interpretation()
                ))
                .toList(),
            report.knnSweep().stream()
                .map(item -> new AppDtos.KnnSweepPoint(
                    item.k(),
                    item.accuracy(),
                    item.f1()
                ))
                .toList(),
            new AppDtos.BenchmarkSummary(
                report.benchmark().name(),
                report.benchmark().source(),
                report.benchmark().citation(),
                report.benchmark().rawPath(),
                report.benchmark().cleanedPath(),
                report.benchmark().sample_count(),
                report.benchmark().usable_sample_count(),
                report.benchmark().feature_count(),
                report.benchmark().positive_rate(),
                report.benchmark().preprocessing(),
                new AppDtos.MissingValueCard(
                    report.benchmark().missingSummary().dataset(),
                    report.benchmark().missingSummary().rowsBefore(),
                    report.benchmark().missingSummary().rowsAfter(),
                    report.benchmark().missingSummary().missingValuesFound(),
                    report.benchmark().missingSummary().missingRowsRemoved()
                ),
                report.benchmark().models().stream()
                    .map(model -> new AppDtos.ModelMetric(
                        model.model(),
                        model.accuracy(),
                        model.precision(),
                        model.recall(),
                        model.f1(),
                        model.auc()
                    ))
                    .toList(),
                report.benchmark().selectedModel(),
                report.benchmark().notes(),
                report.benchmark().featureLabels()
            ),
            report.narrative(),
            report.reportBullets()
        );
    }

    private AppDtos.DatasetAudit mapDatasetAudit(SeedData.DatasetAuditSeed source) {
        return new AppDtos.DatasetAudit(
            source.dataset(),
            source.path(),
            source.rows(),
            source.columns(),
            source.labelColumn(),
            source.labelDistribution(),
            source.missingValuesFound(),
            source.missingRowsRemoved(),
            source.duplicateRowsFound(),
            source.duplicateRowsRemoved(),
            source.invalidRowsFound(),
            source.invalidRowsRemoved(),
            source.fieldDictionary().stream()
                .map(item -> new AppDtos.FieldDictionaryItem(
                    item.key(),
                    item.label(),
                    item.dtype(),
                    item.unit(),
                    item.meaning()
                ))
                .toList(),
            source.rangeChecks().stream()
                .map(item -> new AppDtos.RangeCheckItem(
                    item.field(),
                    item.validRange(),
                    item.invalidCount(),
                    item.action()
                ))
                .toList(),
            source.outlierChecks().stream()
                .map(item -> new AppDtos.OutlierCheckItem(
                    item.field(),
                    item.outlierCount(),
                    item.method(),
                    item.action()
                ))
                .toList(),
            source.actions(),
            source.notes()
        );
    }
}
