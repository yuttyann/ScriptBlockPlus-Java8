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
package com.github.yuttyann.scriptblockplus.listener.trigger;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.listener.TriggerListener;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;

import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus WalkTrigger クラス
 * @author yuttyann44581
 */
public class WalkTrigger extends TriggerListener<PlayerMoveEvent> {

    public WalkTrigger(@NotNull ScriptBlock plugin) {
        super(plugin, ScriptKey.WALK, EventPriority.HIGH);
    }

    @Override
    @Nullable
    public Trigger create(@NotNull PlayerMoveEvent event) {
        SBPlayer sbPlayer = SBPlayer.fromPlayer(event.getPlayer());
        BlockCoords blockCoords = new BlockCoords(sbPlayer.getLocation()).subtract(0, 1, 0);
        if (blockCoords.equals(sbPlayer.getOldBlockCoords().orElse(null))) {
            return null;
        } else {
            sbPlayer.setOldBlockCoords(blockCoords);
        }
        Location location = blockCoords.toLocation();
        return new Trigger(sbPlayer.getPlayer(), location.getBlock(), event);
    }
}