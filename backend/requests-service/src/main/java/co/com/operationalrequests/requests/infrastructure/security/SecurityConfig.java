package co.com.operationalrequests.requests.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.*;
import org.springframework.context.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.*;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
 @Bean JwtAuthenticationConverter jwtAuthenticationConverter(){
  var converter=new JwtAuthenticationConverter(); converter.setJwtGrantedAuthoritiesConverter(jwt->{
   Set<GrantedAuthority> authorities=new HashSet<>(); Map<String,Object> realm=jwt.getClaim("realm_access");Object roles=realm==null?null:realm.get("roles");
   if(roles instanceof Collection<?> values) values.forEach(role->authorities.add(new SimpleGrantedAuthority("ROLE_"+role)));
   return authorities;
  }); return converter;
 }
 @Bean SecurityFilterChain apiSecurity(HttpSecurity http,JwtAuthenticationConverter converter,ObjectMapper mapper)throws Exception{
  return http.csrf(csrf->csrf.disable()).cors(cors->cors.configurationSource(corsConfigurationSource()))
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(auth->auth.requestMatchers("/actuator/health","/actuator/health/**","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll().anyRequest().authenticated())
   .oauth2ResourceServer(oauth->oauth.jwt(jwt->jwt.jwtAuthenticationConverter(converter))
    .authenticationEntryPoint((req,res,e)->writeProblem(res,mapper,401,"Unauthorized","AUTHENTICATION_REQUIRED",req.getRequestURI())))
   .exceptionHandling(ex->ex.accessDeniedHandler((req,res,e)->writeProblem(res,mapper,403,"Forbidden","ACCESS_DENIED",req.getRequestURI()))).build();
 }
 @Bean CorsConfigurationSource corsConfigurationSource(){var c=new CorsConfiguration();c.setAllowedOrigins(List.of("http://localhost:4200","http://localhost:4201"));c.setAllowedMethods(List.of("GET","POST","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type","X-Correlation-Id"));c.setExposedHeaders(List.of("Location","X-Correlation-Id"));c.setAllowCredentials(false);var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/api/**",c);return source;}
 private static void writeProblem(HttpServletResponse response,ObjectMapper mapper,int status,String title,String code,String instance)throws java.io.IOException{response.setStatus(status);response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);mapper.writeValue(response.getOutputStream(),Map.of("type","urn:problem:operational-requests:"+code.toLowerCase(),"title",title,"status",status,"detail",title,"instance",instance,"errorCode",code,"correlationId",Optional.ofNullable(response.getHeader("X-Correlation-Id")).orElse("unknown"),"timestamp",Instant.now()));}
}
