package com.version.gymModuloControl.auth.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.version.gymModuloControl.auth.security.UserDetailsServiceImpl;
import com.version.gymModuloControl.auth.security.jwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/register").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/persona/registrar-cliente").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/persona/registrar-empleado").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/persona/listar-clientes").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/persona/listar-empleados").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/persona/empleados").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/plan/guardar").hasRole("ADMIN")
                        .requestMatchers("/api/plan/listar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/horario-empleado/agregar/**").hasRole("ADMIN")
                        .requestMatchers("/api/horario-empleado/listar").hasAnyRole("ADMIN", "RECEPCIONISTA", "ENTRENADOR")
                        .requestMatchers("/api/categoria/guardar").hasRole("ADMIN")
                        .requestMatchers("/api/categoria/actualizar").hasRole("ADMIN")
                        .requestMatchers("/api/categoria/*/estado").hasRole("ADMIN")
                        .requestMatchers("/api/categoria/listar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/producto/guardar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/producto/actualizar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/producto/*/estado").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/producto/listar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        // --- Permisos para ventas ---
                        .requestMatchers("/api/venta/listar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/venta/guardar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/venta/cambiar-estado/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/venta/detalle/agregar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/venta/detalle/listar/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/venta/detalle/eliminar/**").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .requestMatchers("/api/venta/pago/registrar").hasAnyRole("ADMIN", "RECEPCIONISTA")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}