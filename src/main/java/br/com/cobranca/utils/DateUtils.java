package br.com.cobranca.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public class DateUtils {

    private static final DateTimeFormatter FLEXIBLE_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    private static final DateTimeFormatter FLEXIBLE_BR_DATETIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("dd/MM/yyyy HH:mm:ss")
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter();

    public static LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        String str = dateStr.trim();

        // 1. Tenta parse direto de LocalDate ISO (ex: "2026-07-30")
        try {
            return LocalDate.parse(str, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ignored) {
        }

        // 2. Tenta parse de formatos com fuso horário / Offset (ex: "2026-07-30T07:57:41.884Z" ou "2026-07-30T07:57:41.884-03:00")
        try {
            return OffsetDateTime.parse(str).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return ZonedDateTime.parse(str).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        // 3. Tenta parse de LocalDateTime ISO sem Fuso (ex: "2026-07-30T07:57:41.884744152" ou "2026-07-30T07:57:41")
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        // 4. Tenta os formatadores flexíveis com frações variáveis de segundo (.3, .884, etc.)
        try {
            return LocalDateTime.parse(str, FLEXIBLE_DATETIME_FORMATTER).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(str, FLEXIBLE_BR_DATETIME_FORMATTER).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        // 5. Lista de padrões mais comuns em bancos de dados, APIs e formatos brasileiros
        String[] patterns = {
            "dd/MM/yyyy", // 30/07/2026
            "dd/MM/yyyy HH:mm:ss", // 30/07/2026 07:57:41
            "dd/MM/yyyy HH:mm:ss.SSS", // 30/07/2026 07:57:41.884
            "dd/MM/yyyy HH:mm", // 30/07/2026 07:57
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSS",// 2026-07-30 07:57:41.884744152
            "yyyy-MM-dd HH:mm:ss.SSS", // 2026-07-30 07:57:41.884
            "yyyy-MM-dd HH:mm:ss", // 2026-07-30 07:57:41
            "yyyy-MM-dd HH:mm", // 2026-07-30 07:57
            "yyyyMMdd", // 20260730 (Comum em arquivos CNAB/Retorno bancário)
            "ddMMyyyy" // 30072026
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

                // Se a string contiver hora no padrão, tenta ler como LocalDateTime e extrai a data
                if (pattern.contains("HH")) {
                    return LocalDateTime.parse(str, formatter).toLocalDate();
                }

                return LocalDate.parse(str, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Não foi possível realizar o parse do valor para LocalDate: " + dateStr);
    }

    public static OffsetDateTime parseToOffsetDateTime(String dateStr) {
        return parseToOffsetDateTime(dateStr, ZoneId.systemDefault());
    }

    public static OffsetDateTime parseToOffsetDateTime(String dateStr, ZoneId defaultZone) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        String str = dateStr.trim();

        // 1. Tenta parse direto de OffsetDateTime (ex: "2026-07-30T07:57:41.884-03:00" ou "2026-07-30T07:57:41.884Z")
        try {
            return OffsetDateTime.parse(str);
        } catch (Exception ignored) {
        }

        // 2. Tenta parse de ZonedDateTime (ex: "2026-07-30T07:57:41.884-03:00[America/Sao_Paulo]")
        try {
            return ZonedDateTime.parse(str).toOffsetDateTime();
        } catch (Exception ignored) {
        }

        // 3. Tenta parse de LocalDateTime ISO sem Fuso (ex: "2026-07-30T07:57:41.884744152" ou "2026-07-30T07:57:41")
        try {
            LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.atZone(defaultZone).toOffsetDateTime();
        } catch (Exception ignored) {
        }

        // 4. Formatos customizados comuns em APIs (Espaço ao invés de 'T', padrões brasileiros, etc.)
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss.SSSSSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSSSSS",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss"
        };

        for (String pattern : patterns) {
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime ldt = LocalDateTime.parse(str, dtf);
                return ldt.atZone(defaultZone).toOffsetDateTime();
            } catch (Exception ignored) {
            }
        }

        // 5. Se for apenas Data sem Hora (ex: "2026-07-30" ou "30/07/2026")
        try {
            LocalDate ld = LocalDate.parse(str.contains("/")
                    ? LocalDate.parse(str, DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString() : str);
            return ld.atStartOfDay(defaultZone).toOffsetDateTime();
        } catch (Exception ignored) {
        }

        throw new IllegalArgumentException("Formato de data/hora não reconhecido: " + dateStr);
    }
}
