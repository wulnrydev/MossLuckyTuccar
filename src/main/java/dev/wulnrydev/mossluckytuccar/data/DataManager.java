package dev.wulnrydev.mossluckytuccar.data;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.manager.TraderManager;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * data.yml dosyası ile kalıcı veri saklama işlemlerini yöneten sınıf.
 * Kalan stok verileri ve bir sonraki yenilenme tarihi burada tutulur;
 * böylece sunucu restart/reload olduğunda veriler kaybolmaz.
 *
 * KRİTİK: Kaydetme işlemleri ana thread'i meşgul etmemek için ASENKRON yapılır.
 * Ayrıca:
 *  - Tüm disk yazımları tek bir kilit (saveLock) üzerinden SIRAYLA yapılır;
 *    böylece iki asenkron görevin aynı dosyaya aynı anda yazıp onu bozması önlenir.
 *  - Sık tekrarlanan kayıtlar (her satış) doğrudan diske yazmaz; "dirty" (kirli)
 *    olarak işaretlenir ve periyodik görev topluca kaydeder (I/O yükü azalır).
 */
public class DataManager {

    // Periyodik otomatik kaydetme aralığı: 30 saniye (tick cinsinden)
    private static final long AUTO_SAVE_INTERVAL_TICKS = 20L * 30L;

    private final MossLuckyTuccar plugin;
    private final File dataFile;

    // Disk yazımını serileştiren kilit: aynı dosyaya eşzamanlı yazıp
    // dosyayı bozmayı (race condition) engeller.
    private final Object saveLock = new Object();

    // Bekleyen (kaydedilmemiş) değişiklik var mı?
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    // Periyodik otomatik kaydetme görevi
    private BukkitTask autoSaveTask;

    public DataManager(MossLuckyTuccar plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
    }

    /**
     * data.yml içeriğini okuyup TraderManager'ın çalışma anı durumuna yükler.
     * Sunucu açılışında bir kez (senkron) çağrılır.
     *
     * @param trader verilerin yükleneceği yönetici
     */
    public void loadInto(TraderManager trader) {
        if (!dataFile.exists()) {
            // İlk açılış: veri yok, varsayılan durumda kalır
            return;
        }

        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

        // Bir sonraki yenilenme zamanı (epoch millis)
        long nextRefresh = data.getLong("next-refresh", 0L);
        trader.setNextRefresh(nextRefresh);

        // Talep edilen güncel eşyalar
        List<Material> items = new ArrayList<>();
        for (String raw : data.getStringList("current-items")) {
            Material material = Material.matchMaterial(raw);
            if (material != null) {
                items.add(material);
            }
        }
        trader.setCurrentItemsRaw(items);

        // Her eşyanın kalan stoğu
        if (data.isConfigurationSection("stock")) {
            for (String key : data.getConfigurationSection("stock").getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material != null) {
                    trader.setStockRaw(material, data.getInt("stock." + key));
                }
            }
        }
    }

    /**
     * Periyodik (asenkron) otomatik kaydetmeyi başlatır.
     * Yalnızca bekleyen değişiklik (dirty) varsa diske yazar.
     * onEnable içinde bir kez çağrılır.
     */
    public void startAutoSave(TraderManager trader) {
        this.autoSaveTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                () -> {
                    // Bekleyen değişiklik varsa bayrağı sıfırla ve kaydet
                    if (dirty.compareAndSet(true, false)) {
                        writeSnapshot(trader);
                    }
                }, AUTO_SAVE_INTERVAL_TICKS, AUTO_SAVE_INTERVAL_TICKS);
    }

    /**
     * Periyodik otomatik kaydetmeyi durdurur (eklenti kapanırken).
     */
    public void stopAutoSave() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
            autoSaveTask = null;
        }
    }

    /**
     * Sık çağrılan yerlerde (örn. her satış) kullanılır.
     * Diske ANINDA yazmaz; sadece "kaydedilecek" olarak işaretler.
     * Asıl yazma işini periyodik görev üstlenir; böylece yoğun satışta
     * disk gereksiz yere defalarca yazılmaz.
     */
    public void requestSave() {
        dirty.set(true);
    }

    /**
     * Önemli olaylarda (örn. yenilenme) durumu ANINDA asenkron kaydeder.
     * Ana thread bloklanmaz.
     */
    public void saveAsync(TraderManager trader) {
        // Bu kayıt, bekleyen tüm değişiklikleri de kapsar
        dirty.set(false);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> writeSnapshot(trader));
    }

    /**
     * Sunucu kapanırken verileri SENKRON kaydeder.
     * (onDisable içinde asenkron görev planlanamayacağı için.)
     */
    public void saveSync(TraderManager trader) {
        dirty.set(false);
        writeSnapshot(trader);
    }

    /**
     * Yöneticiden güncel durumun anlık görüntüsünü (snapshot) alır ve diske yazar.
     * TraderManager'ın eşzamanlı (concurrent) veri yapıları sayesinde snapshot
     * herhangi bir thread'den güvenle alınabilir.
     */
    private void writeSnapshot(TraderManager trader) {
        final long nextRefresh = trader.getNextRefresh();
        final List<String> itemNames = new ArrayList<>();
        for (Material material : trader.getCurrentItems()) {
            itemNames.add(material.name());
        }
        final Map<Material, Integer> stockSnapshot = trader.getStockSnapshot();
        writeToDisk(nextRefresh, itemNames, stockSnapshot);
    }

    /**
     * Asıl disk yazma işlemi. Tüm kaydetme yolları bunu kullanır ve
     * saveLock sayesinde aynı anda yalnızca bir yazma gerçekleşir.
     */
    private void writeToDisk(long nextRefresh, List<String> itemNames, Map<Material, Integer> stock) {
        synchronized (saveLock) {
            FileConfiguration data = new YamlConfiguration();
            data.set("next-refresh", nextRefresh);
            data.set("current-items", itemNames);

            for (Map.Entry<Material, Integer> entry : stock.entrySet()) {
                data.set("stock." + entry.getKey().name(), entry.getValue());
            }

            try {
                data.save(dataFile);
            } catch (IOException exception) {
                plugin.getLogger().log(Level.SEVERE, "data.yml kaydedilemedi!", exception);
            }
        }
    }
}
