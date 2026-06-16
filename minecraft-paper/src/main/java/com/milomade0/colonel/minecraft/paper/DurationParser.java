package com.milomade0.colonel.minecraft.paper;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Small duration parser for values like 10s, 5m, 2h, 3d, 1w or combined values like 1h30m.
 */
final class DurationParser {

    private static final Pattern PART_PATTERN = Pattern.compile("(\\d+)\\s*(ms|millis?|milliseconds?|s|sec|seconds?|m|min|minutes?|h|hours?|d|days?|w|weeks?)");

    private DurationParser() {
    }

    static Duration parse(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Duration cannot be empty.");
        }

        String normalized = input.trim().toLowerCase(Locale.ROOT).replace(" ", "");
        Matcher matcher = PART_PATTERN.matcher(normalized);

        Duration duration = Duration.ZERO;
        int end = 0;
        while (matcher.find()) {
            if (matcher.start() != end) {
                throw new IllegalArgumentException("Invalid duration: " + input);
            }

            long amount = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            duration = switch (unit) {
                case "ms", "milli", "millis", "millisecond", "milliseconds" -> duration.plusMillis(amount);
                case "s", "sec", "second", "seconds" -> duration.plusSeconds(amount);
                case "m", "min", "minute", "minutes" -> duration.plusMinutes(amount);
                case "h", "hour", "hours" -> duration.plusHours(amount);
                case "d", "day", "days" -> duration.plusDays(amount);
                case "w", "week", "weeks" -> duration.plusDays(amount * 7);
                default -> throw new IllegalArgumentException("Invalid duration unit: " + unit);
            };
            end = matcher.end();
        }

        if (end != normalized.length() || duration.isZero()) {
            throw new IllegalArgumentException("Invalid duration: " + input);
        }

        return duration;
    }
}
