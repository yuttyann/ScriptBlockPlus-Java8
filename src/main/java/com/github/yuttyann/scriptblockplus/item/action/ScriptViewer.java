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
package com.github.yuttyann.scriptblockplus.item.action;

import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.bridge.nms.GlowEntity;
import com.github.yuttyann.scriptblockplus.enums.Permission;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.item.ChangeSlot;
import com.github.yuttyann.scriptblockplus.item.ItemAction;
import com.github.yuttyann.scriptblockplus.item.RunItem;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.utils.ItemUtils;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * ScriptBlockPlus ScriptViewer クラス
 * @author yuttyann44581
 */
public final class ScriptViewer extends ItemAction {

    public static final Set<SBPlayer> PLAYERS = new HashSet<>();

    static {
        ScriptBlock.getScheduler().run(new TickRunnable(), 0L, 1L);
    }

    public ScriptViewer() {
        super(ItemUtils.getScriptViewer());
    }

    @Override
    public void run(@NotNull RunItem runItem) {
        SBPlayer sbPlayer = SBPlayer.fromPlayer(runItem.getPlayer());
        switch (runItem.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                PLAYERS.add(sbPlayer);
                SBConfig.SCRIPT_VIEWER_START.send(sbPlayer);
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                PLAYERS.remove(sbPlayer);
                GlowEntity.DEFAULT.destroyAll(sbPlayer);
                SBConfig.SCRIPT_VIEWER_STOP.send(sbPlayer);
                break;
            default:
        }
    }

    @Override
    public void slot(@NotNull ChangeSlot changeSlot) { }

    @Override
    public boolean hasPermission(@NotNull Permissible permissible) {
        return Permission.TOOL_SCRIPT_VIEWER.has(permissible);
    }
}