package co.com.operationalrequests.indicators.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;

import static co.com.operationalrequests.indicators.application.IndicatorDtos.*;

@Service
public class AnalyticsService {
    private static final Set<String> TYPES = Set.of("SolicitudRegistrada", "SolicitudTomada", "SolicitudResuelta", "SolicitudCerrada");
    private static final Set<String> STATUSES = Set.of("REGISTRADA", "EN_ATENCION", "RESUELTA", "CERRADA");
    private static final Set<String> ROLES = Set.of("SOLICITANTE", "ANALISTA", "SUPERVISOR");
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public AnalyticsService(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Transactional
    public ProcessingOutcome process(String json) {
        EventEnvelope e = parse(json);
        validate(e);
        try {
            jdbc.update("INSERT INTO processed_event(event_id,event_type,processed_at) VALUES(?,?,SYSUTCDATETIME())", e.eventId(), e.type());
        } catch (DuplicateKeyException duplicate) {
            return new ProcessingOutcome(ProcessingResult.DUPLICATE, e.eventId(), e.type());
        }
        var p = e.payload();
        UUID category = uuid(p, "categoryId");
        String status = text(p, "status");
        String role = text(p, "actorRole");
        String readable = text(p, "readableId");
        String priority = text(p, "priority");
        int dateKey = date(e.occurredAt());
        long categoryKey = category(category);
        int toStatus = status(status);
        int roleKey = role(role);
        Integer fromStatus = fromStatus(e.type());
        jdbc.update("INSERT INTO fact_request_transition(event_id,request_id,date_key,category_key,from_status_key,to_status_key,actor_role_key,occurred_at) VALUES(?,?,?,?,?,?,?,?)", e.eventId(), e.aggregateId(), dateKey, categoryKey, fromStatus, toStatus, roleKey, Timestamp.from(e.occurredAt()));
        jdbc.update("""
                MERGE request_current_state WITH (HOLDLOCK) AS target USING (SELECT ? request_id) source ON target.request_id=source.request_id WHEN MATCHED THEN UPDATE SET readable_id=?,category_key=?,status_key=?,priority=?,last_event_id=?,updated_at=? WHEN NOT MATCHED THEN INSERT(request_id,readable_id,category_key,status_key,priority,registered_date_key,last_event_id,updated_at) VALUES(?,?,?,?,?,?,?,?);
                """, e.aggregateId(), readable, categoryKey, toStatus, priority, e.eventId(), Timestamp.from(e.occurredAt()), e.aggregateId(), readable, categoryKey, toStatus, priority, dateKey, e.eventId(), Timestamp.from(e.occurredAt()));
        return new ProcessingOutcome(ProcessingResult.PROCESSED, e.eventId(), e.type());
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        var states = jdbc.query("SELECT s.status_code,COUNT_BIG(*) FROM request_current_state c JOIN dim_status s ON s.status_key=c.status_key GROUP BY s.status_code ORDER BY s.status_code", (r, n) -> new StateCount(r.getString(1), r.getLong(2)));
        var categories = jdbc.query("SELECT d.category_id,d.category_name,COUNT_BIG(*) FROM request_current_state c JOIN dim_category d ON d.category_key=c.category_key GROUP BY d.category_id,d.category_name ORDER BY d.category_name", (r, n) -> new CategoryCount(r.getObject(1, UUID.class), r.getString(2), r.getLong(3)));
        Long total = jdbc.queryForObject("SELECT COUNT_BIG(*) FROM request_current_state", Long.class);
        Instant updated = jdbc.queryForObject("SELECT MAX(updated_at) FROM request_current_state", (r, n) -> r.getTimestamp(1) == null ? null : r.getTimestamp(1).toInstant());
        return new Summary(states, categories, total == null ? 0 : total, updated);
    }

    @Transactional(readOnly = true)
    public Trend trend(LocalDate from, LocalDate to) {
        var points = jdbc.query("SELECT d.full_date,COUNT_BIG(*) FROM request_current_state c JOIN dim_date d ON d.date_key=c.registered_date_key WHERE d.full_date BETWEEN ? AND ? GROUP BY d.full_date ORDER BY d.full_date", (r, n) -> new TrendPoint(r.getDate(1).toLocalDate(), r.getLong(2)), from, to);
        return new Trend(points);
    }

    private EventEnvelope parse(String json) {
        try {
            return mapper.readValue(json, EventEnvelope.class);
        } catch (Exception ex) {
            throw new InvalidEventException("Formato JSON inválido", ex);
        }
    }

    private static void validate(EventEnvelope e) {
        if (e.eventId() == null || e.aggregateId() == null || e.occurredAt() == null || e.correlationId() == null)
            throw new InvalidEventException("Metadatos obligatorios ausentes");
        if (!TYPES.contains(e.type())) throw new InvalidEventException("Tipo no soportado");
        if (e.version() != 1) throw new InvalidEventException("Versión no soportada");
        if (e.payload() == null || !e.payload().isObject()) throw new InvalidEventException("Payload inválido");
        String status = text(e.payload(), "status"), role = text(e.payload(), "actorRole");
        if (!STATUSES.contains(status) || !ROLES.contains(role))
            throw new InvalidEventException("Estado o rol inválido");
        uuid(e.payload(), "requestId");
        uuid(e.payload(), "categoryId");
        text(e.payload(), "readableId");
        text(e.payload(), "priority");
    }

    private int date(Instant instant) {
        LocalDate d = instant.atZone(ZoneOffset.UTC).toLocalDate();
        int key = d.getYear() * 10000 + d.getMonthValue() * 100 + d.getDayOfMonth();
        jdbc.update("IF NOT EXISTS(SELECT 1 FROM dim_date WITH (UPDLOCK,HOLDLOCK) WHERE date_key=?) INSERT " +
                "INTO dim_date(date_key,full_date,year_number,month_number,day_number) VALUES(?,?,?,?,?)", key, key, d, d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        return key;
    }

    private long category(UUID id) {
        String value = id.toString();
        jdbc.update("IF NOT EXISTS(SELECT 1 FROM dim_category WITH (UPDLOCK,HOLDLOCK) WHERE category_id=?) INSERT " +
                "INTO dim_category(category_id,category_code,category_name,valid_from) VALUES(?,?,?,SYSUTCDATETIME())", id, id, value, "Categoría " + value);
        return jdbc.queryForObject("SELECT category_key FROM dim_category WHERE category_id=?", Long.class, id);
    }

    private int status(String code) {
        return jdbc.queryForObject("SELECT status_key FROM dim_status WHERE status_code=?", Integer.class, code);
    }

    private int role(String code) {
        return jdbc.queryForObject("SELECT actor_role_key FROM dim_actor_role WHERE role_code=?", Integer.class, code);
    }

    private Integer fromStatus(String type) {
        return switch (type) {
            case "SolicitudRegistrada" -> null;
            case "SolicitudTomada" -> status("REGISTRADA");
            case "SolicitudResuelta" -> status("EN_ATENCION");
            case "SolicitudCerrada" -> status("RESUELTA");
            default -> throw new InvalidEventException("Tipo no soportado");
        };
    }

    private static String text(com.fasterxml.jackson.databind.JsonNode p, String field) {
        var n = p.get(field);
        if (n == null || !n.isTextual() || n.asText().isBlank())
            throw new InvalidEventException("Campo requerido inválido: " + field);
        return n.asText();
    }

    private static UUID uuid(com.fasterxml.jackson.databind.JsonNode p, String field) {
        try {
            return UUID.fromString(text(p, field));
        } catch (IllegalArgumentException ex) {
            throw new InvalidEventException("UUID inválido: " + field);
        }
    }
}
