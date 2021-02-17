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
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.Bukkit;

/**
 * ScriptBlockPlus Say オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "say", syntax = "@say ")
public final class Say extends BaseOption {

    @Override
    protected boolean isValid() throws Exception {
        String command = "minecraft:say " + getOptionValue();
        return Utils.tempPerm(getSBPlayer(), Permission.MINECRAFT_COMMAND_SAY, () -> Bukkit.dispatchCommand(getPlayer(), command));
    }
}