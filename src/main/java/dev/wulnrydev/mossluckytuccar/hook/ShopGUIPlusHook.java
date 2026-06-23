package dev.wulnrydev.mossluckytuccar.hook;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * ShopGUI+ API ile iletişimi sağlayan kanca (hook) sınıfı.
 * Eşyaların taban satış fiyatını ShopGUI+ üzerinden çeker.
 * Hata durumlarına karşı güvenli (try/catch) bir yapı kullanır.
 */
public class ShopGUIPlusHook {

    private final MossLuckyTuccar plugin;
    private boolean enabled;

    public ShopGUIPlusHook(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    /**
     * ShopGUI+ eklentisinin sunucuda yüklü olup olmadığını kontrol eder.
     *
     * @return kanca başarıyla kurulduysa true
     */
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("ShopGUIPlus") == null) {
            this.enabled = false;
            return false;
        }
        this.enabled = true;
        return true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Verilen eşyanın oyuncuya özel taban satış fiyatını ShopGUI+ üzerinden çeker.
     * (Bazı ShopGUI+ ayarları oyuncuya/dünyaya göre fiyat değiştirebilir.)
     *
     * @param player satışı yapan oyuncu
     * @param material fiyatı sorgulanan materyal
     * @return tek bir eşyanın taban satış fiyatı; bulunamazsa -1
     */
    public double getSellPrice(Player player, Material material) {
        if (!enabled) {
            return -1;
        }

        try {
            ItemStack item = new ItemStack(material, 1);
            // ShopGUI+ API: oyuncuya göre tek birim satış fiyatını döndürür
            double price = ShopGuiPlusApi.getItemStackPriceSell(player, item);
            // Geçersiz/satılamaz eşyalarda API negatif değer döndürebilir
            return price > 0 ? price : -1;
        } catch (Throwable throwable) {
            // API sürüm farkları veya eşyanın markette olmaması durumunda
            plugin.getLogger().log(Level.FINE,
                    "ShopGUI+ fiyatı alınamadı: " + material, throwable);
            return -1;
        }
    }
}
