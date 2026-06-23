package dev.wulnrydev.mossluckytuccar.hook;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault ekonomi sistemine bağlanan kanca (hook) sınıfı.
 * Oyuncuya satış kazancını (dinar) yatırmak için kullanılır.
 */
public class VaultHook {

    private final MossLuckyTuccar plugin;
    private Economy economy;

    public VaultHook(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    /**
     * Vault üzerinden bir ekonomi sağlayıcısı (örn: EssentialsX) bulup bağlanır.
     *
     * @return ekonomi sağlayıcısı bulunduysa true
     */
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> provider =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (provider == null) {
            return false;
        }

        this.economy = provider.getProvider();
        return economy != null;
    }

    public boolean isEnabled() {
        return economy != null;
    }

    /**
     * Oyuncunun hesabına para yatırır.
     *
     * @param player parayı alacak oyuncu
     * @param amount yatırılacak miktar
     * @return işlem başarılıysa true (başarısızsa çağıran taraf geri alma yapmalı)
     */
    public boolean deposit(OfflinePlayer player, double amount) {
        if (economy == null || amount <= 0) {
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
    }
}
