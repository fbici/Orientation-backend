package com.orientation.orientationapp.dataplat_import;

import com.orientation.orientationapp.dataplat_formats.core.model.ImportContext;
import com.orientation.orientationapp.dataplat_formats.core.model.ImportResult;
import com.orientation.orientationapp.dataplat_formats.enums.DataFormat;
import com.orientation.orientationapp.dataplat_formats.enums.DataType;
import com.orientation.orientationapp.dataplat_formats.enums.ImportStatus;
import com.orientation.orientationapp.dataplat_history.entity.ImportHistoryRecord;
import com.orientation.orientationapp.dataplat_history.repository.ImportHistoryRecordRepository;
import com.orientation.orientationapp.dataplat_import.service.impl.EntityMapperRegistry;
import com.orientation.orientationapp.dataplat_import.service.impl.ImportOrchestratorImpl;
import com.orientation.orientationapp.dataplat_parser.factory.impl.ParserFactoryImpl;
import com.orientation.orientationapp.dataplat_parser.strategy.impl.CsvParser;
import com.orientation.orientationapp.dataplat_quality.entity.QualityReportRecord;
import com.orientation.orientationapp.dataplat_quality.repository.QualityReportRepository;
import com.orientation.orientationapp.dataplat_transformation.engine.impl.TransformationEngineImpl;
import com.orientation.orientationapp.dataplat_validation.engine.impl.ValidationEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportOrchestratorTest {

    private ImportOrchestratorImpl importOrchestrator;

    @Mock
    private ImportHistoryRecordRepository importHistoryRepository;

    @Mock
    private QualityReportRepository qualityReportRepository;

    @BeforeEach
    void setUp() {
        CsvParser csvParser = new CsvParser();
        ParserFactoryImpl parserFactory = new ParserFactoryImpl(List.of(csvParser));
        ValidationEngineImpl validationEngine = new ValidationEngineImpl();
        TransformationEngineImpl transformationEngine = new TransformationEngineImpl();
        EntityMapperRegistry entityMapperRegistry = new EntityMapperRegistry(List.of());

        when(importHistoryRepository.save(any(ImportHistoryRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(qualityReportRepository.save(any(QualityReportRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        importOrchestrator = new ImportOrchestratorImpl(
                parserFactory, validationEngine, transformationEngine, event -> {},
                importHistoryRepository, qualityReportRepository, entityMapperRegistry);
    }

    @Test
    void shouldExecuteImport() {
        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        context.setSourceIdentifier("test.csv");

        ImportResult result = importOrchestrator.executeImport(context);

        assertNotNull(result);
        assertNotNull(result.getImportId());
        assertEquals(ImportStatus.FAILED, result.getStatus());
    }

    @Test
    void shouldGetImportStatus() {
        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        context.setSourceIdentifier("test.csv");

        ImportResult result = importOrchestrator.executeImport(context);

        ImportResult retrieved = importOrchestrator.getImportStatus(result.getImportId());
        assertNotNull(retrieved);
        assertEquals(result.getImportId(), retrieved.getImportId());
    }

    @Test
    void shouldCancelImport() {
        ImportContext context = ImportContext.create(null, null, DataType.UNIVERSITIES, DataFormat.CSV);
        context.setSourceIdentifier("test.csv");

        ImportResult result = importOrchestrator.executeImport(context);

        importOrchestrator.cancelImport(result.getImportId());

        ImportResult retrieved = importOrchestrator.getImportStatus(result.getImportId());
        assertEquals(ImportStatus.CANCELLED, retrieved.getStatus());
    }
}
