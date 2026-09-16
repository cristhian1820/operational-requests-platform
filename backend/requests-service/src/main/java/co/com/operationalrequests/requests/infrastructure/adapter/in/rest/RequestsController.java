package co.com.operationalrequests.requests.infrastructure.adapter.in.rest;
import co.com.operationalrequests.requests.application.command.*; import co.com.operationalrequests.requests.application.port.in.RequestsUseCase;
import co.com.operationalrequests.requests.application.query.*; import co.com.operationalrequests.requests.domain.model.*;
import co.com.operationalrequests.requests.infrastructure.adapter.in.rest.ApiDtos.*; import co.com.operationalrequests.requests.infrastructure.security.JwtActorMapper;
import io.swagger.v3.oas.annotations.*; import io.swagger.v3.oas.annotations.media.*; import io.swagger.v3.oas.annotations.responses.*; import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest; import jakarta.validation.Valid; import jakarta.validation.constraints.Max; import jakarta.validation.constraints.Min;
import java.net.URI; import java.util.*; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt; import org.springframework.web.bind.annotation.*; import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.validation.annotation.Validated;

@RestController @Validated @RequestMapping("/api/v1") @SecurityRequirement(name="bearerAuth")
@ApiResponses({@ApiResponse(responseCode="400",description="Petición inválida"),@ApiResponse(responseCode="401",description="JWT ausente o inválido"),@ApiResponse(responseCode="403",description="Rol o acceso insuficiente"),@ApiResponse(responseCode="404",description="Solicitud inexistente"),@ApiResponse(responseCode="409",description="Conflicto concurrente"),@ApiResponse(responseCode="422",description="Regla de negocio incumplida")})
public class RequestsController {
 private final RequestsUseCase useCase; private final JwtActorMapper actors;
 public RequestsController(RequestsUseCase useCase,JwtActorMapper actors){this.useCase=useCase;this.actors=actors;}
 @GetMapping("/categorias") @PreAuthorize("hasAnyRole('SOLICITANTE','ANALISTA','SUPERVISOR')")
 @Operation(summary="Consulta categorías activas") public List<Categoria> categories(){return useCase.categories();}
 @PostMapping("/solicitudes") @PreAuthorize("hasRole('SOLICITANTE')")
 @Operation(summary="Crea una solicitud",responses={@ApiResponse(responseCode="201"),@ApiResponse(responseCode="400"),@ApiResponse(responseCode="403")})
 public ResponseEntity<SolicitudDetail> create(@Valid @RequestBody CreateRequest body,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request){
  var result=useCase.create(new CrearSolicitudCommand(body.asunto(),body.descripcion(),body.categoriaId(),body.prioridad(),actors.from(jwt),correlation(request)));
  URI location=ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(result.id()).toUri();return ResponseEntity.created(location).body(result);
 }
 @GetMapping("/solicitudes") @PreAuthorize("hasAnyRole('SOLICITANTE','ANALISTA','SUPERVISOR')")
 @Operation(summary="Lista solicitudes con filtros y paginación")
 public PageResult<SolicitudSummary> list(@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="20") @Min(1) @Max(100) int size,
   @RequestParam(required=false) EstadoSolicitud estado,@RequestParam(required=false) UUID categoriaId,@RequestParam(required=false) Prioridad prioridad,@AuthenticationPrincipal Jwt jwt){
  return useCase.list(new SolicitudCriteria(estado,categoriaId,prioridad,null,page,size),actors.from(jwt));
 }
 @GetMapping("/solicitudes/{id}") @PreAuthorize("hasAnyRole('SOLICITANTE','ANALISTA','SUPERVISOR')")
 @Operation(summary="Consulta el detalle, observaciones e historial") public SolicitudDetail detail(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){return useCase.detail(id,actors.from(jwt));}
 @PostMapping("/solicitudes/{id}/asignaciones") @PreAuthorize("hasRole('ANALISTA')")
 @Operation(summary="Toma atómicamente una solicitud REGISTRADA",responses={@ApiResponse(responseCode="200"),@ApiResponse(responseCode="409")})
 public SolicitudDetail assign(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request){return useCase.assign(id,actors.from(jwt),correlation(request));}
 @PostMapping("/solicitudes/{id}/observaciones") @PreAuthorize("hasRole('ANALISTA')")
 @Operation(summary="Agrega una observación como analista asignado") public SolicitudDetail observe(@PathVariable UUID id,@Valid @RequestBody ObservationRequest body,@AuthenticationPrincipal Jwt jwt){return useCase.observe(id,body.texto(),actors.from(jwt));}
 @PostMapping("/solicitudes/{id}/transiciones") @PreAuthorize("hasAnyRole('ANALISTA','SUPERVISOR')")
 @Operation(summary="Resuelve, devuelve o cierra una solicitud",responses={@ApiResponse(responseCode="200"),@ApiResponse(responseCode="403"),@ApiResponse(responseCode="422")})
 public SolicitudDetail transition(@PathVariable UUID id,@Valid @RequestBody TransitionRequest body,@AuthenticationPrincipal Jwt jwt,HttpServletRequest request){return useCase.transition(new TransicionarSolicitudCommand(id,body.estadoDestino(),body.motivo(),body.observacion(),actors.from(jwt),correlation(request)));}
 private static UUID correlation(HttpServletRequest request){return (UUID)request.getAttribute(CorrelationIdFilter.ATTRIBUTE);}
}
