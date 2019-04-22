package org.lytsiware.clash.security;

import org.lytsiware.clash.domain.user.UserEntity;
import org.lytsiware.clash.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CcsUserDetailService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Username %s not found", username)));

        return new User(user.getUsername(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority(user.getRole())));
    }

    public static void main(String[] args) {
        System.out.println(new BCryptPasswordEncoder(11).encode("sampletest"));

    }
}
