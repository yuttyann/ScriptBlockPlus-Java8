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