package sharpmc.pl.utils.builders;

import sharpmc.pl.utils.ChatUtil;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack is;

    public ItemBuilder(Material material) {
        is = new ItemStack(material, 1);
    }

    public ItemBuilder(Material material, int amount) {
        is = new ItemStack(material, amount);
    }

    public ItemBuilder(@NonNull ItemStack itemStack) {
        this.is = new ItemStack(itemStack);
    }
    public static ItemBuilder of(@NonNull Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder of(@NonNull Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public static ItemBuilder of(@NonNull ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public ItemBuilder setName(@NonNull String name) {
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(ChatUtil.coloredHex(name));
        is.setItemMeta(im);
        return this;
    }

    public Integer getCustomModelData() {
        ItemMeta meta = is.getItemMeta();
        return meta != null ? meta.getCustomModelData() : null;
    }

    public ItemBuilder setBase64(String base64) {
        if (is.getType() != Material.PLAYER_HEAD && is.getType() != Material.PLAYER_WALL_HEAD) {
            return this;
        }

        try {
            SkullMeta skullMeta = (SkullMeta) is.getItemMeta();

            UUID id = UUID.randomUUID();

            PlayerProfile profile = Bukkit.createPlayerProfile(id, "CustomHead");
            PlayerTextures textures = profile.getTextures();

            String decodedValue = new String(Base64.getDecoder().decode(base64));

            String textureUrl = extractTextureUrl(decodedValue);

            if (textureUrl != null) {
                try {
                    URL url = new URL(textureUrl);
                    textures.setSkin(url);
                    profile.setTextures(textures);
                    skullMeta.setOwnerProfile(profile);
                    is.setItemMeta(skullMeta);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    private String extractTextureUrl(String json) {
        if (json.contains("textures")) {
            int urlIndex = json.indexOf("\"url\":\"");
            if (urlIndex != -1) {
                int startIndex = urlIndex + 7;
                int endIndex = json.indexOf("\"", startIndex);
                if (endIndex != -1) {
                    return json.substring(startIndex, endIndex);
                }
            }
        }
        return null;
    }

    public ItemBuilder storedEnchant(Enchantment enchantment, int level) {
        // Sprawdzamy, czy item jest książką
        if (is.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = is.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta enchantmentMeta = (EnchantmentStorageMeta) meta;
                enchantmentMeta.addStoredEnchant(enchantment, level, true);
                is.setItemMeta(enchantmentMeta);
            }
        }
        return this;
    }

    public Optional<Integer> getNamespaced_integer(NamespacedKey key) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(key, PersistentDataType.INTEGER)) {
                return Optional.ofNullable(container.get(key, PersistentDataType.INTEGER));
            }
        }
        return Optional.empty();
    }

    public int getAmount() {
        return is.getAmount();
    }

    public Material getMaterial() {
        return is.getType();
    }

    public Optional<Double> getNamespaced_double(NamespacedKey key) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(key, PersistentDataType.DOUBLE)) {
                return Optional.ofNullable(container.get(key, PersistentDataType.DOUBLE));
            }
        }
        return Optional.empty();
    }

    public ItemMeta getItemMeta() {
        return is.getItemMeta();
    }

    public Optional<String> getNamespaced_string(NamespacedKey key) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(key, PersistentDataType.STRING)) {
                return Optional.ofNullable(container.get(key, PersistentDataType.STRING));
            }
        }
        return Optional.empty();
    }

    public List<String> getLore() {
        if (!is.hasItemMeta() || !is.getItemMeta().hasLore()) return null;
        return is.getItemMeta().getLore();
    }
    public ItemBuilder addLore(@NonNull String line) {
        ItemMeta im = is.getItemMeta();
        List<String> currentLore = im.hasLore() ? im.getLore() : new ArrayList<>();
        currentLore.add(ChatUtil.colored(line));
        im.setLore(currentLore);
        this.is.setItemMeta(im);
        return this;
    }


    public ItemBuilder addNamespacedKey_string(NamespacedKey key, String value) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.STRING, value);
            is.setItemMeta(meta);
        }
        return this;
    }
    public ItemBuilder setItemMeta(ItemMeta itemMeta) {
        is.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder addNamespacedKey_integer(NamespacedKey key, Integer value) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.INTEGER, value);
            is.setItemMeta(meta);
        }
        return this;
    }

    public ItemBuilder addNamespacedKey_double(NamespacedKey key, double value) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.DOUBLE, value);
            is.setItemMeta(meta);
        }
        return this;
    }


    public ItemBuilder setAmount(int amount) {
        this.is.setAmount(amount);
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        ItemMeta im = this.is.getItemMeta();
        im.setUnbreakable(unbreakable);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLores(@NonNull List<String> lore) {
        ItemMeta im = this.is.getItemMeta();
        im.setLore(ChatUtil.coloredHex(lore));
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addEnchantment(@NonNull Enchantment enchant, int level) {
        this.is.addUnsafeEnchantment(enchant, level);
        return this;
    }

    public ItemBuilder addFlags(@NonNull ItemFlag... itemFlag) {
        ItemMeta itemMeta = this.is.getItemMeta();
        itemMeta.addItemFlags(itemFlag);
        this.is.setItemMeta(itemMeta);
        return this;
    }

    public ItemBuilder setSkullOwner(String name) {
        if (!(is.getItemMeta() instanceof SkullMeta)) return this;

        is.setDurability((byte) 3);
        SkullMeta meta = (SkullMeta) is.getItemMeta();
        meta.setOwner(name);
        is.setItemMeta(meta);

        return this;
    }

    public String getName() {
        if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) return null;
        return is.getItemMeta().getDisplayName();
    }

    public ItemBuilder customModelData(int data) {
        ItemMeta meta = is.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(data);
            is.setItemMeta(meta);
        }
        return this;
    }

    public ItemStack toItemStack() {
        return is;
    }

    @Override
    public ItemBuilder clone() {
        return new ItemBuilder(this.is.clone());
    }
}