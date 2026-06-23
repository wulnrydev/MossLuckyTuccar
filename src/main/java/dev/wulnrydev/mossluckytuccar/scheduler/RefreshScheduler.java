package dev.wulnrydev.mossluckytuccar.scheduler;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import dev.wulnrydev.mossluckytuccar.manager.TraderManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Otomatik yenilenmeyi yöneten zamanlayıcı (scheduler).
 *
 * Her dakika, yenilenme zamanının gelip gelmediğini kontrol eder.
 * Geldiyse tüccarı yeniler ve sunucuya duyuru yapar.
 *
 * Performans notu: Saniyede bir yerine dakikada bir kontrol edilir;
 * bu, "tam saat 18:00" hassasiyeti için fazlasıyla yeterlidir ve
 * sunucuya neredeyse hiç yük bindirmez.
 */
public class RefreshScheduler {

    // 1 dakika = 20 tick/saniye * 60 saniye = 1200 tick
    private static final long CHECK_INTERVAL_TICKS = 20L * 60L;

    private final MossLuckyTuccar plugin;
    private BukkitTask task;

    public RefreshScheduler(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    /**
     * Periyodik kontrol görevini başlatır.
     */
    public void start() {
        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                TraderManager trader = plugin.getTraderManager();
                // Yenilenme zamanı geldiyse tüccarı yenile.
                // Yalnızca GERÇEKTEN yenilenebildiyse (havuz dolu) duyuru yapılır;
                // aksi halde boş havuzda her dakika spam oluşurdu.
                if (trader.shouldRefreshNow() && trader.refresh()) {
                    plugin.getServer().broadcastMessage(
                            plugin.getMessagesManager().get("trader-refreshed-broadcast"));
                }
            }
        }.runTaskTimer(plugin, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    /**
     * Görevi durdurur (eklenti kapanırken).
     */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
