package dev.wulnrydev.mossluckytuccar.manager;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.util.MaterialLang;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Satış işleminin (transaction) tüm mantığını yürüten servis.
 * Stok kontrolü, fiyat hesaplama, envanterden eşya çıkarma ve
 * ödeme adımlarını tek bir yerde toplar.
 */
public class TradeService {

    /**
     * Satış modu: tek bir eşya mı yoksa envanterdeki tümü mü satılacak.
     */
    public enum Mode {
        ONE,  // 1 adet sat
        ALL   // envanterdeki tümünü sat
    }

    private final MossLuckyTuccar plugin;

    public TradeService(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    /**
     * Bir oyuncunun belirtilen eşyayı Şanslı Tüccar'a satmasını işler.
     *
     * @param player satışı yapan oyuncu
     * @param material satılacak eşya
     * @param mode satış modu (tek/tümü)
     */
    public void sell(Player player, Material material, Mode mode) {
        TraderManager trader = plugin.getTraderManager();

        // 1) Tüccar bu eşyayı talep ediyor mu?
        if (!trader.isWanted(material)) {
            plugin.getMessagesManager().send(player, "sell-not-wanted");
            return;
        }

        // 2) Oyuncunun envanterinde bu eşyadan kaç tane var?
        int inInventory = countItems(player, material);
        if (inInventory <= 0) {
            plugin.getMessagesManager().send(player, "sell-no-item");
            return;
        }

        // 3) Stok kontrolü
        if (trader.getStock(material) <= 0) {
            plugin.getMessagesManager().send(player, "sell-no-stock");
            return;
        }

        // 4) Taban satış fiyatını ShopGUI+ üzerinden çek
        double basePrice = plugin.getShopHook().getSellPrice(player, material);
        if (basePrice <= 0) {
            plugin.getMessagesManager().send(player, "sell-no-price");
            return;
        }

        // 5) Satılmak istenen miktar (mod + envanter sınırı)
        int requested = (mode == Mode.ALL) ? inInventory : 1;
        requested = Math.min(requested, inInventory);

        // 6) Stoktan düş (atomik) - gerçekte satılabilen miktarı al
        int sold = trader.decreaseStock(material, requested);
        if (sold <= 0) {
            plugin.getMessagesManager().send(player, "sell-no-stock");
            return;
        }

        // 7) Toplam kazancı hesapla: taban fiyat x çarpan x miktar
        double multiplier = plugin.getConfigManager().getMultiplier();
        double total = basePrice * multiplier * sold;

        // 8) ÖNCE ödemeyi yap. Başarısız olursa düşülen stoğu geri ver ve
        //    oyuncunun eşyalarına DOKUNMA (eşya kaybını önler).
        if (!plugin.getVaultHook().deposit(player, total)) {
            trader.restoreStock(material, sold);
            plugin.getMessagesManager().send(player, "sell-failed");
            return;
        }

        // 9) Ödeme başarılı -> envanterden satılan miktarı çıkar
        removeItems(player, material, sold);

        // 10) Başarı mesajı gönder
        plugin.getMessagesManager().send(player, "sell-success",
                "{miktar}", String.valueOf(sold),
                "{esya}", formatMaterialName(material),
                "{fiyat}", formatPrice(total));

        // 11) Değişikliği "kaydedilecek" olarak işaretle (periyodik olarak diske yazılır)
        plugin.getDataManager().requestSave();
    }

    /**
     * Oyuncunun envanterinde belirtilen materyalden toplam kaç adet olduğunu sayar.
     */
    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * Oyuncunun envanterinden belirtilen miktarda eşya çıkarır.
     */
    private void removeItems(Player player, Material material, int amount) {
        ItemStack removal = new ItemStack(material, amount);
        player.getInventory().removeItem(removal);
    }

    /**
     * Materyal adını doğrudan Türkçe karşılığına çevirir (DIAMOND -> Elmas).
     * Sözlükte karşılığı yoksa okunabilir İngilizce ada geri düşer.
     * Hem bu serviste hem de menüde (TraderMenu) ortak kullanılır.
     */
    public static String formatMaterialName(Material material) {
        return MaterialLang.translate(material);
    }

    /**
     * Fiyatı en fazla 2 ondalık basamakla, gereksiz sıfırları atarak biçimlendirir.
     */
    public static String formatPrice(double price) {
        if (price == Math.floor(price)) {
            return String.valueOf((long) price);
        }
        return String.format(java.util.Locale.US, "%.2f", price);
    }
}
