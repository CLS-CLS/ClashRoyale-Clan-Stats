package org.lytsiware.clash;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("#{environment.secret}")
    private String secret;

    @Value("#{environment.user}")
    private String user;

    @Bean
    @Primary
    @Profile("unsecured")
    PasswordEncoder noOpPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    @Profile("unsecured")
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(noOpPasswordEncoder());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            if (!user.equals(username)) {
                throw new UsernameNotFoundException(username);
            } else {
                return new User(username, secret, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
            }
        };
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                        "/warstats/input",
                        "/rest/warstats/recalculate",
                        "/rest/warstats/inputdata/**",
                        "/admin/**",
                        "/rest/newPlayers/update/**",
                        "/rest/scheduler/**",
                        "/rest/upload/**")
                .authenticated()
                .and().httpBasic()
                .and().cors().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    }
}