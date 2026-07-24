package dev.wulnrydev.mossluckytuccar.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * Bukkit {@link Material} adlarını doğrudan Türkçe karşılıklarına çeviren
 * yardımcı sınıf. Böylece menüde ve mesajlarda eşyalar İngilizce yerine
 * Türkçe görünür; ekstra bir özelleştirme (config) gerekmez.
 *
 * <p>Sözlükte bulunmayan bir materyal için, adı okunabilir biçime çevrilerek
 * (DIAMOND_SWORD -> Diamond Sword) döndürülür; böylece hiçbir eşya boş kalmaz.
 */
public final class MaterialLang {

    // Materyal -> Türkçe ad eşlemesi
    private static final Map<Material, String> NAMES = new HashMap<>();

    private MaterialLang() {
    }

    /**
     * Verilen materyalin Türkçe adını döndürür.
     * Sözlükte yoksa okunabilir İngilizce ada geri düşer.
     */
    public static String translate(Material material) {
        String name = NAMES.get(material);
        if (name != null) {
            return name;
        }
        return prettify(material);
    }

    /**
     * Materyal adını okunabilir biçime çevirir (DIAMOND_SWORD -> Diamond Sword).
     * Sözlükte karşılığı olmayan eşyalar için yedek (fallback) olarak kullanılır.
     */
    public static String prettify(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(part.charAt(0)))
                   .append(part.substring(1))
                   .append(' ');
        }
        return builder.toString().trim();
    }

    // Materyal ismini güvenli biçimde ekler (sürümde yoksa yok sayılır)
    private static void put(String material, String turkish) {
        Material mat = Material.matchMaterial(material);
        if (mat != null) {
            NAMES.put(mat, turkish);
        }
    }

    static {
        // --- Bitkiler / Yiyecek / Tarım ---
        put("POTATO", "Patates");
        put("BAKED_POTATO", "Fırında Patates");
        put("POISONOUS_POTATO", "Zehirli Patates");
        put("CARROT", "Havuç");
        put("GOLDEN_CARROT", "Altın Havuç");
        put("WHEAT", "Buğday");
        put("WHEAT_SEEDS", "Buğday Tohumu");
        put("BREAD", "Ekmek");
        put("BEETROOT", "Pancar");
        put("BEETROOT_SEEDS", "Pancar Tohumu");
        put("BEETROOT_SOUP", "Pancar Çorbası");
        put("PUMPKIN", "Balkabağı");
        put("CARVED_PUMPKIN", "Oyulmuş Balkabağı");
        put("PUMPKIN_PIE", "Balkabağı Turtası");
        put("PUMPKIN_SEEDS", "Balkabağı Tohumu");
        put("MELON", "Karpuz");
        put("MELON_SLICE", "Karpuz Dilimi");
        put("MELON_SEEDS", "Karpuz Tohumu");
        put("SUGAR_CANE", "Şeker Kamışı");
        put("SUGAR", "Şeker");
        put("COCOA_BEANS", "Kakao Çekirdeği");
        put("NETHER_WART", "Nether Mantarı");
        put("APPLE", "Elma");
        put("GOLDEN_APPLE", "Altın Elma");
        put("ENCHANTED_GOLDEN_APPLE", "Büyülü Altın Elma");
        put("SWEET_BERRIES", "Tatlı Yemişler");
        put("GLOW_BERRIES", "Işıltılı Yemişler");
        put("CHORUS_FRUIT", "Koro Meyvesi");
        put("EGG", "Yumurta");
        put("MILK_BUCKET", "Süt Kovası");
        put("HONEY_BOTTLE", "Bal Şişesi");
        put("HONEYCOMB", "Bal Peteği");
        put("BROWN_MUSHROOM", "Kahverengi Mantar");
        put("RED_MUSHROOM", "Kırmızı Mantar");
        put("KELP", "Yosun");
        put("BAMBOO", "Bambu");
        put("CACTUS", "Kaktüs");
        put("SEEDS", "Tohum");

        // --- Et / Balık ---
        put("BEEF", "Çiğ Biftek");
        put("COOKED_BEEF", "Pişmiş Biftek");
        put("PORKCHOP", "Çiğ Domuz Pirzolası");
        put("COOKED_PORKCHOP", "Pişmiş Domuz Pirzolası");
        put("CHICKEN", "Çiğ Tavuk");
        put("COOKED_CHICKEN", "Pişmiş Tavuk");
        put("MUTTON", "Çiğ Koyun Eti");
        put("COOKED_MUTTON", "Pişmiş Koyun Eti");
        put("RABBIT", "Çiğ Tavşan Eti");
        put("COOKED_RABBIT", "Pişmiş Tavşan Eti");
        put("COD", "Morina Balığı");
        put("COOKED_COD", "Pişmiş Morina Balığı");
        put("SALMON", "Somon");
        put("COOKED_SALMON", "Pişmiş Somon");
        put("TROPICAL_FISH", "Tropikal Balık");
        put("PUFFERFISH", "Balon Balığı");

        // --- Cevherler / Külçeler / Değerli ---
        put("IRON_INGOT", "Demir Külçe");
        put("IRON_NUGGET", "Demir Parçası");
        put("IRON_ORE", "Demir Cevheri");
        put("RAW_IRON", "Ham Demir");
        put("GOLD_INGOT", "Altın Külçe");
        put("GOLD_NUGGET", "Altın Parçası");
        put("GOLD_ORE", "Altın Cevheri");
        put("RAW_GOLD", "Ham Altın");
        put("COPPER_INGOT", "Bakır Külçe");
        put("COPPER_ORE", "Bakır Cevheri");
        put("RAW_COPPER", "Ham Bakır");
        put("NETHERITE_INGOT", "Netherit Külçe");
        put("NETHERITE_SCRAP", "Netherit Kalıntısı");
        put("ANCIENT_DEBRIS", "Kadim Kalıntı");
        put("DIAMOND", "Elmas");
        put("DIAMOND_ORE", "Elmas Cevheri");
        put("EMERALD", "Zümrüt");
        put("EMERALD_ORE", "Zümrüt Cevheri");
        put("COAL", "Kömür");
        put("CHARCOAL", "Odun Kömürü");
        put("COAL_ORE", "Kömür Cevheri");
        put("REDSTONE", "Kızıltaş");
        put("REDSTONE_ORE", "Kızıltaş Cevheri");
        put("LAPIS_LAZULI", "Lapis Lazuli");
        put("LAPIS_ORE", "Lapis Cevheri");
        put("QUARTZ", "Nether Kuvarsı");
        put("AMETHYST_SHARD", "Ametist Parçası");
        put("NETHER_STAR", "Nether Yıldızı");

        // --- Odun / Kütük ---
        put("OAK_LOG", "Meşe Kütüğü");
        put("SPRUCE_LOG", "Ladin Kütüğü");
        put("BIRCH_LOG", "Huş Kütüğü");
        put("JUNGLE_LOG", "Orman Kütüğü");
        put("ACACIA_LOG", "Akasya Kütüğü");
        put("DARK_OAK_LOG", "Koyu Meşe Kütüğü");
        put("MANGROVE_LOG", "Mangrov Kütüğü");
        put("CHERRY_LOG", "Kiraz Kütüğü");
        put("CRIMSON_STEM", "Kızıl Gövde");
        put("WARPED_STEM", "Çarpık Gövde");
        put("OAK_PLANKS", "Meşe Tahtası");
        put("SPRUCE_PLANKS", "Ladin Tahtası");
        put("BIRCH_PLANKS", "Huş Tahtası");
        put("STICK", "Çubuk");

        // --- Taş / Blok / İnşaat ---
        put("STONE", "Taş");
        put("COBBLESTONE", "Kaba Taş");
        put("MOSSY_COBBLESTONE", "Yosunlu Kaba Taş");
        put("DEEPSLATE", "Derinardıç");
        put("COBBLED_DEEPSLATE", "Kaba Derinardıç");
        put("GRANITE", "Granit");
        put("DIORITE", "Diyorit");
        put("ANDESITE", "Andezit");
        put("DIRT", "Toprak");
        put("GRASS_BLOCK", "Çim Bloğu");
        put("SAND", "Kum");
        put("RED_SAND", "Kırmızı Kum");
        put("GRAVEL", "Çakıl");
        put("CLAY", "Kil");
        put("CLAY_BALL", "Kil Topu");
        put("BRICK", "Tuğla");
        put("OBSIDIAN", "Obsidyen");
        put("NETHERRACK", "Nethertaşı");
        put("GLOWSTONE", "Işıltaşı");
        put("GLOWSTONE_DUST", "Işıltaşı Tozu");
        put("END_STONE", "Son Taşı");
        put("SANDSTONE", "Kumtaşı");
        put("GLASS", "Cam");
        put("ICE", "Buz");
        put("SNOWBALL", "Kartopu");
        put("SNOW_BLOCK", "Kar Bloğu");

        // --- Bloklar (değerli) ---
        put("IRON_BLOCK", "Demir Bloğu");
        put("GOLD_BLOCK", "Altın Bloğu");
        put("DIAMOND_BLOCK", "Elmas Bloğu");
        put("EMERALD_BLOCK", "Zümrüt Bloğu");
        put("COAL_BLOCK", "Kömür Bloğu");
        put("REDSTONE_BLOCK", "Kızıltaş Bloğu");
        put("LAPIS_BLOCK", "Lapis Bloğu");
        put("NETHERITE_BLOCK", "Netherit Bloğu");
        put("COPPER_BLOCK", "Bakır Bloğu");
        put("HAY_BLOCK", "Saman Balyası");

        // --- Aletler / Silahlar / Zırh (genel) ---
        put("DIAMOND_SWORD", "Elmas Kılıç");
        put("IRON_SWORD", "Demir Kılıç");
        put("NETHERITE_SWORD", "Netherit Kılıç");
        put("BOW", "Yay");
        put("ARROW", "Ok");
        put("SHIELD", "Kalkan");
        put("FISHING_ROD", "Olta");
        put("FLINT", "Çakmaktaşı");
        put("FLINT_AND_STEEL", "Çakmak");
        put("SHEARS", "Makas");
        put("BUCKET", "Kova");
        put("WATER_BUCKET", "Su Kovası");
        put("LAVA_BUCKET", "Lav Kovası");

        // --- Çeşitli / Canavar düşüşleri ---
        put("STRING", "İp");
        put("FEATHER", "Tüy");
        put("LEATHER", "Deri");
        put("BONE", "Kemik");
        put("BONE_MEAL", "Kemik Unu");
        put("GUNPOWDER", "Barut");
        put("SPIDER_EYE", "Örümcek Gözü");
        put("ROTTEN_FLESH", "Çürük Et");
        put("ENDER_PEARL", "Ender İncisi");
        put("ENDER_EYE", "Ender Gözü");
        put("BLAZE_ROD", "Alev Çubuğu");
        put("BLAZE_POWDER", "Alev Tozu");
        put("GHAST_TEAR", "Hayalet Gözyaşı");
        put("MAGMA_CREAM", "Magma Kreması");
        put("SLIME_BALL", "Balçık Topu");
        put("PHANTOM_MEMBRANE", "Hayalet Zarı");
        put("PRISMARINE_SHARD", "Prizmarin Parçası");
        put("PRISMARINE_CRYSTALS", "Prizmarin Kristali");
        put("NAUTILUS_SHELL", "Nautilus Kabuğu");
        put("SCUTE", "Kaplumbağa Kabuğu");
        put("INK_SAC", "Mürekkep Kesesi");
        put("GLOW_INK_SAC", "Işıltılı Mürekkep Kesesi");
        put("PAPER", "Kağıt");
        put("BOOK", "Kitap");
        put("TORCH", "Meşale");
        put("TNT", "TNT");
        put("FIREWORK_ROCKET", "Havai Fişek");
        put("NAME_TAG", "İsim Etiketi");
        put("SADDLE", "Eyer");
        put("EXPERIENCE_BOTTLE", "Tecrübe Şişesi");
        put("TOTEM_OF_UNDYING", "Ölümsüzlük Totemi");
    }
}
