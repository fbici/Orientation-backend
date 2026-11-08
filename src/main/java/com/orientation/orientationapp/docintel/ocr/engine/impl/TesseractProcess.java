package com.orientation.orientationapp.docintel.ocr.engine.impl;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
@Component
public class TesseractProcess {
 private final String command,languages,dataDirectory;
 private final int timeout;
 public TesseractProcess(@Value("${app.ocr.tesseract-command:tesseract}") String command,
  @Value("${app.ocr.languages:fra+eng}") String languages,
  @Value("${app.ocr.tessdata-dir:}") String dataDirectory,
  @Value("${app.ocr.timeout-seconds:45}") int timeout) {
  this.command=command;this.languages=languages;this.dataDirectory=dataDirectory;this.timeout=Math.max(1,Math.min(timeout,120));
  if(!languages.matches("[a-zA-Z_]+(\\+[a-zA-Z_]+)*"))throw new IllegalArgumentException("Invalid OCR language configuration");
 }
 public record Recognition(String text,double confidence) {}
 public Recognition recognize(BufferedImage image) {
  Path folder=null;
  try {
   folder=Files.createTempDirectory("orientia-ocr-");
   Path input=folder.resolve("page.png"),output=folder.resolve("result");
   if(!ImageIO.write(image,"png",input.toFile()))throw new java.io.IOException();
   var arguments=new ArrayList<>(List.of(command,input.toString(),output.toString(),"-l",languages,"--oem","1","--psm","3"));
   if(!dataDirectory.isBlank()){arguments.add("--tessdata-dir");arguments.add(dataDirectory);}
   arguments.add("txt");arguments.add("tsv");
   Process process=new ProcessBuilder(arguments).redirectErrorStream(true).redirectOutput(folder.resolve("diagnostics.log").toFile()).start();
   try {
    if(!process.waitFor(timeout,TimeUnit.SECONDS)) {
     process.destroyForcibly();process.waitFor(5,TimeUnit.SECONDS);
     throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT,"Local OCR exceeded its time limit");
    }
   } catch(InterruptedException ex) {process.destroyForcibly();Thread.currentThread().interrupt();throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"OCR interrupted");}
   if(process.exitValue()!=0)throw unavailable();
   Path text=folder.resolve("result.txt"),tsv=folder.resolve("result.tsv");
   if(!Files.exists(text) || !Files.exists(tsv) || Files.size(text)>20_000_000 || Files.size(tsv)>20_000_000)throw unavailable();
   String raw=Files.readString(text,StandardCharsets.UTF_8).replace('\f','\n');
   double sum=0;int count=0;
   for(String line:Files.readAllLines(tsv,StandardCharsets.UTF_8)) {
    var cells=line.split("\\t",12);
    if(cells.length<12 || cells[11].isBlank())continue;
    try { double c=Double.parseDouble(cells[10]);if(Double.isFinite(c) && c>=0 && c<=100){sum+=c;count++;} }
    catch(NumberFormatException ignored) {}
   }
   return new Recognition(raw,count==0?0:sum/count/100.0);
  } catch(java.io.IOException ex) {throw unavailable();}
  finally {
   if(folder!=null && folder.toAbsolutePath().normalize().startsWith(Path.of(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize())) {
    try(var files=Files.walk(folder)) {
     for(Path p:files.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);
    } catch(java.io.IOException ignored) { /* Only this invocation's private scratch directory is eligible for cleanup. */ }
   }
  }
 }
 private ResponseStatusException unavailable() {
  return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Local OCR unavailable: configure Tesseract executable and language data");
 }
}
