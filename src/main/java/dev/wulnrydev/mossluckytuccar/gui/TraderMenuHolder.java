package dev.wulnrydev.mossluckytuccar.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Şanslı Tüccar menüsüne ait özel InventoryHolder.
 *
 * Bu sınıf sayesinde tıklama olaylarında menünün bizim eklentimize ait
 * olduğunu güvenli şekilde anlarız (başlık karşılaştırması gibi kırılgan
 * yöntemlere gerek kalmaz) ve hangi slotun hangi eşyaya karşılık geldiğini
 * (slot -> Material eşlemesi) burada saklarız.
 */
public class TraderMenuHolder implements InventoryHolder {

    private final Player owner;
    // Slot numarası -> o slottaki satılabilir eşya
    private final Map<Integer, Material> slotItems = new HashMap<>();
    private Inventory inventory;
    // "Ana Menüye Dön" butonunun slotu (-1 = buton yok)
    private int backSlot = -1;

    public TraderMenuHolder(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Bir slota satılabilir eşya bağlar.
     */
    public void mapSlot(int slot, Material material) {
        slotItems.put(slot, material);
    }

    /**
     * Belirtilen slottaki satılabilir eşyayı döndürür (yoksa null).
     */
    public Material getMaterialAt(int slot) {
        return slotItems.get(slot);
    }

    /**
     * "Ana Menüye Dön" butonunun bulunduğu slotu ayarlar (-1 = buton yok).
     */
    public void setBackSlot(int backSlot) {
        this.backSlot = backSlot;
    }

    public int getBackSlot() {
        return backSlot;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
