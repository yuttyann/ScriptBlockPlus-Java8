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
package com.github.yuttyann.scriptblockplus.file.json.derived;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.file.json.CacheJson;
import com.github.yuttyann.scriptblockplus.file.json.SingleJson;
import com.github.yuttyann.scriptblockplus.file.json.annotation.JsonTag;
import com.github.yuttyann.scriptblockplus.file.json.element.PlayerTemp;
import com.github.yuttyann.scriptblockplus.file.json.element.TimerTemp;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import com.github.yuttyann.scriptblockplus.utils.collection.ReuseIterator;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Set;
import java.util.UUID;

/**
 * ScriptBlockPlus PlayerTempJson クラス
 * @author yuttyann44581
 */
@JsonTag(path = "json/playertemp")
public class PlayerTempJson extends SingleJson<PlayerTemp> {

    private static final CacheJson<UUID> CACHE_JSON = new CacheJson<>(PlayerTempJson.class, PlayerTempJson::new);
    
    private PlayerTempJson(@NotNull File json) {
        super(json);
    }

    protected PlayerTempJson(@NotNull UUID uuid) {
        super(uuid.toString());
    }

    @Override
    protected boolean isTemporary() {
        return false;
    }

    @Override
    @NotNull
    protected PlayerTemp newInstance() {
        return new PlayerTemp();
    }

    @NotNull
    public static PlayerTempJson get(@NotNull UUID uuid) {
        return newJson(uuid, CACHE_JSON);
    }

    public static void removeAll(@NotNull ScriptKey scriptKey, @NotNull BlockCoords blockCoords) {
        removeAll(scriptKey, new ReuseIterator<>(new BlockCoords[] { blockCoords }));
    }

    public static synchronized void removeAll(@NotNull ScriptKey scriptKey, @NotNull ReuseIterator<BlockCoords> reuseIterator) {
        TimerTemp timerTemp = TimerTemp.empty();
        for (File json : getFiles(PlayerTempJson.class)) {
            PlayerTempJson tempJson = new PlayerTempJson(json);
            if (tempJson.isEmpty()) {
                continue;
            }
            Set<TimerTemp> timer = tempJson.load().getTimerTemp();
            if (timer.isEmpty()) {
                continue;
            }
            boolean removed = false;
            reuseIterator.reset();
            while (reuseIterator.hasNext()) {
                BlockCoords blockCoords = reuseIterator.next();
                if (timer.remove(timerTemp.setUniqueId(null).setScriptKey(scriptKey).setBlockCoords(blockCoords))) {
                    removed = true;
                }
                if (timer.remove(timerTemp.setUniqueId(UUID.fromString(tempJson.getName())))) {
                    removed = true;
                }
            }
            if (removed) {
                tempJson.saveFile();
            }
        }
    }
}