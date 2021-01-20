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
package com.github.yuttyann.scriptblockplus.listener;

import java.util.HashSet;
import java.util.Set;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.enums.Filter;
import com.github.yuttyann.scriptblockplus.file.json.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.element.BlockScript;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import com.github.yuttyann.scriptblockplus.script.ScriptRead;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.selector.CommandSelector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus BlockListener クラス
 * @author yuttyann44581
 */
public class BlockListener implements Listener {

    private static final int LENGTH = Filter.getPrefix().length();
    private static final Set<String> REDSTONE_FLAG = new HashSet<>();
    private static final FilterValue[] EMPTY_FILTER_ARRAY = new FilterValue[0];

    private class FilterSplit {

        private final String filters, selector;

        private FilterSplit(@NotNull String source) {
            if (source.startsWith(Filter.getPrefix())) {
                int end = source.indexOf("}");
                this.filters = source.substring(LENGTH, end).trim();
                this.selector = source.substring(end + 1, source.length()).trim();
            } else {
                this.filters = null;
                this.selector = source;
            }
        }

        @NotNull
        public String getSelector() {
            return selector;
        }

        @Nullable
        public FilterValue[] getFilterValues() {
            if (StringUtils.isEmpty(filters)) {
                return EMPTY_FILTER_ARRAY;
            }
            String[] array = StringUtils.split(filters, ',');
            return StreamUtils.toArray(array, FilterValue::new, new FilterValue[array.length]);
        }
    }

    private class FilterValue {

        private final String value;
        private final Filter filter;

        private FilterValue(@NotNull String source) {
            for (Filter filter : Filter.values()) {
                if (source.startsWith(filter.getSyntax())) {
                    this.value = filter.getValue(source);
                    this.filter = filter;
                    return;
                }
            }
            this.value = null;
            this.filter = Filter.NONE;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        @NotNull
        public Filter getFilter() {
            return filter;
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        Location location = event.getBlock().getLocation();
        String fullCoords = BlockCoords.getFullCoords(location);
        if (!event.getBlock().isBlockIndirectlyPowered()) {
            REDSTONE_FLAG.remove(fullCoords);
            return;
        }
        if (REDSTONE_FLAG.contains(fullCoords)) {
            return;
        }
        for (ScriptKey scriptKey : ScriptKey.values()) {
            BlockScriptJson scriptJson = new BlockScriptJson(scriptKey);
            if (!scriptJson.exists()) {
                continue;
            }
            BlockScript blockScript = scriptJson.load();
            if (!blockScript.has(location)) {
                continue;
            }
            String selector = blockScript.get(location).getSelector();
            if (StringUtils.isEmpty(selector) || !CommandSelector.has(selector)) {
                continue;
            }
            int[] index = new int[] { 0 };
            FilterSplit filterSplit = new FilterSplit(selector);
            FilterValue[] filterValues = filterSplit.getFilterValues();
            for (Entity target : CommandSelector.getTargets(Bukkit.getConsoleSender(), setCenter(location), filterSplit.getSelector())) {
                if (!(target instanceof Player)) {
                    continue;
                }
                Player player = (Player) target;
                if (filterValues.length > 0 && !StreamUtils.allMatch(filterValues, t -> has(player, t, index[0]))) {
                    continue;
                }
                index[0]++;
                REDSTONE_FLAG.add(fullCoords);
                new ScriptRead(player, location, scriptKey).read(0);
            }
        }
    }

    public boolean has(@NotNull Player player, @NotNull FilterValue filterValue, int index) {
        String value = filterValue.getValue();
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        switch (filterValue.getFilter()) {
            case OP:
                return Boolean.parseBoolean(value) ? player.isOp() : !player.isOp();
            case PERM:
                return player.hasPermission(value);
            case LIMIT:
                return index < Integer.parseInt(value);
            default:
                return false;
        }
    }

    @NotNull
    private Location setCenter(@NotNull Location location) {
        location.setX(location.getBlockX() + 0.5D);
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ() + 0.5D);
        return location;
    }
}