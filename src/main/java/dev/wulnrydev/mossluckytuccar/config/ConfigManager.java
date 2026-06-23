package dev.wulnrydev.mossluckytuccar.config;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * config.yml dosyasını yöneten ve değerleri önbelleğe (cache) alan sınıf.
 * Ayarlar bir kez okunup bellekte tutulur; böylece her erişimde
 * disk/dosya okuması yapılmaz (performans için kritik).
 */
public class ConfigManager {

    private final MossLuckyTuccar plugin;

    // --- Önbelleğe alınan ayarlar ---
    private int refreshHour;
    private double multiplier;
    private int defaultStock;
    private int traderItemsCount;
    private List<Material> itemsPool;

    // Menü ayarları
    private String menuTitle;
    private int menuRows;
    private List<Integer> itemSlots;

    private boolean fillerEnabled;
    private Material fillerMaterial;
    private String fillerName;

    private boolean infoItemEnabled;
    private int infoItemSlot;
    private Material infoItemMaterial;
    private String infoItemName;
    private List<String> infoItemLore;

    private String itemDisplayName;
    private List<String> itemDisplayLore;

    // "Ana Menüye Dön" butonu ayarları
    private boolean backButtonEnabled;
    private int backButtonSlot;
    private Material backButtonMaterial;
    private String backButtonName;
    private List<String> backButtonLore;
    private String backButtonCommand;

    public ConfigManager(MossLuckyTuccar plugin) {
        this.plugin = plugin;
        load();
    }

    /**
     * config.yml dosyasını diskten okur ve tüm değerleri önbelleğe alır.
     * /reload komutunda tekrar çağrılır.
     */
    public void load() {
        // Varsayılan config.yml yoksa oluştur, varsa diskten yükle
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // --- Genel ayarlar ---
        this.refreshHour = clampHour(config.getInt("settings.refresh-hour", 18));
        // Çarpan 0 veya negatif olamaz; aksi halde oyuncu eşyasını bedavaya kaptırırdı.
        double rawMultiplier = config.getDouble("settings.multiplier", 2.0);
        if (rawMultiplier <= 0) {
            plugin.getLogger().warning("config.yml -> settings.multiplier 0'dan büyük olmalı! "
                    + "Geçersiz değer (" + rawMultiplier + ") yerine 2.0 kullanılıyor.");
            rawMultiplier = 2.0;
        }
        this.multiplier = rawMultiplier;
        this.defaultStock = Math.max(0, config.getInt("settings.default-stock", 10000));
        this.traderItemsCount = config.getInt("settings.trader-items-count", 9);

        // --- Eşya havuzu (geçersiz materyalleri atla) ---
        this.itemsPool = new ArrayList<>();
        for (String raw : config.getStringList("items-pool")) {
            Material material = Material.matchMaterial(raw.toUpperCase());
            if (material != null && material.isItem()) {
                itemsPool.add(material);
            } else {
                plugin.getLogger().log(Level.WARNING,
                        "config.yml -> items-pool: Geçersiz materyal atlandı: " + raw);
            }
        }

        // --- Menü ayarları ---
        this.menuTitle = config.getString("menu.title", "&#15FF08Şanslı Tüccar");
        this.menuRows = Math.max(1, Math.min(6, config.getInt("menu.rows", 6)));
        this.itemSlots = config.getIntegerList("menu.item-slots");

        this.fillerEnabled = config.getBoolean("menu.filler.enabled", true);
        this.fillerMaterial = parseMaterial(config.getString("menu.filler.material"), Material.GRAY_STAINED_GLASS_PANE);
        this.fillerName = config.getString("menu.filler.name", " ");

        this.infoItemEnabled = config.getBoolean("menu.info-item.enabled", true);
        this.infoItemSlot = config.getInt("menu.info-item.slot", 4);
        this.infoItemMaterial = parseMaterial(config.getString("menu.info-item.material"), Material.CLOCK);
        this.infoItemName = config.getString("menu.info-item.name", "&#00D420Bilgi");
        this.infoItemLore = config.getStringList("menu.info-item.lore");

        this.itemDisplayName = config.getString("menu.item-display.name", "&#00D420{item}");
        this.itemDisplayLore = config.getStringList("menu.item-display.lore");

        // --- "Ana Menüye Dön" butonu ---
        this.backButtonEnabled = config.getBoolean("menu.back-button.enabled", true);
        this.backButtonSlot = config.getInt("menu.back-button.slot", 31);
        this.backButtonMaterial = parseMaterial(config.getString("menu.back-button.material"), Material.ARROW);
        this.backButtonName = config.getString("menu.back-button.name", "&#ffd500◀ Ana Menü");
        this.backButtonLore = config.getStringList("menu.back-button.lore");
        this.backButtonCommand = config.getString("menu.back-button.command", "menü");
    }

    // Saat değerini 0-23 aralığına sabitler
    private int clampHour(int hour) {
        return Math.max(0, Math.min(23, hour));
    }

    // Materyal adını güvenli şekilde çözer; geçersizse varsayılanı döndürür
    private Material parseMaterial(String name, Material fallback) {
        if (name == null) {
            return fallback;
        }
        Material material = Material.matchMaterial(name.toUpperCase());
        return material != null ? material : fallback;
    }

    // --- Getter metotları (önbellekten okur) ---

    public int getRefreshHour() {
        return refreshHour;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public int getDefaultStock() {
        return defaultStock;
    }

    public int getTraderItemsCount() {
        return traderItemsCount;
    }

    public List<Material> getItemsPool() {
        return itemsPool;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public int getMenuRows() {
        return menuRows;
    }

    public int getMenuSize() {
        return menuRows * 9;
    }

    public List<Integer> getItemSlots() {
        return itemSlots;
    }

    public boolean isFillerEnabled() {
        return fillerEnabled;
    }

    public Material getFillerMaterial() {
        return fillerMaterial;
    }

    public String getFillerName() {
        return fillerName;
    }

    public boolean isInfoItemEnabled() {
        return infoItemEnabled;
    }

    public int getInfoItemSlot() {
        return infoItemSlot;
    }

    public Material getInfoItemMaterial() {
        return infoItemMaterial;
    }

    public String getInfoItemName() {
        return infoItemName;
    }

    public List<String> getInfoItemLore() {
        return infoItemLore;
    }

    public String getItemDisplayName() {
        return itemDisplayName;
    }

    public List<String> getItemDisplayLore() {
        return itemDisplayLore;
    }

    public boolean isBackButtonEnabled() {
        return backButtonEnabled;
    }

    public int getBackButtonSlot() {
        return backButtonSlot;
    }

    public Material getBackButtonMaterial() {
        return backButtonMaterial;
    }

    public String getBackButtonName() {
        return backButtonName;
    }

    public List<String> getBackButtonLore() {
        return backButtonLore;
    }

    public String getBackButtonCommand() {
        return backButtonCommand;
    }
}
