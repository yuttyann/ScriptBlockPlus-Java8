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
package com.github.yuttyann.scriptblockplus.script.endprocess;

import com.github.yuttyann.scriptblockplus.player.ObjectMap;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.SBRead;
import com.github.yuttyann.scriptblockplus.script.option.other.ItemCost;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus EndInventory エンドプロセスクラス
 * @author yuttyann44581
 */
public class EndInventory implements EndProcess {

    public static final ItemStack[] EMPTY_ARRAY = new ItemStack[0];

    @Override
    @NotNull
    public EndProcess newInstance() {
        return new EndInventory();
    }

    @Override
    public void success(@NotNull SBRead sbRead) {
        SBPlayer sbPlayer = sbRead.getSBPlayer();
        StreamUtils.ifAction(sbPlayer.isOnline(), () -> Utils.updateInventory(sbPlayer.getPlayer()));
    }

    @Override
    public void failed(@NotNull SBRead sbRead) {
        ItemStack[] items = sbRead.get(ItemCost.KEY_OPTION, EMPTY_ARRAY);
        if (items.length > 0) {
            SBPlayer sbPlayer = sbRead.getSBPlayer();
            if (sbPlayer.isOnline()) {
                try {
                    sbPlayer.getInventory().setContents(items);
                } finally {
                    Utils.updateInventory(sbPlayer.getPlayer());
                }
            } else {
                ObjectMap objectMap = sbRead.getSBPlayer().getObjectMap();
                if (!objectMap.has(ItemCost.KEY_PLAYER)) {
                    objectMap.put(ItemCost.KEY_PLAYER, items);
                }
            }
        }
    }
}