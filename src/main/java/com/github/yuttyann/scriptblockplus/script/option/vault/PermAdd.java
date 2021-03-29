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
package com.github.yuttyann.scriptblockplus.script.option.vault;

import java.util.List;

import com.github.yuttyann.scriptblockplus.bridge.plugin.VaultPermission;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;

/**
 * ScriptBlockPlus PermAdd オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "perm_add", syntax = "@permADD:")
public final class PermAdd extends BaseOption {

    @Override
    protected boolean isValid() throws Exception {
        VaultPermission vaultPermission = VaultPermission.INSTANCE;
        if (!vaultPermission.isEnabled() || vaultPermission.isSuperPerms()) {
            throw new UnsupportedOperationException();
        }
        List<String> slash = StringUtils.split(getOptionValue(), '/');
        String world = slash.size() > 1 ? slash.get(0) : null;
        String permission = slash.size() > 1 ? slash.get(1) : slash.get(0);
        vaultPermission.playerAdd(world, getPlayer(), permission);
        return true;
    }
}