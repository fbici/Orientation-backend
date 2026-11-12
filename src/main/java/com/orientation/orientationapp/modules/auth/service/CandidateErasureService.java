package com.orientation.orientationapp.modules.auth.service;

import com.orientation.orientationapp.storage.service.StorageService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.*;

/** Explicit SQL includes soft-deleted children, which JPA's global filter hides. */
@Service @RequiredArgsConstructor
public class CandidateErasureService {
    private final JdbcTemplate jdbc;
    private final StorageService storage;

    @Transactional
    public boolean eraseDue(UUID userId) {
        var due=jdbc.queryForList("SELECT user_id FROM candidate_deletion_requests WHERE user_id=? AND purge_after<=now() FOR UPDATE SKIP LOCKED",UUID.class,userId);
        if(due.isEmpty())return false;
        var users=jdbc.queryForList("SELECT email,enabled FROM users WHERE id=? FOR UPDATE",userId);
        if(users.size()!=1 || !Boolean.FALSE.equals(users.getFirst().get("enabled")))throw new IllegalStateException("Account not disabled");
        if(jdbc.queryForObject("SELECT count(*) FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=? AND r.code<>'CANDIDAT'",Integer.class,userId)>0)
            throw new IllegalStateException("Staff account requires review");
        String email=(String)users.getFirst().get("email"), owner=userId.toString();
        var candidates=jdbc.queryForList("SELECT id FROM candidates WHERE user_id=? FOR UPDATE",UUID.class,userId);
        if(candidates.isEmpty())throw new IllegalStateException("Candidate ownership missing");
        var personalIds=new HashSet<String>();personalIds.add(owner);
        var files=new HashSet<String>();
        var documents=jdbc.queryForList("SELECT id FROM doc_documents WHERE uploaded_by=? FOR UPDATE",UUID.class,owner);
        // Never erase shared/public knowledge as a side effect of a personal deletion.
        for(var id:documents) {
            if(jdbc.queryForObject("SELECT count(*) FROM doc_documents d WHERE d.id=? AND (d.status='PROCESSING' OR EXISTS(SELECT 1 FROM knowledge_nodes n WHERE n.source_document_id=d.id) OR EXISTS(SELECT 1 FROM knowledge_search_index n WHERE n.source_document_id=d.id) OR EXISTS(SELECT 1 FROM doc_document_versions v JOIN guide_versions g ON g.document_version_id=v.id WHERE v.document_id=d.id))",Integer.class,id)>0)
                throw new IllegalStateException("Document requires review");
            personalIds.add(id.toString());
            files.addAll(jdbc.queryForList("SELECT stored_file_name FROM doc_documents WHERE id=?",String.class,id));
            files.addAll(jdbc.queryForList("SELECT stored_file_name FROM doc_document_versions WHERE document_id=?",String.class,id));
        }
        for(var id:candidates) {
            personalIds.add(id.toString());
            files.addAll(jdbc.queryForList("SELECT file_url FROM transcripts WHERE candidate_id=? AND file_url IS NOT NULL",String.class,id));
            personalIds.addAll(jdbc.queryForList("SELECT id::text FROM transcripts WHERE candidate_id=?",String.class,id));
        }
        // Validate every path and shared reference before deleting any bytes.
        for(String file:files) {
            storage.load(file);
            if(jdbc.queryForObject("SELECT (SELECT count(*) FROM transcripts t JOIN candidates c ON c.id=t.candidate_id WHERE t.file_url=? AND c.user_id IS DISTINCT FROM ?) + (SELECT count(*) FROM doc_documents WHERE stored_file_name=? AND uploaded_by IS DISTINCT FROM ?) + (SELECT count(*) FROM doc_document_versions v JOIN doc_documents d ON d.id=v.document_id WHERE v.stored_file_name=? AND d.uploaded_by IS DISTINCT FROM ?)",Integer.class,file,userId,file,owner,file,owner)>0)
                throw new IllegalStateException("Shared file requires review");
        }
        // Deletes are idempotent. On a filesystem/SQL failure the address stays reserved
        // and the disabled account is retried; never claim erasure on partial failure.
        for(var id:documents) {
            for(String table:List.of("doc_extracted_facts","doc_document_audits","doc_document_extractions","doc_document_classifications","doc_document_metadata","doc_document_pages","doc_document_versions"))
                jdbc.update("DELETE FROM "+table+" WHERE document_id=?",id);
            jdbc.update("DELETE FROM doc_documents WHERE id=?",id);
        }
        for(var id:candidates) {
            jdbc.update("DELETE FROM decision_records WHERE candidate_id=?",id);
            jdbc.update("DELETE FROM recommendation_explanations WHERE recommendation_id IN (SELECT id FROM recommendations WHERE candidate_id=?)",id);
            jdbc.update("DELETE FROM knowledge_recommendation_feedback WHERE candidate_id=?",id);
            jdbc.update("DELETE FROM knowledge_learning_history WHERE candidate_id=?",id);
            jdbc.update("DELETE FROM recommendations WHERE candidate_id=?",id);
            jdbc.update("DELETE FROM recommendation_runs WHERE candidate_id=?",id);
            jdbc.update("DELETE FROM transcript_analyses WHERE transcript_id IN (SELECT id FROM transcripts WHERE candidate_id=?)",id);
            jdbc.update("DELETE FROM transcript_lines WHERE transcript_id IN (SELECT id FROM transcripts WHERE candidate_id=?)",id);
            jdbc.update("DELETE FROM transcripts WHERE candidate_id=?",id);
            jdbc.update("DELETE FROM candidates WHERE id=?",id);
        }
        for(String id:personalIds) {
            jdbc.update("DELETE FROM activity_logs WHERE entity_id=?",id);
            jdbc.update("DELETE FROM audit_events WHERE entity_id=?",id);
        }
        jdbc.update("DELETE FROM audit_events WHERE user_id=? OR lower(username)=lower(?)",owner,email);
        jdbc.update("DELETE FROM doc_document_audits WHERE user_id=?",owner);
        jdbc.update("DELETE FROM bo_notifications WHERE user_id=?",owner);
        jdbc.update("DELETE FROM invitations WHERE invited_by=? OR lower(email)=lower(?)",userId,email);
        jdbc.update("UPDATE teams SET leader_id=NULL WHERE leader_id=?",userId);
        for(String table:List.of("decision_records","activity_logs","team_members","user_profiles","email_verification_challenges","account_tokens","auth_external_identities","api_keys","refresh_tokens","user_roles","candidate_deletion_requests")) {
            String column=table.equals("decision_records")?"actor_id":"user_id";
            jdbc.update("DELETE FROM "+table+" WHERE "+column+"=?",userId);
        }
        jdbc.update("DELETE FROM users WHERE id=?",userId);
        for(String file:files)storage.delete(file);
        return true;
    }
}
