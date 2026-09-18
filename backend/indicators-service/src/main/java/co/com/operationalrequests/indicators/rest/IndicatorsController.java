package co.com.operationalrequests.indicators.rest;

import co.com.operationalrequests.indicators.application.AnalyticsService;
import co.com.operationalrequests.indicators.application.IndicatorDtos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@RestController
@Validated
@RequestMapping("/api/v1/indicadores")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ANALISTA','SUPERVISOR')")
public class IndicatorsController {
    private final AnalyticsService service;

    public IndicatorsController(AnalyticsService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    @Operation(summary = "Solicitudes actuales agrupadas por estado y categoría")
    public IndicatorDtos.Summary summary() {
        return service.summary();
    }

    @GetMapping("/tendencia")
    @Operation(summary = "Solicitudes registradas por día UTC")
    public IndicatorDtos.Trend trend(@RequestParam(name = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde, @RequestParam(name = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate end = hasta == null ? LocalDate.now(ZoneOffset.UTC) : hasta;
        LocalDate start = desde == null ? end.minusDays(29) : desde;
        if (start.isAfter(end) || ChronoUnit.DAYS.between(start, end) > 366)
            throw new IllegalArgumentException("El rango debe ser válido y no superar 366 días");
        return service.trend(start, end);
    }
}
