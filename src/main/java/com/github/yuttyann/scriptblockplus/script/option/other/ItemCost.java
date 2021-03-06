/**
 * ScriptBlockPlus - Allow you to add script to any blocks.
 * Copyright (C) 2021 yuttyann44581
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.yuttyann.scriptblockplus.script.option.other;

import java.util.List;

import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.ItemUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus ItemCost オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "itemcost", syntax = "$item:")
public final class ItemCost extends BaseOption {

    public static final String KEY_OPTION = Utils.randomUUID();
    public static final String KEY_PLAYER = Utils.randomUUID();

    @Override
    protected boolean isValid() throws Exception {
        List<String> space = StringUtils.split(getOptionValue(), ' ');
        List<String> itemId = StringUtils.split(StringUtils.removeStart(space.get(0), Utils.MINECRAFT), ':');
        if (Calculation.REALNUMBER_PATTERN.matcher(itemId.get(0)).matches()) {
            throw new IllegalAccessException("Numerical values can not be used");
        }
        Material material = ItemUtils.getMaterial(itemId.get(0));
        int damage = itemId.size() > 1 ? Integer.parseInt(itemId.get(1)) : 0;
        int amount = Integer.parseInt(space.get(1));
        String create = space.size() > 2 ? StringUtils.createString(space, 2) : null;
        String name = StringUtils.isEmpty(create) ? material.name() : StringUtils.setColor(create);

        Player player = getPlayer();
        PlayerInventory inventory = player.getInventory();
        ItemStack[] inventoryItems = inventory.getContents();
        if (!getTempMap().has(KEY_OPTION)) {
            getTempMap().put(KEY_OPTION, copyItems(inventoryItems));
        }
        int result = amount;
        for (ItemStack item : inventoryItems) {
            if (item != null && ItemUtils.getDamage(item) == damage && ItemUtils.compare(item, material, name)) {
                result -= result > 0 ? setAmount(item, item.getAmount() - result) : 0;
            }
        }
        if (result > 0) {
            SBConfig.ERROR_ITEM.replace(material, amount, damage, StringUtils.setColor(create)).send(player);
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