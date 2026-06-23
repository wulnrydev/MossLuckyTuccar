package dev.wulnrydev.mossluckytuccar;

import dev.wulnrydev.mossluckytuccar.command.TraderCommand;
import dev.wulnrydev.mossluckytuccar.config.ConfigManager;
import dev.wulnrydev.mossluckytuccar.config.MessagesManager;
import dev.wulnrydev.mossluckytuccar.data.DataManager;
import dev.wulnrydev.mossluckytuccar.gui.TraderMenu;
import dev.wulnrydev.mossluckytuccar.hook.ShopGUIPlusHook;
import dev.wulnrydev.mossluckytuccar.hook.VaultHook;
import dev.wulnrydev.mossluckytuccar.listener.MenuListener;
import dev.wulnrydev.mossluckytuccar.manager.TradeService;
import dev.wulnrydev.mossluckytuccar.manager.TraderManager;
import dev.wulnrydev.mossluckytuccar.scheduler.RefreshScheduler;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MossLuckyTuccar - Ana eklenti sınıfı.
 * Yapımcı: wulnrydev
 *
 * Tüm yöneticileri (manager) ve kancaları (hook) başlatır,
 * komutları ve dinleyicileri kaydeder, zamanlayıcıyı çalıştırır.
 */
public final class MossLuckyTuccar extends JavaPlugin {

    // --- Yöneticiler ve kancalar ---
    private ConfigManager configManager;
    private MessagesManager messagesManager;
    private DataManager dataManager;
    private TraderManager traderManager;
    private TradeService tradeService;
    private TraderMenu traderMenu;

    private ShopGUIPlusHook shopHook;
    private VaultHook vaultHook;

    private RefreshScheduler refreshScheduler;

    @Override
    public void onEnable() {
        // 1) Yapılandırma dosyalarını yükle (config + messages)
        this.configManager = new ConfigManager(this);
        this.messagesManager = new MessagesManager(this);

        // 2) Bağımlılık kancalarını kur (ShopGUI+ ve Vault)
        this.shopHook = new ShopGUIPlusHook(this);
        if (!shopHook.setup()) {
            getLogger().severe("ShopGUIPlus bulunamadı! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.vaultHook = new VaultHook(this);
        if (!vaultHook.setup()) {
            getLogger().severe("Vault/Ekonomi sağlayıcısı bulunamadı! Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 3) Veri ve tüccar yöneticilerini başlat
        this.dataManager = new DataManager(this);
        this.traderManager = new TraderManager(this);
        // Kaydedilmiş veriyi (stok + yenilenme zamanı) yükle
        dataManager.loadInto(traderManager);
        // Gerekiyorsa ilk yenilemeyi yap
        traderManager.initialize();

        // 4) Servis ve menü sistemlerini başlat
        this.tradeService = new TradeService(this);
        this.traderMenu = new TraderMenu(this);

        // 5) Komutları ve dinleyicileri kaydet
        registerCommand();
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);

        // 6) Otomatik yenilenme zamanlayıcısını başlat
        this.refreshScheduler = new RefreshScheduler(this);
        refreshScheduler.start();

        // 7) Periyodik (asenkron) otomatik veri kaydını başlat
        dataManager.startAutoSave(traderManager);

        getLogger().info("MossLuckyTuccar başarıyla etkinleştirildi! (wulnrydev)");
    }

    @Override
    public void onDisable() {
        // Zamanlayıcıyı durdur
        if (refreshScheduler != null) {
            refreshScheduler.stop();
        }

        // Otomatik kaydetme görevini durdur ve verileri SENKRON kaydet
        // (kapanışta asenkron görev çalışmaz)
        if (dataManager != null && traderManager != null) {
            dataManager.stopAutoSave();
            dataManager.saveSync(traderManager);
        }

        getLogger().info("MossLuckyTuccar devre dışı bırakıldı.");
    }

    /**
     * /sanslituccar komutunu yürütücü ve tab-tamamlayıcı ile kaydeder.
     */
    private void registerCommand() {
        PluginCommand command = getCommand("sanslituccar");
        if (command != null) {
            TraderCommand executor = new TraderCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    // --- Getter metotları (diğer sınıfların erişimi için) ---

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessagesManager getMessagesManager() {
        return messagesManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public TraderManager getTraderManager() {
        return traderManager;
    }

    public TradeService getTradeService() {
        return tradeService;
    }

    public TraderMenu getTraderMenu() {
        return traderMenu;
    }

    public ShopGUIPlusHook getShopHook() {
        return shopHook;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }
}
