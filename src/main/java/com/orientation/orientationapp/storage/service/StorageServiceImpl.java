package com.orientation.orientationapp.storage.service;
import com.orientation.orientationapp.storage.config.StorageConfig;
import com.orientation.orientationapp.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.support.*;
import jakarta.annotation.PostConstruct;
import java.nio.file.*;
import java.io.*;
import java.util.*;

@Service @RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {
    private final StorageConfig storageConfig;
    private Path uploadLocation;
    @PostConstruct public void init() {
        try {
            Path configured=Path.of(storageConfig.getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(configured);
            if (Files.isSymbolicLink(configured)) throw new IOException("Invalid storage root");
            uploadLocation=configured.toRealPath();
        } catch (IOException|RuntimeException ex) { throw failure("Storage is unavailable"); }
    }
    private BusinessException failure(String message) { return new BusinessException(message,"STORAGE_ERROR"); }
    @Override public void validate(MultipartFile file) {
        if (file==null || file.isEmpty() || file.getSize()>storageConfig.getMaxSize()) throw failure("Invalid file size");
        String type=file.getContentType();
        if (type==null || Arrays.stream(storageConfig.getAllowedTypes().split(",")).noneMatch(type::equals))
            throw failure("Unsupported file type");
        try (InputStream in=file.getInputStream()) {
            byte[] header=in.readNBytes(8);
            boolean valid=switch(type) {
                case "application/pdf" -> header.length>=5 && new String(header,0,5,java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF-");
                case "image/png" -> Arrays.equals(header,new byte[]{(byte)137,80,78,71,13,10,26,10});
                case "image/jpeg" -> header.length>=3 && (header[0]&255)==255 && (header[1]&255)==216 && (header[2]&255)==255;
                default -> false;
            };
            if (!valid) throw failure("File signature does not match its declared type");
        } catch (IOException ex) { throw failure("Cannot read file"); }
    }
    @Override public String store(MultipartFile file) {
        validate(file);
        String extension=switch(file.getContentType()) {
            case "application/pdf" -> ".pdf";case "image/png" -> ".png";case "image/jpeg" -> ".jpg";
            default -> throw failure("Unsupported file type");
        };
        String filename=UUID.randomUUID()+extension;Path path=load(filename);
        OutputStream output;
        try { output=Files.newOutputStream(path,StandardOpenOption.CREATE_NEW); }
        catch(IOException ex) { throw failure("Cannot create stored file"); }
        try (OutputStream out=output; InputStream in=file.getInputStream()) {
            byte[] buffer=new byte[8192];long size=0;int count;
            while((count=in.read(buffer))!=-1) {
                size+=count;if(size>storageConfig.getMaxSize()) throw failure("Invalid file size");
                out.write(buffer,0,count);
            }
        } catch(IOException|RuntimeException ex) {
            try { Files.deleteIfExists(path); } catch(IOException cleanup) { ex.addSuppressed(cleanup); }
            throw failure("Cannot store file");
        }
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCompletion(int status) {
                    if(status!=STATUS_COMMITTED) delete(filename);
                }
            });
        return filename;
    }
    @Override public Path load(String filename) {
        if (filename==null || !filename.matches("[0-9a-fA-F-]{36}\\.(pdf|png|jpg)")) throw failure("Invalid file reference");
        Path path=uploadLocation.resolve(filename).normalize();
        if (!path.startsWith(uploadLocation) || Files.isSymbolicLink(path)) throw failure("Invalid file reference");
        return path;
    }
    @Override public void delete(String filename) {
        try { Files.deleteIfExists(load(filename)); } catch(IOException ex) { throw failure("Cannot remove stored file"); }
    }
    @Override public boolean exists(String filename) { return Files.isRegularFile(load(filename),LinkOption.NOFOLLOW_LINKS); }
}
