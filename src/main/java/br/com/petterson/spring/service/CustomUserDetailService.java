package br.com.petterson.spring.service;

import br.com.petterson.spring.model.UserEstudos;
import br.com.petterson.spring.repository.EstudosRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailService implements UserDetailsService {

    private final EstudosRepository estudosRepository;

    @Autowired
    public CustomUserDetailService(
        EstudosRepository estudosRepository) {
        this.estudosRepository = estudosRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEstudos user = Optional.ofNullable(estudosRepository.findByUsername(username))
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<GrantedAuthority> authorityListAdmin = AuthorityUtils
            .createAuthorityList("ROLE_USER", "ROLE_ADMIN");
        List<GrantedAuthority> authorityListUser = AuthorityUtils.createAuthorityList("ROLE_USER");

        return new User(user.getUsername(), user.getPassword(),
            user.isAdmin() ? authorityListAdmin : authorityListUser);
    }
}
