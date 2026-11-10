package com.orientation.orientationapp.modules.auth.service;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.orientation.orientationapp.exception.BusinessException;
import org.springframework.stereotype.Service;
@Service
public class InternationalPhoneService {
    private final PhoneNumberUtil numbers=PhoneNumberUtil.getInstance();
    public String normalize(String raw) {
        if(raw==null || raw.isBlank()) return null;
        String value=raw.strip();
        if(value.length()>100 || !value.startsWith("+") || !value.matches("[+0-9 ().-]+")) throw invalid();
        try {
            var number=numbers.parse(value,null);
            if(number.hasExtension() || !numbers.isValidNumber(number)) throw invalid();
            return numbers.format(number,PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch(NumberParseException ex) { throw invalid(); }
    }
    private BusinessException invalid() {
        return new BusinessException("Numéro de téléphone invalide. Utilisez le format international avec indicatif (+...).","INVALID_PHONE");
    }
}
