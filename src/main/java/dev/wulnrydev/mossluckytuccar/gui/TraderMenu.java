package dev.wulnrydev.mossluckytuccar.gui;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.config.ConfigManager;
import dev.wulnrydev.mossluckytuccar.manager.TradeService;
import dev.wulnrydev.mossluckytuccar.manager.TraderManager;
import dev.wulnrydev.mossluckytuccar.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Şanslı Tüccar GUI (menü) sistemini oluşturan sınıf.
 * Menü tamamen config.yml üzerinden özelleştirilebilir.
 * Eşyalar, güncel fiyatları (ShopGUI+ x çarpan) ve kalan stoklarıyla gösterilir.
 */
public class TraderMenu {

    private final MossLuckyTuccar plugin;

    public TraderMenu(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    /**
     * Belirtilen oyuncuya Şanslı Tüccar menüsünü oluşturup açar.
     *
     * @param player menüyü açacak oyuncu
     */
    public void open(Player player) {
        ConfigManager config = plugin.getConfigManager();
        TraderManager trader = plugin.getTraderManager();

        // Menü taşıyıcısını ve envanterini oluştur
        TraderMenuHolder holder = new TraderMenuHolder(player);
        Inventory inventory = Bukkit.createInventory(holder, config.getMenuSize(),
                ColorUtil.colorize(config.getMenuTitle()));
        holder.setInventory(inventory);

        // 1) Boş alanları cam (filler) ile doldur
        if (config.isFillerEnabled()) {
            ItemStack filler = createFiller(config);
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                inventory.setItem(slot, filler);
            }
        }

        // 2) Bilgi (yenilenme süresi) eşyasını yerleştir.
        //    Slot, envanterin geçerli aralığında olmalı (negatif/taşan değerler atlanır),
        //    aksi halde setItem çağrısı hata fırlatır ve menü açılmazdı.
        int infoSlot = config.getInfoItemSlot();
        if (config.isInfoItemEnabled() && infoSlot >= 0 && infoSlot < inventory.getSize()) {
            inventory.setItem(infoSlot, createInfoItem(config, trader));
        }

        // 3) Talep edilen eşyaları slotlara yerleştir
        List<Material> items = trader.getCurrentItems();
        List<Integer> slots = config.getItemSlots();
        int placed = 0;
        for (Material material : items) {
            if (placed >= slots.size()) {
                break; // Tanımlı slottan fazla eşya varsa kalanını gösterme
            }
            int slot = slots.get(placed);
            if (slot < 0 || slot >= inventory.getSize()) {
                placed++;
                continue;
            }

            inventory.setItem(slot, createDisplayItem(config, trader, player, material));
            holder.mapSlot(slot, material); // Slot -> eşya eşlemesini kaydet
            placed++;
        }

        // 4) "Ana Menüye Dön" butonunu yerleştir (menünün en altı)
        if (config.isBackButtonEnabled()) {
            int backSlot = config.getBackButtonSlot();
            if (backSlot >= 0 && backSlot < inventory.getSize()) {
                inventory.setItem(backSlot, createBackButton(config));
                holder.setBackSlot(backSlot); // Dinleyici bu slotu tanısın diye kaydet
            }
        }

        player.openInventory(inventory);
    }

    /**
     * Boşlukları dolduran cam eşyasını oluşturur.
     */
    private ItemStack createFiller(ConfigManager config) {
        ItemStack item = new ItemStack(config.getFillerMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(config.getFillerName()));
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * "Ana Menüye Dön" butonunu (ok) oluşturur.
     */
    private ItemStack createBackButton(ConfigManager config) {
        ItemStack item = new ItemStack(config.getBackButtonMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(config.getBackButtonName()));

            List<String> lore = new ArrayList<>();
            for (String line : config.getBackButtonLore()) {
                lore.add(ColorUtil.colorize(line));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Yenilenme süresini gösteren bilgi eşyasını oluşturur.
     */
    private ItemStack createInfoItem(ConfigManager config, TraderManager trader) {
        ItemStack item = new ItemStack(config.getInfoItemMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtil.colorize(config.getInfoItemName()));

            // Lore içindeki {time} ve {refresh-hour} değişkenlerini doldur
            List<String> lore = new ArrayList<>();
            String remaining = trader.getFormattedRemaining();
            String refreshHour = String.format("%02d", config.getRefreshHour());
            String hours = String.valueOf(trader.getRemainingHours());
            String minutes = String.valueOf(trader.getRemainingMinutes());
            for (String line : config.getInfoItemLore()) {
                lore.add(ColorUtil.colorize(line
                        .replace("{time}", remaining)
                        .replace("{hours}", hours)
                        .replace("{minutes}", minutes)
                        .replace("{refresh-hour}", refreshHour)));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Satılabilir bir eşyanın menü görünümünü (fiyat + stok ile) oluşturur.
     */
    private ItemStack createDisplayItem(ConfigManager config, TraderManager trader,
                                        Player player, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        // Güncel birim fiyatı hesapla: ShopGUI+ fiyatı x çarpan
        double basePrice = plugin.getShopHook().getSellPrice(player, material);
        double multiplier = config.getMultiplier();
        String priceText = basePrice > 0
                ? TradeService.formatPrice(basePrice * multiplier)
                : "Yok";

        int stock = trader.getStock(material);

        // İsim (materyal adı okunabilir biçime ortak yardımcıyla çevrilir)
        meta.setDisplayName(ColorUtil.colorize(config.getItemDisplayName()
                .replace("{item}", TradeService.formatMaterialName(material))));

        // Lore içindeki değişkenleri doldur
        List<String> lore = new ArrayList<>();
        for (String line : config.getItemDisplayLore()) {
            lore.add(ColorUtil.colorize(line
                    .replace("{price}", priceText)
                    .replace("{stock}", String.valueOf(stock))));
        }
        meta.setLore(lore);

        // Olası ek bilgileri (enchant, attribute) gizle - temiz görünüm için
        meta.addItemFlags(ItemFlag.values());

        item.setItemMeta(meta);
        return item;
    }
}
