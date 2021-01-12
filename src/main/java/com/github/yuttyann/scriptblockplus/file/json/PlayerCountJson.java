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
package com.github.yuttyann.scriptblockplus.file.json;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.file.Json;
import com.github.yuttyann.scriptblockplus.file.json.annotation.JsonOptions;
import com.github.yuttyann.scriptblockplus.file.json.element.PlayerCount;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import com.google.common.collect.Sets;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * ScriptBlockPlus PlayerCountJson クラス
 * @author yuttyann44581
 */
@JsonOptions(path = "json/playercount", file = "{id}.json", classes = { Location.class, ScriptKey.class })
public class PlayerCountJson extends Json<PlayerCount> {

    public PlayerCountJson(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    protected int hashCode(@NotNull Object[] args) {
        return Objects.hash(BlockCoords.getFullCoords((Location) args[0]), args[1]);
    }

    @Override
    @NotNull
    public PlayerCount newInstance(@NotNull Object[] args) {
        return new PlayerCount(BlockCoords.getFullCoords((Location) args[0]), (ScriptKey) args[1]);
    }

    public static void clear(@NotNull Location location, @NotNull ScriptKey scriptKey) {
        clear(Sets.newHashSet(location), scriptKey);
    }

    public static void clear(@NotNull Set<Location> locations, @NotNull ScriptKey scriptKey) {
        Object[] args = new Object[] { (Location) null, scriptKey };
        for (String id : Json.getNames(PlayerCountJson.class)) {
            PlayerCountJson countJson = new PlayerCountJson(UUID.fromString(id));
            if (!countJson.exists()) {
                continue;
            }
            boolean modifiable = false;
            for (Location location : locations) {
                args[0] = location;
                if (!countJson.has(args)) {
                    continue;
                }
                PlayerCount playerCount = countJson.load(args);
                if (playerCount.getAmount() > 0) {
                    modifiable = true;
                    countJson.remove(playerCount);
                }
            }
            if (modifiable) {
                countJson.saveFile();
            }
        }
    }
}