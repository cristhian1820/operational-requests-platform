package co.com.operationalrequests.indicators.application;
import java.time.*; import java.util.*;
public final class IndicatorDtos { private IndicatorDtos(){}
 public record StateCount(String estado,long cantidad){}
 public record CategoryCount(UUID categoriaId,String categoria,long cantidad){}
 public record Summary(List<StateCount> porEstado,List<CategoryCount> porCategoria,long total,Instant actualizadoEn){}
 public record TrendPoint(LocalDate fecha,long cantidadRegistradas){}
 public record Trend(List<TrendPoint> puntos){}
 public enum ProcessingResult { PROCESSED,DUPLICATE }
 public record ProcessingOutcome(ProcessingResult result,UUID eventId,String type){}
}
