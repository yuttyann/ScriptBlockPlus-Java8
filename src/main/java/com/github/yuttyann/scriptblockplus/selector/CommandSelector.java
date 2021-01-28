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
package com.github.yuttyann.scriptblockplus.selector;

import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.hook.plugin.Placeholder;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.utils.NMSHelper;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import com.github.yuttyann.scriptblockplus.selector.entity.EntitySelector;
import com.google.common.collect.Lists;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

/**
 * ScriptBlockPlus CommandSelector クラス
 * @author yuttyann44581
 */
public final class CommandSelector {

    private final static String SELECTOR_SUFFIX = "aeprs";
    private final static String[] SELECTOR_NAMES = { "@a", "@e", "@p", "@r", "@s" };

    private static class Index {

        private final int start;
        private int end;

        private Index(int start, int end) {
            this.start = start;
            this.end = end;
        }

        @NotNull
        private String substring(@NotNull String source) {
            return source.substring(start, Math.min(end + 1, source.length()));
        }
    }

    public static boolean has(@NotNull String source) {
        return StreamUtils.anyMatch(SELECTOR_NAMES, source::contains);
    }

    @NotNull
    public static List<String> build(@NotNull CommandSender sender, @Nullable Location location, @NotNull String command) {
        int modCount = 0;
        List<Index> indexList = new ArrayList<Index>();
        List<String> commandList = Lists.newArrayList(parse(command, sender, indexList));
        for (int i = 0; i < indexList.size(); i++) {
            String selector = indexList.get(i).substring(command);
            Entity[] entities = getTargets(sender, location, selector);
            if (entities == null || entities.length == 0) {
                if (StreamUtils.anyMatch(SELECTOR_NAMES, s -> selector.startsWith(s + "["))) {
                    continue;
                } else if (selector.startsWith(SELECTOR_NAMES[2]) && sender instanceof Player) {
                    entities = new Entity[] { (Entity) sender };
                } else {
                    continue;
                }
            }
            boolean works = true;
            for (int j = 1; j < entities.length; j++) {
                if (entities[j] == null) {
                    works = false;
                    break;
                }
                commandList.add(StringUtils.replace(commandList.get(0), "{" + i + "}", getEntityName(entities[j])));
            }
            if (!works || entities.length == 0 || entities[0] == null) {
                return Collections.emptyList();
            } else {
                int index = i;
                String name = getEntityName(entities[0]);
                commandList.replaceAll(s -> StringUtils.replace(s, "{" + index + "}", name));
            }
            modCount++;
        }
        if (modCount > 0 && modCount != indexList.size()) {
            String name = sender.getName();
            IntStream stream = IntStream.of(indexList.size());
            stream.forEach(i -> commandList.replaceAll(s -> StringUtils.replace(s, "{" + i + "}", name)));
        }
        return modCount == 0 ? Collections.emptyList() : commandList;
    }

    @NotNull
    private static String parse(@NotNull String source, @NotNull CommandSender sender, @NotNull List<Index> indexList) {
        char[] chars = source.toCharArray();
        StringBuilder builder = new StringBuilder();
        Location location = null;
        for (int i = 0, j = 0, k = 0; i < chars.length; i++) {
            int one = i + 1, two = one + 1;
            if (chars[i] == '@' && one < chars.length && SELECTOR_SUFFIX.indexOf(chars[one]) > -1) {
                Index index = new Index(i, one);
                if (two < chars.length && chars[two] == '[') {
                    for (int l = two, m = 0; l < chars.length; l++) {
                        if (chars[l] == '[') {
                            m++;
                        } else if (chars[l] == ']') {
                            if (--m == 0) {
                                index.end = l;
                                i += Math.max(index.end - index.start, 0);
                                break;
                            }
                        }
                    }
                } else {
                    i++;
                }
                indexList.add(index);
                builder.append('{').append(j++).append('}');
            } else if ((chars[i] == '~' || chars[i] == '^') && (sender instanceof Entity || sender instanceof BlockCommandSender)) {
                if (k >= 3) {
                    builder.append(sender.getName());
                } else {
                    String axes = k == 0 ? "x" : k == 1 ? "y" : k == 2 ? "z" : "x";
                    StringBuilder tempBuilder = new StringBuilder().append(chars[i]);
                    for (int l = one; l < chars.length; l++) {
                        if ("+-.0123456789".indexOf(chars[l]) > -1) {
                            tempBuilder.append(chars[l]);
                            i++;
                        } else {
                            break;
                        }
                    }
                    if (location == null) {
                        location = EntitySelector.copy(sender, null);
                    }
                    EntitySelector.setLocation(location, axes, tempBuilder.toString());
                    builder.append('{').append(axes).append('}');
                    k++;
                }
            } else {
                builder.append(chars[i]);
            }
        }
        String result = builder.toString();
        if (location != null) {
            result = StringUtils.replace(result, "{x}", location.getX());
            result = StringUtils.replace(result, "{y}", location.getY());
            result = StringUtils.replace(result, "{z}", location.getZ());
        }
        return result;
    }

    @NotNull
    public static Entity[] getTargets(@NotNull CommandSender sender, @Nullable Location location, @NotNull String selector) {
        selector = Placeholder.INSTANCE.replace(getWorld(sender, location), selector);
        if (PackageType.HAS_NMS && Utils.isCBXXXorLater("1.13")) {
            try {
                return NMSHelper.selectEntities(sender, location, selector);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        } else if (Utils.isCBXXXorLater("1.13.2")) {
            return Bukkit.selectEntities(sender, selector).toArray(new Entity[0]);
        }
        return EntitySelector.getEntities(sender, location, selector);
    }

    @NotNull
    private static World getWorld(@NotNull CommandSender sender, @Nullable Location location) {
        if (location != null) {
            return location.getWorld();
        }
        World world = null;
        if (sender instanceof ProxiedCommandSender) {
            sender = ((ProxiedCommandSender) sender).getCallee();
        }
        if (sender instanceof Entity) {
            world = ((Entity) sender).getWorld();
        } else if (sender instanceof SBPlayer) {
            world = ((SBPlayer) sender).getWorld();
        } else if (sender instanceof BlockCommandSender) {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        }
        return world == null ? Bukkit.getWorlds().get(0) : world;
    }

    @NotNull
    private static String getEntityName(@NotNull Entity entity) {
        return entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
    }
}