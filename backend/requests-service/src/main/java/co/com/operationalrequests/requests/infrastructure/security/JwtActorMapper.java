package co.com.operationalrequests.requests.infrastructure.security;
import co.com.operationalrequests.requests.application.command.ActorActual; import co.com.operationalrequests.requests.domain.model.RolActor;
import java.util.*; import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.stereotype.Component;
@Component public class JwtActorMapper {
 public ActorActual from(Jwt jwt){
  UUID id; try{id=UUID.fromString(jwt.getSubject());}catch(Exception e){throw new IllegalArgumentException("El claim sub no es un UUID válido");}
  Map<String,Object> realm=jwt.getClaim("realm_access"); Object raw=realm==null?null:realm.get("roles"); Set<RolActor> roles=new HashSet<>();
  if(raw instanceof Collection<?> values) for(Object value:values) try{roles.add(RolActor.valueOf(String.valueOf(value)));}catch(IllegalArgumentException ignored){}
  return new ActorActual(id,jwt.getClaimAsString("preferred_username"),Set.copyOf(roles));
 }
}
