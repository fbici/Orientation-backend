package com.orientation.orientationapp.ai.chat.controller;
import com.orientation.orientationapp.ai.chat.model.*;
import com.orientation.orientationapp.ai.chat.service.ChatService;
import com.orientation.orientationapp.ai.history.DecisionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import java.util.UUID;
@RestController @RequestMapping("/ai/chat") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','CANDIDAT')")
public class ChatController {
 private final ChatService chat;
 private final DecisionHistoryService history;
 @PostMapping("/sessions") public ChatSession create(@RequestParam(required=false) UUID candidateId){return chat.createSessionFor(candidateId);}
 @PostMapping("/sessions/{id}/messages") public ChatMessage ask(@PathVariable UUID id,@Valid @RequestBody AskRequest request){return chat.ask(id,request);}
 @GetMapping("/sessions/{id}") public ChatSession get(@PathVariable UUID id){return chat.getSession(id);}
 @GetMapping("/sessions/{id}/messages") public org.springframework.data.domain.Page<DecisionHistoryService.View> messages(@PathVariable UUID id,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){
  chat.getSession(id);return history.children(id,page,size);
 }
 @GetMapping("/sessions") public org.springframework.data.domain.Page<DecisionHistoryService.View> sessions(@RequestParam(required=false)UUID candidateId,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){
  return history.history(candidateId,"CHAT_SESSION",page,size);
 }
}
