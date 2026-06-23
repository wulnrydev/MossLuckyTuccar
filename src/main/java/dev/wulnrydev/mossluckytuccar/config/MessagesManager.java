package dev.wulnrydev.mossluckytuccar.config;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.util.ColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * messages.yml dosyasını yöneten sınıf.
 * Tüm mesajlar önbelleğe alınır, {prefix} ve diğer değişkenler
 * (placeholder) çalışma anında değiştirilir.
 */
public class MessagesManager {

    private final MossLuckyTuccar plugin;
    private final File messagesFile;

    // Mesajlar ham (renksiz) haliyle önbellekte tutulur
    private final Map<String, String> cache = new HashMap<>();
    private String prefix = "";

    public MessagesManager(MossLuckyTuccar plugin) {
        this.plugin = plugin;
        this.messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        load();
    }

    /**
     * messages.yml dosyasını yükler ve tüm mesajları önbelleğe alır.
     */
    public void load() {
        // Dosya yoksa jar içinden kopyala
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(messagesFile);
        cache.clear();

        // Prefix'i ayrı tut (diğer mesajlarda {prefix} olarak kullanılır)
        this.prefix = config.getString("prefix", "");

        // Tüm anahtarları önbelleğe al
        for (String key : config.getKeys(false)) {
            if (config.isString(key)) {
                cache.put(key, config.getString(key));
            }
        }
    }

    /**
     * Bir mesajı placeholder'larını değiştirip renklendirerek döndürür.
     *
     * @param key          mesaj anahtarı
     * @param replacements değişken-değer çiftleri (ör: "{miktar}", "5")
     * @return renklendirilmiş, hazır mesaj
     */
    public String get(String key, String... replacements) {
        String message = cache.getOrDefault(key, "&cMesaj bulunamadı: " + key);

        // {prefix} değişkenini her zaman değiştir
        message = message.replace("{prefix}", prefix);

        // Verilen diğer değişkenleri sırayla değiştir
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        return ColorUtil.colorize(message);
    }

    /**
     * Bir alıcıya doğrudan mesaj gönderir (mesaj boşsa gönderilmez).
     */
    public void send(CommandSender target, String key, String... replacements) {
        String message = get(key, replacements);
        if (!message.isEmpty()) {
            target.sendMessage(message);
        }
    }

    public String getPrefix() {
        return ColorUtil.colorize(prefix);
    }
}
