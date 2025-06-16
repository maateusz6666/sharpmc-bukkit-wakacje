package sharpmc.pl.utils;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeUtil {

    static String format = "{d} {h} {m} {s}";

    public static String getTime(int seconds) {
        return formatDuration(Duration.ofSeconds(seconds));
    }

    public static String getTime(Duration duration) {
        return formatDuration(duration);
    }

    private static String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long secs = duration.toSecondsPart();

        String d = days > 0 ? days + "dni." : "";
        String h = hours > 0 ? hours + "godz." : "";
        String m = minutes > 0 ? minutes + "min." : "";
        String s = secs > 0 || (days == 0 && hours == 0 && minutes == 0) ? secs + "sek." : "";

        String timeFormat = format
                .replace("{d}", d)
                .replace("{h}", h)
                .replace("{m}", m)
                .replace("{s}", s);

        return timeFormat.trim();
    }

    public static String getActuallyDate() {
        Date data = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(data);
    }

    public static String timeToString(final long time) {
        if (time < 1L) {
            return "< 1s";
        }

        final long months = TimeUnit.MILLISECONDS.toDays(time) / 30L;
        final long days = TimeUnit.MILLISECONDS.toDays(time) % 30L;
        final long hours = TimeUnit.MILLISECONDS.toHours(time) - TimeUnit.DAYS
                .toHours(TimeUnit.MILLISECONDS.toDays(time));
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(time) - TimeUnit.HOURS
                .toMinutes(TimeUnit.MILLISECONDS.toHours(time));
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES
                .toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));

        final StringBuilder stringBuilder = new StringBuilder();
        if (months > 0L) {
            stringBuilder.append(months).append("msc")
                    .append(" ");
        }
        if (days > 0L) {
            stringBuilder.append(days).append("dni.")
                    .append(" ");
        }
        if (hours > 0L) {
            stringBuilder.append(hours).append("godz.")
                    .append(" ");
        }
        if (minutes > 0L) {
            stringBuilder.append(minutes).append("min.")
                    .append(" ");
        }
        if (seconds > 0L) {
            stringBuilder.append(seconds).append("sek.");
        }

        return !stringBuilder.isEmpty() ? stringBuilder.toString().trim() : time + "ms.";
    }

    public static String formatTime(long timeMillis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
}
