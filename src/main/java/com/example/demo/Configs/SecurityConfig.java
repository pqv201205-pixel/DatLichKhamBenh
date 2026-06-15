package com.example.demo.Configs;

import com.example.demo.Security.CustomUserDetailsService;
import com.example.demo.Security.JwtAccessDeniedHandler;
import com.example.demo.Security.JwtAuthEntryPoint;
import com.example.demo.Security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * SecurityConfig
 *
 * Cấu hình toàn bộ Spring Security:
 *  - Stateless session (JWT, không dùng HttpSession)
 *  - CORS cho phép frontend React/Vue gọi API
 *  - Phân quyền endpoint theo Role: ADMIN / DOCTOR / PATIENT
 *  - Đăng ký JwtAuthenticationFilter trước UsernamePasswordAuthenticationFilter
 *  - 401 và 403 trả về JSON (không redirect)
 *
 * Phân quyền endpoint:
 *  PUBLIC       — /api/auth/**  (đăng ký, đăng nhập, quên mật khẩu, refresh)
 *  PUBLIC GET   — /api/doctors/**, /api/specialties/**  (bệnh nhân/khách xem được)
 *  PATIENT      — đặt lịch, xem lịch cá nhân, đánh giá
 *  DOCTOR       — quản lý lịch làm việc, xem lịch hẹn của mình, cập nhật trạng thái
 *  ADMIN        — quản lý user, chuyên khoa, dashboard, tất cả lịch hẹn
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // bật @PreAuthorize, @PostAuthorize ở Service/Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    // ─── Password Encoder ─────────────────────────────────────────────────────

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // cost factor 12 — cân bằng bảo mật/hiệu năng
    }

    // ─── Authentication Provider ──────────────────────────────────────────────

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ─── Authentication Manager ───────────────────────────────────────────────

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ─── Security Filter Chain ────────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Tắt CSRF (API stateless dùng JWT không cần CSRF token)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Cấu hình CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Stateless — không tạo HttpSession
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Xử lý lỗi 401 / 403 bằng handler JSON tùy chỉnh
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))

                // 5. Phân quyền endpoint
                .authorizeHttpRequests(auth -> auth

                        // ── PUBLIC ──────────────────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        // Swagger UI (môi trường dev)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        // WebSocket handshake endpoint
                        .requestMatchers("/ws/**").permitAll()

                        // Xem danh sách bác sĩ & chuyên khoa — không cần đăng nhập
                        .requestMatchers(HttpMethod.GET,
                                "/api/doctors",
                                "/api/doctors/{id}",
                                "/api/doctors/{id}/reviews",
                                "/api/doctors/{id}/slots",
                                "/api/specialties",
                                "/api/specialties/{id}"
                        ).permitAll()

                        // ── PATIENT ─────────────────────────────────────────────────
                        .requestMatchers(
                                "/api/patients/me",
                                "/api/patients/me/update",
                                "/api/appointments/book",
                                "/api/appointments/my",
                                "/api/appointments/{id}/cancel",
                                "/api/appointments/{id}/history",
                                "/api/appointments/{id}/review",
                                "/api/appointments/{id}/medical-record",
                                "/api/files/upload",
                                "/api/waiting-list/**",
                                "/api/notifications/**"
                        ).hasRole("PATIENT")

                        // ── DOCTOR ──────────────────────────────────────────────────
                        .requestMatchers(
                                "/api/doctors/me",
                                "/api/doctors/me/update",
                                "/api/doctors/me/avatar",
                                "/api/schedules/**",
                                "/api/appointments/doctor/**",
                                "/api/appointments/{id}/status",
                                "/api/appointments/{id}/medical-record/create",
                                "/api/appointments/checkin"
                        ).hasRole("DOCTOR")

                        // ── ADMIN ────────────────────────────────────────────────────
                        .requestMatchers(
                                "/api/admin/**",
                                "/api/specialties/create",
                                "/api/specialties/{id}/update",
                                "/api/specialties/{id}/delete",
                                "/api/users/**",
                                "/api/dashboard/**",
                                "/api/audit-logs/**"
                        ).hasRole("ADMIN")

                        // DOCTOR và ADMIN đều xem được lịch hẹn tổng
                        .requestMatchers("/api/appointments/**")
                        .hasAnyRole("DOCTOR", "ADMIN")

                        // Mọi request còn lại phải đăng nhập
                        .anyRequest().authenticated()
                )

                // 6. Đăng ký authentication provider
                .authenticationProvider(authenticationProvider())

                // 7. Thêm JWT filter TRƯỚC filter xác thực username/password mặc định
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ─── CORS ─────────────────────────────────────────────────────────────────

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Cho phép frontend React/Vue/Angular gọi API
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",   // React dev
                "http://localhost:5173",   // Vite / Vue dev
                "http://localhost:4200",   // Angular dev
                "https://yourdomain.com"   // Production — thay URL thật
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Requested-With",
                "Cache-Control"
        ));

        config.setExposedHeaders(List.of("Authorization")); // frontend đọc được header này
        config.setAllowCredentials(true);
        config.setMaxAge(3600L); // cache preflight 1 giờ

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/ws/**", config);
        return source;
    }
}