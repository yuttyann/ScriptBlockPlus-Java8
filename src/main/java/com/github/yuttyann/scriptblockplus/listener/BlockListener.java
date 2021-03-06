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
package com.github.yuttyann.scriptblockplus.listener;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.enums.splittype.Filter;
import com.github.yuttyann.scriptblockplus.file.json.derived.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.element.BlockScript;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import com.github.yuttyann.scriptblockplus.script.ScriptRead;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.selector.CommandSelector;
import com.github.yuttyann.scriptblockplus.selector.Split;
import com.github.yuttyann.scriptblockplus.selector.SplitValue;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus BlockListener クラス
 * @author yuttyann44581
 */
public final class BlockListener implements Listener {

    private static final Set<BlockCoords> REDSTONE_FLAG = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();
        BlockCoords blockCoords = BlockCoords.of(block);
        if (!block.isBlockIndirectlyPowered()) {
            REDSTONE_FLAG.remove(blockCoords);
            return;
        }
        if (REDSTONE_FLAG.contains(blockCoords)) {
            return;
        }
        for (ScriptKey scriptKey : ScriptKey.iterable()) {
            BlockScriptJson scriptJson = BlockScriptJson.get(scriptKey);
            if (scriptJson.isEmpty()) {
                continue;
            }
            BlockScript blockScript = scriptJson.fastLoad(blockCoords);
            if (blockScript == null) {
                continue;
            }
            String selector = blockScript.getSelector();
            if (StringUtils.isEmpty(selector) || !CommandSelector.has(selector)) {
                continue;
            }
            AtomicInteger index = new AtomicInteger();
            Split filterSplit = new Split(selector, "filter", "{", "}");
            SplitValue[] filterValues = filterSplit.getValues(Filter.values());
            Split selectorSplit = new Split(selector, "@", "[", "]", filterSplit.toString().length());
            for (Entity target : CommandSelector.getTargets(Bukkit.getConsoleSender(), blockCoords.toLocation(), selectorSplit.toString())) {
                if (!(target instanceof Player)) {
                    continue;
                }
                Player player = (Player) target;
                if (!StreamUtils.allMatch(filterValues, s -> has(s, player, index.get()))) {
                    continue;
                }
                index.incrementAndGet();
                REDSTONE_FLAG.add(blockCoords);
                new ScriptRead(player, blockCoords, scriptKey).read(0);
            }
        }
    }

    private boolean has(@NotNull SplitValue splitValue, @NotNull Player player, int index) {
        String value = splitValue.getValue();
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        switch ((Filter) splitValue.getType()) {
            case OP:
                return Boolean.parseBoolean(value) ? player.isOp() : !player.isOp();
            case PERM:
                return player.hasPermission(value);
            case LIMIT:
                return index < Integer.parseInt(value);
            default:
                return false;
        }
    }
}