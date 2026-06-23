package dev.wulnrydev.mossluckytuccar.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Zaman hesaplamaları için yardımcı sınıf.
 * Bir sonraki yenilenme zamanını hesaplar ve kalan süreyi
 * "XX Saat XX Dakika" formatında biçimlendirir.
 */
public final class TimeUtil {

    private TimeUtil() {
    }

    /**
     * Verilen saate göre bir sonraki yenilenme anının epoch (millis) değerini hesaplar.
     * Eğer bugünkü hedef saat henüz geçmediyse bugünü, geçtiyse yarını döndürür.
     *
     * @param refreshHour yenilenme saati (0-23)
     * @return bir sonraki yenilenme zamanının epoch milisaniye değeri
     */
    public static long calculateNextRefresh(int refreshHour) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.now(zone);
        // Bugünün hedef saati (örn: bugün 18:00)
        LocalDateTime target = now.toLocalDate().atTime(LocalTime.of(refreshHour, 0));

        // Hedef saat geçtiyse bir sonraki güne taşı
        if (!target.isAfter(now)) {
            target = target.plusDays(1);
        }

        return target.atZone(zone).toInstant().toEpochMilli();
    }

    /**
     * Şu an ile hedef zaman arasındaki kalan süreyi "XX Saat XX Dakika" formatında döndürür.
     *
     * @param targetEpochMillis hedef zamanın epoch milisaniye değeri
     * @return biçimlendirilmiş kalan süre metni
     */
    public static String formatRemaining(long targetEpochMillis) {
        long remainingMillis = targetEpochMillis - System.currentTimeMillis();
        if (remainingMillis < 0) {
            remainingMillis = 0;
        }

        Duration duration = Duration.ofMillis(remainingMillis);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        // 1 dakikadan az kaldıysa "Az kaldı" göster
        if (hours == 0 && minutes == 0) {
            return "1 Dakikadan az";
        }

        return hours + " Saat " + minutes + " Dakika";
    }

    /**
     * Hedef zamana kalan tam saat sayısını döndürür (örn: 5).
     */
    public static long remainingHours(long targetEpochMillis) {
        long remainingMillis = Math.max(0, targetEpochMillis - System.currentTimeMillis());
        return Duration.ofMillis(remainingMillis).toHours();
    }

    /**
     * Hedef zamana kalan, saatten artan dakika sayısını döndürür (0-59).
     */
    public static long remainingMinutes(long targetEpochMillis) {
        long remainingMillis = Math.max(0, targetEpochMillis - System.currentTimeMillis());
        return Duration.ofMillis(remainingMillis).toMinutesPart();
    }
}
