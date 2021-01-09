package com.github.yuttyann.scriptblockplus.utils;

import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

/**
 * ScriptBlockPlus ItemUtils クラス
 * 
 * @author yuttyann44581
 */
public class ItemUtils {

    private static final String MINECRAFT = "minecraft:";
    private static final Map<String, Material> KEY_MATERIALS;

    static {
        if (Utils.isCBXXXorLater("1.13")) {
            KEY_MATERIALS = null;
        } else {
            KEY_MATERIALS = new HashMap<>();
            if (PackageType.HAS_NMS) {
                try {
                    KEY_MATERIALS.putAll(PackageType.getItemRegistry());
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @NotNull
    public static Material getMaterial(@NotNull String name) {
        if (KEY_MATERIALS != null) {
            Material material = KEY_MATERIALS.get(name.startsWith(MINECRAFT) ? name : MINECRAFT + name);
            if (material != null) {
                return material;
            }
        }
        name = removeMinecraftKey(name);
        name = name.toUpperCase(Locale.ENGLISH);
        name = name.replaceAll("\\s+", "_").replaceAll("\\W", "");
        Material material = Material.getMaterial(name);
        return material == null ? Material.AIR : material;
    }

    @NotNull
    public static ItemStack getBlockSelector() {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dBlock Selector");
        meta.setLore(StringUtils.setListColor(SBConfig.BLOCK_SELECTOR.getValue()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public static ItemStack getScriptEditor() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dScript Editor");
        meta.setLore(StringUtils.setListColor(SBConfig.SCRIPT_EDITOR.getValue()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public static ItemStack getScriptViewer() {
        ItemStack item = new ItemStack(getMaterial(Utils.isCBXXXorLater("1.13") ? "CLOCK" : "WATCH"));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§dScript Viewer");
        meta.setLore(StringUtils.setListColor(SBConfig.SCRIPT_VIEWER.getValue()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @NotNull
    public static String getKey(@NotNull Material material) {
        if (KEY_MATERIALS == null) {
            return material.getKey().toString();
        }
        Predicate<Entry<?, ?>> filter = (e) -> e.getValue() == material;
        return KEY_MATERIALS.entrySet().stream().filter(filter).findFirst().get().getKey();
    }

    @NotNull
    public static String removeMinecraftKey(@NotNull String name) {
        return StringUtils.removeStart(name, MINECRAFT);
    }

    @NotNull
    public static String getName(@NotNull ItemStack item, @Nullable String def) {
        def = def == null ? item.getType().name() : def;
        if (item.getType() == Material.AIR) {
            return def;
        }
        ItemMeta meta = item.getItemMeta();
        return meta == null ? def : meta.hasDisplayName() ? meta.getDisplayName() : def;
    }

    @NotNull
    public static String getName(@NotNull ItemStack item) {
        return getName(item, item.getType().name());
    }

    @SuppressWarnings("deprecation")
    public static int getDamage(@NotNull ItemStack item) {
        if (Utils.isCBXXXorLater("1.13")) {
            ItemMeta meta = item.getItemMeta();
            return meta == null ? 0 : ((Damageable) meta).getDamage();
        }
        return item.getDurability();
    }

    public static boolean isItem(@Nullable ItemStack item, @Nullable Material material, @NotNull String name) {
        return isItem(item, material, name::equals);
    }

    public static boolean isItem(@Nullable ItemStack item, @Nullable Material material, @NotNull Predicate<String> name) {
        return item != null && material != null && item.getType() == material && name.test(getName(item));
    }
}