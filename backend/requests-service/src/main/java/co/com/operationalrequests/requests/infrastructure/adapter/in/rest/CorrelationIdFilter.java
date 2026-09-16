package co.com.operationalrequests.requests.infrastructure.adapter.in.rest;
import jakarta.servlet.*; import jakarta.servlet.http.*; import java.io.IOException; import java.util.UUID;
import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.Ordered; import org.springframework.core.annotation.Order;
@Component @Order(Ordered.HIGHEST_PRECEDENCE) public class CorrelationIdFilter extends OncePerRequestFilter {
 public static final String HEADER="X-Correlation-Id",ATTRIBUTE="correlationId";
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
  UUID id=parse(request.getHeader(HEADER));request.setAttribute(ATTRIBUTE,id);response.setHeader(HEADER,id.toString());chain.doFilter(request,response);
 }
 private static UUID parse(String value){if(value==null||value.length()>36)return UUID.randomUUID();try{return UUID.fromString(value);}catch(IllegalArgumentException e){return UUID.randomUUID();}}
}
