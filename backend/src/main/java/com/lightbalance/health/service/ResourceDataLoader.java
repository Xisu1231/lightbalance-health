package com.lightbalance.health.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lightbalance.health.dto.SeedData;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ResourceDataLoader {

    private final ObjectMapper objectMapper;

    private SeedData.AppSeed appSeed;
    private SeedData.ModelReport modelReport;
    private SeedData.PreprocessingAuditSeed preprocessingAudit;
    private List<SeedData.DatasetRow> datasetRows = List.of();

    public ResourceDataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        this.appSeed = objectMapper.readValue(
            new ClassPathResource("seed/app_seed.json").getInputStream(),
            SeedData.AppSeed.class
        );
        this.modelReport = objectMapper.readValue(
            new ClassPathResource("seed/model_report.json").getInputStream(),
            SeedData.ModelReport.class
        );
        this.preprocessingAudit = objectMapper.readValue(
            new ClassPathResource("seed/preprocessing_audit.json").getInputStream(),
            SeedData.PreprocessingAuditSeed.class
        );
        this.datasetRows = loadDatasetRows();
    }

    public SeedData.AppSeed getAppSeed() {
        return appSeed;
    }

    public SeedData.ModelReport getModelReport() {
        return modelReport;
    }

    public SeedData.PreprocessingAuditSeed getPreprocessingAudit() {
        return preprocessingAudit;
    }

    public List<SeedData.DatasetRow> getDatasetRows() {
        return datasetRows;
    }

    private List<SeedData.DatasetRow> loadDatasetRows() throws IOException {
        List<SeedData.DatasetRow> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            new ClassPathResource("seed/lifestyle_risk_dataset.csv").getInputStream(),
            StandardCharsets.UTF_8
        ))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                rows.add(new SeedData.DatasetRow(
                    parts[0],
                    Double.parseDouble(parts[5]),
                    Double.parseDouble(parts[6]),
                    Integer.parseInt(parts[12]),
                    Integer.parseInt(parts[7]),
                    Integer.parseInt(parts[9]),
                    parts[18]
                ));
            }
        }
        return rows;
    }
}
