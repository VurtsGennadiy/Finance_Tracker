package ru.vgd.tracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRepository;

/**
 * Реализация {@link UserDetailsService} на основе JPA {@link UserRepository}.
 */
@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.loadUserWithRolesByUsername(username)
                .orElseGet(() -> userRepository.loadUserWithRolesByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));

        return new SecurityUser(user);
    }
}
