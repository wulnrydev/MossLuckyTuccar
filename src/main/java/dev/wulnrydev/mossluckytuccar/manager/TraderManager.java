package dev.wulnrydev.mossluckytuccar.manager;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.config.ConfigManager;
import dev.wulnrydev.mossluckytuccar.util.TimeUtil;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Şanslı Tüccar'ın çekirdek mantığını yöneten sınıf.
 *
 * Sorumlulukları:
 *  - Tüccarın talep ettiği güncel eşya listesini tutmak
 *  - Eşyaların GLOBAL (sunucu geneli) stoğunu yönetmek
 *  - Yenilenme (refresh) mantığını uygulamak
 *  - Bir sonraki yenilenme zamanını takip etmek
 *
 * Thread güvenliği: Stok ve eşya listesi eşzamanlı (concurrent) yapılarla
 * tutulur; çünkü kaydetme işlemleri asenkron thread'lerden okunabilir.
 */
public class TraderManager {

    private final MossLuckyTuccar plugin;

    // Tüccarın şu an talep ettiği eşyalar (global, tüm oyuncular için ortak)
    private final List<Material> currentItems = new CopyOnWriteArrayList<>();

    // Her eşyanın kalan global stoğu
    private final Map<Material, Integer> stock = new ConcurrentHashMap<>();

    // Bir sonraki yenilenme anının epoch (millis) değeri
    private volatile long nextRefresh;

    public TraderManager(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    /**
     * Eklenti açılışında çağrılır. Kaydedilmiş veri yoksa veya
     * yenilenme zamanı geçmişse yeni bir döngü başlatır.
     */
    public void initialize() {
        // Hiç eşya yoksa (ilk kurulum) ya da yenilenme zamanı geçmişse yenile
        if (currentItems.isEmpty() || System.currentTimeMillis() >= nextRefresh || nextRefresh <= 0) {
            refresh();
        }
    }

    /**
     * Tüccarı yeniler: havuzdan yeni eşyalar seçer, stokları sıfırlar
     * ve bir sonraki yenilenme zamanını hesaplar.
     */
    public synchronized boolean refresh() {
        ConfigManager config = plugin.getConfigManager();
        List<Material> pool = new ArrayList<>(config.getItemsPool());

        // Havuz boşsa yeni eşya seçilemez. Yine de bir sonraki yenilenme zamanını
        // ayarlıyoruz; aksi halde zamanlayıcı her dakika boşuna deneyip
        // log/duyuru spam'i üretir.
        if (pool.isEmpty()) {
            this.nextRefresh = TimeUtil.calculateNextRefresh(config.getRefreshHour());
            plugin.getLogger().warning("items-pool boş! Tüccar yenilenemedi. "
                    + "Lütfen config.yml içindeki 'items-pool' listesini doldurun.");
            return false;
        }

        // Talep edilecek eşya sayısını belirle
        int count = config.getTraderItemsCount();
        if (count <= 0 || count > pool.size()) {
            count = pool.size();
        }

        // Havuzu karıştır ve ilk 'count' eşyayı seç
        Collections.shuffle(pool, ThreadLocalRandom.current());
        List<Material> selected = new ArrayList<>(pool.subList(0, count));

        // Yeni durumu uygula
        currentItems.clear();
        stock.clear();
        int defaultStock = config.getDefaultStock();
        for (Material material : selected) {
            currentItems.add(material);
            stock.put(material, defaultStock);
        }

        // Bir sonraki yenilenme zamanını ayarla
        this.nextRefresh = TimeUtil.calculateNextRefresh(config.getRefreshHour());

        plugin.getLogger().info("Şanslı Tüccar yenilendi. Talep edilen eşya sayısı: " + currentItems.size());

        // Yeni durumu kalıcı olarak (asenkron) kaydet
        plugin.getDataManager().saveAsync(this);
        return true;
    }

    /**
     * Tüccarın belirtilen eşyayı talep edip etmediğini kontrol eder.
     */
    public boolean isWanted(Material material) {
        return currentItems.contains(material);
    }

    /**
     * Belirtilen eşyanın kalan global stoğunu döndürür.
     */
    public int getStock(Material material) {
        return stock.getOrDefault(material, 0);
    }

    /**
     * Belirtilen eşyanın stoğundan verilen miktarı düşmeye çalışır.
     * Asıl düşülen miktarı döndürür (stok yetersizse daha az olabilir).
     *
     * Eşzamanlı satışlarda (race condition) tutarlılık için atomik çalışır.
     *
     * @param material satılan eşya
     * @param requested talep edilen düşüş miktarı
     * @return gerçekte stoktan düşülen (yani satılabilen) miktar
     */
    public synchronized int decreaseStock(Material material, int requested) {
        int available = stock.getOrDefault(material, 0);
        if (available <= 0 || requested <= 0) {
            return 0;
        }

        int toSell = Math.min(available, requested);
        stock.put(material, available - toSell);
        return toSell;
    }

    /**
     * Başarısız bir işlemden sonra (örn. ödeme yapılamadıysa) düşülen stoğu
     * geri ekler. Yalnızca o eşya hâlâ talep ediliyorsa stoğu eski haline getirir.
     */
    public synchronized void restoreStock(Material material, int amount) {
        if (amount <= 0) {
            return;
        }
        stock.put(material, getStock(material) + amount);
    }

    // --- Yenilenme zamanı erişimcileri ---

    public long getNextRefresh() {
        return nextRefresh;
    }

    public void setNextRefresh(long nextRefresh) {
        this.nextRefresh = nextRefresh;
    }

    /**
     * Bir sonraki yenilenmenin gelip gelmediğini kontrol eder.
     * Scheduler tarafından düzenli olarak çağrılır.
     */
    public boolean shouldRefreshNow() {
        return nextRefresh <= 0 || System.currentTimeMillis() >= nextRefresh;
    }

    // --- Çalışma anı listesi erişimcileri ---

    public List<Material> getCurrentItems() {
        return currentItems;
    }

    /**
     * Kalan sürenin biçimlendirilmiş halini ("XX Saat XX Dakika") döndürür.
     */
    public String getFormattedRemaining() {
        return TimeUtil.formatRemaining(nextRefresh);
    }

    /**
     * Yenilenmeye kalan tam saat sayısı (info menüsündeki {hours} için).
     */
    public long getRemainingHours() {
        return TimeUtil.remainingHours(nextRefresh);
    }

    /**
     * Yenilenmeye kalan, saatten artan dakika sayısı (info menüsündeki {minutes} için).
     */
    public long getRemainingMinutes() {
        return TimeUtil.remainingMinutes(nextRefresh);
    }

    // --- DataManager tarafından kullanılan ham (raw) erişimciler ---

    public void setCurrentItemsRaw(List<Material> items) {
        currentItems.clear();
        currentItems.addAll(items);
    }

    public void setStockRaw(Material material, int amount) {
        stock.put(material, amount);
    }

    /**
     * Stok haritasının anlık (kaydetme amaçlı) kopyasını döndürür.
     */
    public Map<Material, Integer> getStockSnapshot() {
        return new HashMap<>(stock);
    }
}
