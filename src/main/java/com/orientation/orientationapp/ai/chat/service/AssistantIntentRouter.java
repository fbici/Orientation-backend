package com.orientation.orientationapp.ai.chat.service;
import org.springframework.stereotype.Component;
import java.text.Normalizer;
import java.util.Locale;
@Component
public class AssistantIntentRouter {
 public enum Intent { RECOMMENDATIONS, EXPLANATION, SUBJECTS, SCHOLARSHIPS, COMPARISON, SIMULATION, SEARCH, UNSUPPORTED }
 public Intent detect(String input) {
  String s=normalize(input);
  if(s.matches("(?s).*(ignore.{0,30}instruction|system.{0,10}prompt|revel.{0,20}(token|secret)|mot de passe|autre candidat|execute.{0,15}(sql|commande)).*"))
   return Intent.UNSUPPORTED;
  if(s.contains("simul")||s.contains("si ")&&s.contains("moyenne"))return Intent.SIMULATION;
  if(s.contains("compar")||s.contains("difference"))return Intent.COMPARISON;
  if(s.contains("bourse")||s.contains("financement"))return Intent.SCHOLARSHIPS;
  if(s.contains("matiere")||s.contains("amelior")||s.contains("mathematique"))return Intent.SUBJECTS;
  if(s.contains("pourquoi")||s.contains("non eligible")||s.contains("explication"))return Intent.EXPLANATION;
  if(s.contains("formation")||s.contains("recommand")||s.contains("mon profil")||s.contains("orientation"))return Intent.RECOMMENDATIONS;
  if(s.contains("guide")||s.contains("universit")||s.contains("recherch")||s.contains("programme"))return Intent.SEARCH;
  return Intent.UNSUPPORTED;
 }
 public String normalize(String text) {
  return Normalizer.normalize(text==null?"":text,Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
 }
}
