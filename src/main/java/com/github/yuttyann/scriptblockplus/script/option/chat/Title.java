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

import com.github.yuttyann.scriptblockplus.enums.Permission;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus Title オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "title", syntax = "@title:")
public final class Title extends BaseOption {

    @Override
    protected boolean isValid() throws Exception {
        String[] array = StringUtils.split(getOptionValue(), '/');
        String title = StringUtils.setColor(array[0] + "");
        String subtitle = StringUtils.setColor(array.length > 1 ? array[1] : "");
        int fadeIn = 10, stay = 40, fadeOut = 10;
        if (array.length == 3) {
            String[] times = StringUtils.split(array[2], '-');
            if (times.length == 3) {
                fadeIn = Integer.parseInt(times[0]);
                stay = Integer.parseInt(times[1]);
                fadeOut = Integer.parseInt(times[2]);
            }
        }
        send(getSBPlayer(), title, subtitle, fadeIn, stay, fadeOut);
        return true;
    }

    public static void send(@NotNull SBPlayer sbPlayer, @Nullable String title, @Nullable String subtitle, int fadeIn, int stay, int fadeOut) {
        Player player = sbPlayer.getPlayer();
        if (Utils.isCBXXXorLater("1.12")) {
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } else {
            String prefix = "minecraft:title " + sbPlayer.getName();
            Utils.tempPerm(sbPlayer, Permission.MINECRAFT_COMMAND_TITLE, () -> {
                Bukkit.dispatchCommand(player, prefix + " times " + fadeIn + " " + stay + " " + fadeOut);
                Bukkit.dispatchCommand(player, prefix + " subtitle {\"text\":\"" + subtitle + "\"}");
                Bukkit.dispatchCommand(player, prefix + " title {\"text\":\"" + title + "\"}");
                return true;
            });
        }
    }
}