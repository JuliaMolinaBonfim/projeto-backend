
package br.com.taskflow.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import com.nimbusds.jose.proc.SecurityContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class SecurityConfig {

    // Algoritmo que transforma a senha em hash antes de salvar no banco.
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    // Gera (assina) os tokens JWT no login.
    @Bean
    fun jwtEncoder(@Value("\${app.jwt.secret}") segredo: String): JwtEncoder {
        return NimbusJwtEncoder(ImmutableSecret<SecurityContext>(criarChave(segredo)))
    }

    // Valida os tokens JWT que chegam em cada requisição (assinatura e validade).
    @Bean
    fun jwtDecoder(@Value("\${app.jwt.secret}") segredo: String): JwtDecoder {
        return NimbusJwtDecoder.withSecretKey(criarChave(segredo))
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // CSRF protege sessões/cookies. Como a API é stateless e usa token no header, é desnecessário.
            .csrf { it.disable() }
            // Sem sessão no servidor: cada requisição precisa carregar o próprio token.
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                // As regras são avaliadas de cima para baixo; a primeira que combinar vence.
                it.requestMatchers("/api/auth/**", "/error").permitAll()
                it.requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                it.requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                it.requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                it.anyRequest().authenticated()
            }
            .oauth2ResourceServer { recurso ->
                recurso.jwt { jwt -> jwt.jwtAuthenticationConverter(conversorDeJwt()) }
            }

        return http.build()
    }

    // Diz ao Spring onde ficam os perfis dentro do token (claim "roles") e
    // adiciona o prefixo ROLE_ (ex.: "ADMIN" vira "ROLE_ADMIN", que é o que hasRole("ADMIN") espera).
    private fun conversorDeJwt(): JwtAuthenticationConverter {
        val autoridades = JwtGrantedAuthoritiesConverter()
        autoridades.setAuthoritiesClaimName("roles")
        autoridades.setAuthorityPrefix("ROLE_")

        val conversor = JwtAuthenticationConverter()
        conversor.setJwtGrantedAuthoritiesConverter(autoridades)
        return conversor
    }

    private fun criarChave(segredo: String): SecretKey {
        val bytes = segredo.toByteArray(Charsets.UTF_8)
        require(bytes.size >= 32) { "A propriedade app.jwt.secret precisa ter no mínimo 32 caracteres" }
        return SecretKeySpec(bytes, "HmacSHA256")
    }
}
