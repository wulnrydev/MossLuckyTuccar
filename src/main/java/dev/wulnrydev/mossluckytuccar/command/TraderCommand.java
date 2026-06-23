package dev.wulnrydev.mossluckytuccar.command;

import dev.wulnrydev.mossluckytuccar.MossLuckyTuccar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * /sanslituccar (/st) komutunu ve alt komutlarını işleyen sınıf.
 *
 *  /sanslituccar               -> menüyü açar (yetki gerekmez)
 *  /sanslituccar admin reload  -> configleri yeniler (mossluckytuccar.admin)
 *  /sanslituccar admin forcerefresh -> stoğu hemen yeniler (mossluckytuccar.admin)
 */
public class TraderCommand implements CommandExecutor, TabCompleter {

    private static final String ADMIN_PERMISSION = "mossluckytuccar.admin";

    private final MossLuckyTuccar plugin;

    public TraderCommand(MossLuckyTuccar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Argümansız kullanım: menüyü aç
        if (args.length == 0) {
            openMenu(sender);
            return true;
        }

        // admin alt komutu
        if (args[0].equalsIgnoreCase("admin")) {
            handleAdmin(sender, args);
            return true;
        }

        // Tanınmayan kullanım: menüyü aç
        openMenu(sender);
        return true;
    }

    /**
     * Oyuncuya menüyü açar (sadece oyuncular kullanabilir).
     */
    private void openMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.getMessagesManager().send(sender, "only-player");
            return;
        }
        plugin.getTraderMenu().open(player);
    }

    /**
     * admin alt komutlarını işler (yetki kontrolü ile).
     */
    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.hasPermission(ADMIN_PERMISSION)) {
            plugin.getMessagesManager().send(sender, "admin-no-permission");
            return;
        }

        if (args.length < 2) {
            plugin.getMessagesManager().send(sender, "admin-usage");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "reload" -> {
                // Tüm config dosyalarını yeniden yükle
                plugin.getConfigManager().load();
                plugin.getMessagesManager().load();
                plugin.getMessagesManager().send(sender, "admin-reload");
            }
            case "forcerefresh" -> {
                // Eşyaları ve stoğu beklemeden hemen yenile
                plugin.getTraderManager().refresh();
                plugin.getMessagesManager().send(sender, "admin-forcerefresh");
            }
            default -> plugin.getMessagesManager().send(sender, "admin-usage");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // İlk argüman: admin yetkisi varsa "admin" öner
        if (args.length == 1 && sender.hasPermission(ADMIN_PERMISSION)) {
            return filter(Collections.singletonList("admin"), args[0]);
        }

        // İkinci argüman: admin alt komutları
        if (args.length == 2 && args[0].equalsIgnoreCase("admin")
                && sender.hasPermission(ADMIN_PERMISSION)) {
            return filter(Arrays.asList("reload", "forcerefresh"), args[1]);
        }

        return Collections.emptyList();
    }

    // Tab tamamlamada yazılana göre filtreleme yapar
    private List<String> filter(List<String> options, String input) {
        List<String> result = new ArrayList<>();
        String lower = input.toLowerCase();
        for (String option : options) {
            if (option.toLowerCase().startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
