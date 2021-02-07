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
package com.github.yuttyann.scriptblockplus.script.option.other;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.file.json.derived.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.derived.PlayerCountJson;
import com.github.yuttyann.scriptblockplus.file.json.derived.PlayerTempJson;
import com.github.yuttyann.scriptblockplus.file.json.element.ScriptParam;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;

/**
 * ScriptBlockPlus Amount オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "amount", syntax = "@amount:")
public class Amount extends BaseOption {

    @Override
    protected boolean isValid() throws Exception {
        BlockScriptJson scriptJson = BlockScriptJson.get(getScriptKey());
        BlockCoords blockCoords = getBlockCoords();
        ScriptParam scriptParam = scriptJson.load().get(blockCoords);
        if (scriptParam.getAmount() == -1) {
            scriptParam.setAmount(Integer.parseInt(getOptionValue()));
        }
        scriptParam.subtractAmount(1);
        if (scriptParam.getAmount() <= 0) {
            PlayerTempJson.removeAll(getScriptKey(), blockCoords);
            PlayerCountJson.removeAll(getScriptKey(), blockCoords);
            scriptJson.load().remove(blockCoords);
        }
        scriptJson.saveFile();
        return true;
    }
}