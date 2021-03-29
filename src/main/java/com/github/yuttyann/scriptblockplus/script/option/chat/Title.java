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

import java.util.List;

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
        List<String> split = StringUtils.split(getOptionValue(), '/');
        String title = StringUtils.setColor(split.get(0) + "");
        String subtitle = StringUtils.setColor(split.size() > 1 ? split.get(1) : "");
        int fadeIn = 10, stay = 40, fadeOut = 10;
        if (split.size() == 3) {
            List<String> times = StringUtils.split(split.get(2), '-');
            if (times.size() == 3) {
                fadeIn = Integer.parseInt(times.get(0));
                stay = Integer.parseInt(times.get(1));
                fadeOut = Integer.parseInt(times.get(2));
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