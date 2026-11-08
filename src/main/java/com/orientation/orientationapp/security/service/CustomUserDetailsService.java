package com.orientation.orientationapp.security.service;

import com.orientation.orientationapp.modules.auth.entity.User;
import com.orientation.orientationapp.modules.auth.entity.UserRole;
import com.orientation.orientationapp.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = loadUserFromDatabase(username);

        List<SimpleGrantedAuthority> authorities = user.getUserRoles().stream()
                .map(UserRole::getRole)
                .filter(role -> Boolean.TRUE.equals(role.getActive()) && !Boolean.TRUE.equals(role.getDeleted()))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                .collect(Collectors.toList());

        user.getUserRoles().stream().map(UserRole::getRole)
            .filter(role -> Boolean.TRUE.equals(role.getActive()) && !Boolean.TRUE.equals(role.getDeleted()))
            .flatMap(role -> role.getRolePermissions().stream())
            .map(rp -> rp.getPermission())
            .filter(p -> Boolean.TRUE.equals(p.getActive()) && !Boolean.TRUE.equals(p.getDeleted()))
            .map(p -> new SimpleGrantedAuthority(p.getCode())).forEach(authorities::add);
        return new CustomUserDetails(
                user.getId().toString(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                AccountState.active(user),
                authorities
        );
    }

    private User loadUserFromDatabase(String username) {
        // Try email first (most common case)
        if (username.contains("@")) {
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        }

        // Try UUID (JWT subject case)
        try {
            UUID id = UUID.fromString(username);
            return userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
