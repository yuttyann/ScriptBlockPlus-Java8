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

import java.util.Objects;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.listener.TriggerListener;
import com.github.yuttyann.scriptblockplus.player.BaseSBPlayer;
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
        BlockCoords blockCoords = ((BaseSBPlayer) sbPlayer).getDirectOldBlockCoords();
        if (compare(sbPlayer, sbPlayer.getLocation(), blockCoords)) {
            return null;
        }
        return new Trigger(event, sbPlayer.getPlayer(), blockCoords);
    }

    private boolean compare(@NotNull SBPlayer sbPlayer, @NotNull Location location, @Nullable BlockCoords blockCoords) {
        if (blockCoords == null) {
            sbPlayer.setOldBlockCoords(BlockCoords.of(location).subtract(0, 1, 0));
            return false;
        }
        int oldX = blockCoords.getX();
        int oldY = blockCoords.getY();
        int oldZ = blockCoords.getZ();
        if (Objects.equals(location.getWorld(), blockCoords.getWorld())) {
            blockCoords.setX(location.getBlockX());
            blockCoords.setY(location.getBlockY() - 1);
            blockCoords.setZ(location.getBlockZ());
        } else {
            sbPlayer.setOldBlockCoords(blockCoords = BlockCoords.of(location).subtract(0, 1, 0));
        }
        return blockCoords.compare(oldX, oldY, oldZ);
    }
}