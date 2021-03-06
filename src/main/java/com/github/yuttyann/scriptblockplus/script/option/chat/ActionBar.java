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
package com.github.yuttyann.scriptblockplus.script.option.chat;

import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.enums.Permission;
import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.NMSHelper;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * ScriptBlockPlus ActionBar オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "actionbar", syntax = "@actionbar:")
public final class ActionBar extends BaseOption implements Runnable {

    private int tick, stay;
    private String message;
    private BukkitTask task;

    @Override
    protected boolean isValid() throws Exception {
        List<String> split = StringUtils.split(getOptionValue(), '/');
        this.message = StringUtils.setColor(split.get(0));
        if (split.size() > 1) {
            this.stay = Integer.parseInt(split.get(1));
            this.task = ScriptBlock.getScheduler().run(this, 0L, 20L);
        } else {
            send(getSBPlayer(), message);
        }
        return true;
    }

    @Override
    public void run() {
        try {
            if (!getSBPlayer().isOnline() || tick++ >= stay) {
                task.cancel();
            }
            send(getSBPlayer(), task.isCancelled() ? "" : message);
        } catch (Exception e) {
            task.cancel();
        }
    }

    public static void send(@NotNull SBPlayer sbPlayer, @NotNull String message) {
        Player player = sbPlayer.getPlayer();
        if (Utils.isCBXXXorLater("1.12.2")) {
            String command = "minecraft:title " + sbPlayer.getName() + " actionbar {\"text\":\"" + message + "\"}";
            Utils.tempPerm(sbPlayer, Permission.MINECRAFT_COMMAND_TITLE, () -> Bukkit.dispatchCommand(player, command));
        } else if (PackageType.HAS_NMS) {
            try {
                NMSHelper.sendActionBar(player, message);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else {
            Utils.sendColorMessage(player, "§cActionBar: §r" + message);
        }
    }
}