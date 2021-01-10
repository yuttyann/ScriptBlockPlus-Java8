package com.github.yuttyann.scriptblockplus.script.option.other;

import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.ItemUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus ItemCost オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "itemcost", syntax = "$item:")
public class ItemCost extends BaseOption {

    public static final String KEY_OPTION = Utils.randomUUID();
    public static final String KEY_PLAYER = Utils.randomUUID();

    @Override
    @NotNull
    public Option newInstance() {
        return new ItemCost();
    }

    @Override
    protected boolean isValid() throws Exception {
        String[] array = StringUtils.split(getOptionValue(), ' ');
        String[] param = StringUtils.split(StringUtils.removeStart(array[0], Utils.MINECRAFT), ':');
        if (Calculation.REALNUMBER_PATTERN.matcher(param[0]).matches()) {
            throw new IllegalAccessException("Numerical values can not be used");
        }
        Material material = ItemUtils.getMaterial(param[0]);
        int damage = param.length > 1 ? Integer.parseInt(param[1]) : 0;
        int amount = Integer.parseInt(array[1]);
        String create = array.length > 2 ? StringUtils.createString(array, 2) : null;
        String name = StringUtils.isEmpty(create) ? material.name() : StringUtils.setColor(create);

        Player player = getPlayer();
        Inventory inventory = player.getInventory();
        ItemStack[] inventoryItems = inventory.getContents();
        if (!getTempMap().has(KEY_OPTION)) {
            getTempMap().put(KEY_OPTION, copyItems(inventoryItems));
        }
        int result = amount;
        for (ItemStack item : inventoryItems) {
            if (item != null && ItemUtils.getDamage(item) == damage && ItemUtils.isItem(item, material, name)) {
                result -= result > 0 ? setAmount(item, item.getAmount() - result) : 0;
            }
        }
        if (result > 0) {
            SBConfig.ERROR_ITEM.replace(material, amount, damage, name).send(player);
            return false;
        }
        inventory.setContents(inventoryItems);
        return true;
    }

    private int setAmount(@NotNull ItemStack item, int amount) {
        int oldAmount = item.getAmount();
        item.setAmount(amount);
        return oldAmount;
    }

    @NotNull
    private ItemStack[] copyItems(@NotNull ItemStack[] items) {
        ItemStack[] copy = new ItemStack[items.length];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = items[i] == null ? new ItemStack(Material.AIR) : items[i].clone();
        }
        return copy;
    }
}