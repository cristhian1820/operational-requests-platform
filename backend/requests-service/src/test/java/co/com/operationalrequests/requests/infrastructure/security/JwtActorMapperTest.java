package co.com.operationalrequests.requests.infrastructure.security;
import co.com.operationalrequests.requests.domain.model.RolActor; import java.time.Instant; import java.util.*; import org.junit.jupiter.api.Test; import org.springframework.security.oauth2.jwt.Jwt; import static org.assertj.core.api.Assertions.*;
class JwtActorMapperTest {
 @Test void mapsRealmRolesWithStableSubject(){UUID sub=UUID.randomUUID();Jwt jwt=new Jwt("token",Instant.now(),Instant.now().plusSeconds(60),Map.of("alg","none"),Map.of("sub",sub.toString(),"preferred_username","analista","realm_access",Map.of("roles",List.of("ANALISTA","offline_access"))));var actor=new JwtActorMapper().from(jwt);assertThat(actor.id()).isEqualTo(sub);assertThat(actor.username()).isEqualTo("analista");assertThat(actor.roles()).containsExactly(RolActor.ANALISTA);}
}
