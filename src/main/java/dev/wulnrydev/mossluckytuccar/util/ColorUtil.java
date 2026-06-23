package dev.wulnrydev.mossluckytuccar.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renk dönüştürme yardımcı sınıfı.
 * Hem klasik renk kodlarını (&a, &7, &l) hem de 1.16+ Hex renk
 * kodlarını (&#RRGGBB) destekler.
 */
public final class ColorUtil {

    // &#RRGGBB formatındaki hex renk kodlarını yakalayan desen
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // Yardımcı (utility) sınıf olduğu için örneklenmesini engelliyoruz
    private ColorUtil() {
    }

    /**
     * Verilen metindeki tüm renk kodlarını (hex + klasik) işler.
     *
     * @param text renklendirilecek ham metin
     * @return Minecraft'ın anlayacağı renklendirilmiş metin
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Önce hex renk kodlarını dönüştür
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder builder = new StringBuilder(text.length() + 32);
        while (matcher.find()) {
            String hex = matcher.group(1);
            // Hex kodunu bungee ChatColor formatına çevir
            matcher.appendReplacement(builder, Matcher.quoteReplacement(ChatColor.of("#" + hex).toString()));
        }
        matcher.appendTail(builder);

        // Ardından klasik & renk kodlarını dönüştür
        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    /**
     * Bir metin listesinin tamamını renklendirir.
     *
     * @param list renklendirilecek satırlar
     * @return renklendirilmiş yeni liste
     */
    public static List<String> colorize(List<String> list) {
        List<String> result = new ArrayList<>(list.size());
        for (String line : list) {
            result.add(colorize(line));
        }
        return result;
    }
}
