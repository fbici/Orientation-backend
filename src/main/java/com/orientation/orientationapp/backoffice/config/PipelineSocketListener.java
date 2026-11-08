package com.orientation.orientationapp.backoffice.config;
import com.orientation.orientationapp.intelligence.pipeline.DocumentProcessingPipeline.PipelineProgressEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
@Component @RequiredArgsConstructor
public class PipelineSocketListener {
 private final SimpMessagingTemplate messaging;
 @EventListener public void progress(PipelineProgressEvent event) {
  messaging.convertAndSendToUser(event.userId().toString(),"/queue/pipeline",
   Map.of("documentId",event.documentId(),"step",event.step(),"message",event.message(),"type","PIPELINE_PROGRESS"));
 }
}
