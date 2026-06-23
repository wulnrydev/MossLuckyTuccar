package dev.wulnrydev.mossluckytuccar.listener;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.gui.TraderMenuHolder;
import dev.wulnrydev.mossluckytuccar.manager.TradeService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Şanslı Tüccar menüsündeki tıklamaları dinleyen sınıf.
 *
 * Performans notu: Bu dinleyici yalnızca bizim menümüze ait
 * (TraderMenuHolder) tıklamalarda iş yapar; diğer tüm envanter
 * olaylarını anında görmezden gelir. Böylece gereksiz işlem yapılmaz.
 */
public class MenuListener implements Listener {

    private final MossLuckyTuccar plugin;

    public MenuListener(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Bizim menümüz değilse hiçbir şey yapma (erken çıkış)
        if (!(holder instanceof TraderMenuHolder menuHolder)) {
            return;
        }

        // Menüdeki eşyaların alınmasını/taşınmasını tamamen engelle
        event.setCancelled(true);

        // Yalnızca oyuncu üst menüye tıkladıysa devam et
        if (event.getClickedInventory() == null
                || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();

        // "Ana Menüye Dön" butonuna mı tıklandı?
        if (rawSlot == menuHolder.getBackSlot()) {
            player.closeInventory();
            String command = plugin.getConfigManager().getBackButtonCommand();
            if (command != null && !command.isBlank()) {
                // Komutu bir sonraki tick'te, oyuncu adına çalıştır (örn: /menü)
                plugin.getServer().getScheduler().runTask(plugin,
                        () -> player.performCommand(command));
            }
            return;
        }

        // Tıklanan slotta satılabilir bir eşya var mı?
        Material material = menuHolder.getMaterialAt(rawSlot);
        if (material == null) {
            return;
        }

        // Tık tipine göre satış modunu belirle
        TradeService.Mode mode = event.isRightClick()
                ? TradeService.Mode.ONE   // Sağ tık -> 1 adet
                : TradeService.Mode.ALL;  // Sol tık -> tümü

        // Satış işlemini yürüt
        plugin.getTradeService().sell(player, material, mode);

        // Menüyü bir sonraki tick'te yeniden çiz (güncel stok/fiyat görünsün).
        // Tıklama olayı işlenirken senkron envanter açmaktan kaçınmak daha güvenlidir.
        plugin.getServer().getScheduler().runTask(plugin,
                () -> plugin.getTraderMenu().open(player));
    }

    /**
     * Menü açıkken sürükleyerek (drag) eşya bırakmayı/taşımayı engeller.
     * Tıklamalar zaten iptal ediliyor; bu da sürükleme yolunu güvenceye alır.
     */
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof TraderMenuHolder) {
            event.setCancelled(true);
        }
    }
}
