package com.orientation.orientationapp.modules.auth.service;
import com.orientation.orientationapp.modules.auth.entity.*;
import com.orientation.orientationapp.modules.auth.repository.*;
import com.orientation.orientationapp.modules.auth.dto.response.LoginResponse;
import com.orientation.orientationapp.modules.auth.service.impl.AuthenticationServiceImpl;
import com.orientation.orientationapp.modules.user.entity.Candidate;
import com.orientation.orientationapp.modules.user.repository.CandidateRepository;
import com.orientation.orientationapp.common.enums.CandidateStatus;
import com.orientation.orientationapp.security.service.AccountState;
import com.orientation.orientationapp.security.jwt.JwtTokenProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import java.util.*;
@Service @RequiredArgsConstructor
public class GoogleLoginService {
    private static final String ISSUER="https://accounts.google.com";
    private final GoogleIdentityVerifier verifier;
    private final ExternalIdentityRepository identities;
    private final UserRepository users;
    private final TenantRepository tenants;
    private final RoleRepository roles;
    private final CandidateRepository candidates;
    private final PasswordEncoder encoder;
    private final RefreshTokenService sessions;
    private final JwtTokenProvider jwt;
    @Value("${app.registration.tenant-code:DEFAULT}") private String publicTenant;
    @Transactional
    public LoginResponse login(String idToken,String ip,String agent) {
        var identity=verifier.verify(idToken);
        var linked=identities.findByIssuerAndSubject(ISSUER,identity.subject());
        User user;
        if(linked.isPresent()) {
            user=users.lockById(linked.get().getUser().getId()).orElseThrow(()->new BadCredentialsException("Invalid account"));
        } else {
            // Existing email is never sufficient to link a Google account.
            if(users.countAllByEmail(identity.email())>0)
                throw new ResponseStatusException(HttpStatus.CONFLICT,"GOOGLE_ACCOUNT_EXISTS");
            var tenant=tenants.findByCode(publicTenant).orElseThrow(()->new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"GOOGLE_SIGNIN_UNAVAILABLE"));
            var role=roles.findByCode("CANDIDAT").filter(r->Boolean.TRUE.equals(r.getActive())&&!Boolean.TRUE.equals(r.getDeleted()))
                .orElseThrow(()->new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"GOOGLE_SIGNIN_UNAVAILABLE"));
            user=new User();user.setEmail(identity.email());user.setFirstName(identity.firstName());user.setLastName(identity.lastName());
            // No usable password is disclosed or derived from provider data.
            user.setPassword(encoder.encode(UUID.randomUUID().toString()+UUID.randomUUID().toString().substring(0,20)));
            user.setTenant(tenant);user.setEmailVerified(true);user.setStatus(CandidateStatus.ACTIVE);
            if(!AccountState.active(user))throw new BadCredentialsException("Account unavailable");
            var membership=new UserRole();membership.setUser(user);membership.setRole(role);user.getUserRoles().add(membership);
            users.saveAndFlush(user);
            var candidate=new Candidate();candidate.setUser(user);candidate.setTenant(tenant);candidate.setEmail(user.getEmail());
            candidate.setFirstName(user.getFirstName());candidate.setLastName(user.getLastName());candidates.saveAndFlush(candidate);
            var link=new ExternalIdentity();link.setIssuer(ISSUER);link.setSubject(identity.subject());link.setUser(user);identities.saveAndFlush(link);
        }
        if(!AccountState.active(user))throw new BadCredentialsException("Account unavailable");
        var session=sessions.createRefreshToken(user,ip,agent);
        return LoginResponse.builder().accessToken(jwt.generateAccessToken(user.getId().toString(),null,session.getFamilyId()))
            .refreshToken(session.getPlainToken()).expiresIn(jwt.getJwtExpiration()/1000).tokenType("Bearer")
            .user(AuthenticationServiceImpl.userInfo(user)).build();
    }
}
