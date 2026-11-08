package com.orientation.orientationapp.security;
import com.orientation.orientationapp.storage.service.StorageServiceImpl;
import com.orientation.orientationapp.storage.config.StorageConfig;
import com.orientation.orientationapp.exception.BusinessException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class StorageSecurityTest {
    @TempDir Path directory;
    private StorageServiceImpl storage;
    @BeforeEach void setup() {
        var config=new StorageConfig();config.setUploadDir(directory.toString());
        config.setAllowedTypes("application/pdf,image/png,image/jpeg");config.setMaxSize(1024);
        storage=new StorageServiceImpl(config);storage.init();
    }
    @Test void ignoresOriginalFilenameAndContainsInternalPaths() {
        var file=new MockMultipartFile("file","../../outside.pdf","application/pdf","%PDF-1.4\n".getBytes());
        String stored=storage.store(file);
        assertFalse(stored.contains("outside"));assertEquals(directory,storage.load(stored).getParent());
        assertTrue(storage.exists(stored));
    }
    @Test void rejectsTraversalAndAbsoluteFileReferences() {
        assertThrows(BusinessException.class,()->storage.load("../outside.pdf"));
        assertThrows(BusinessException.class,()->storage.load(directory.resolve("outside.pdf").toString()));
    }
    @Test void rejectsForgedMimeAndOversizeFiles() {
        assertThrows(BusinessException.class,()->storage.store(new MockMultipartFile("file","a.pdf","application/pdf","not-pdf".getBytes())));
        assertThrows(BusinessException.class,()->storage.store(new MockMultipartFile("file","a.pdf","application/pdf",new byte[1025])));
        assertThrows(BusinessException.class,()->storage.store(new MockMultipartFile("file","a.exe","application/x-msdownload",new byte[]{1})));
    }
    @Test void removesOnlyNewFilesWhenTransactionRollsBack() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            String stored=storage.store(new MockMultipartFile("file","a.pdf","application/pdf","%PDF-1.4\n".getBytes()));
            assertTrue(storage.exists(stored));
            TransactionSynchronizationManager.getSynchronizations().forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));
            assertFalse(storage.exists(stored));
        } finally { TransactionSynchronizationManager.clearSynchronization(); }
    }
}
