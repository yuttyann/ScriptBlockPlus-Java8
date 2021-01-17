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
package com.github.yuttyann.scriptblockplus.utils;

import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.hook.plugin.Placeholder;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.option.other.Calculation;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * ScriptBlockPlus CommandSelector クラス
 * @author yuttyann44581
 */
public final class CommandSelector {

    private final static String SELECTOR_SUFFIX = "aeprs";
    private final static String[] SELECTOR_NAMES = { "@a", "@e", "@p", "@r", "@s" };

    private static class Index {

        private final int start;
        private int end = 0;

        private Index(int start) {
            this.start = start;
        }

        @NotNull
        private String substring(@NotNull String source) {
            return source.substring(start, Math.min(end + 1, source.length()));
        }
    }

    public static boolean has(@NotNull String text) {
        return Stream.of(SELECTOR_NAMES).anyMatch(text::contains);
    }

    @NotNull
    public static List<String> build(@NotNull CommandSender sender, @Nullable Location location, @NotNull String command) {
        int modCount = 0;
        List<Index> indexList = new ArrayList<>();
        List<String> commandList = new ArrayList<>();
        commandList.add(parse(command, sender, indexList));
        for (int i = 0; i < indexList.size(); i++) {
            String selector = indexList.get(i).substring(command);
            Entity[] entities = getTargets(sender, location, selector);
            if (entities == null || entities.length == 0) {
                if (StreamUtils.anyMatch(SELECTOR_NAMES, s -> selector.startsWith(s + "["))) {
                    continue;
                } else if (selector.startsWith("@p") && sender instanceof Player) {
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
                commandList.add(StringUtils.replace(commandList.get(0), "{" + i + "}", getName(entities[j])));
            }
            if (!works || entities.length == 0 || entities[0] == null) {
                return Collections.emptyList();
            } else {
                int index = i;
                String name = getName(entities[0]);
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
        for (int i = 0, j = 0, k = 0; i < chars.length; i++) {
            int type = i + 1, tag = i + 2;
            if (chars[i] == '~' || chars[i] == '^') {
                if (k >= 3) {
                    builder.append(sender.getName());
                } else {
                    String xyz = k == 0 ? "x" : k == 1 ? "y" : k == 2 ? "z" : "x";
                    StringBuilder tempBuilder = new StringBuilder();
                    for (int l = type; l < chars.length; l++) {
                        if ("0123456789".indexOf(chars[l]) > -1) {
                            i++;
                            tempBuilder.append(chars[l]);
                        } else {
                            break;
                        }
                    }
                    builder.append(getRelative(tempBuilder.toString(), xyz, (Entity) sender));
                    k++;
                }
            } else if (chars[i] == '@' && type < chars.length && SELECTOR_SUFFIX.indexOf(chars[type]) > -1) {
                Index textIndex = new Index(i);
                textIndex.end = type;
                if (tag < chars.length && chars[tag] == '[') {
                    for (int l = tag, m = 0; l < chars.length; l++) {
                        if (chars[l] == '[') {
                            m++;
                        } else if (chars[l] == ']') {
                            if (--m == 0) {
                                textIndex.end = l;
                                i += Math.max(textIndex.end - textIndex.start, 0);
                                break;
                            }
                        }
                    }
                } else {
                    i++;
                }
                indexList.add(textIndex);
                builder.append('{').append(j++).append('}');
            } else {
                builder.append(chars[i]);
            }
        }
        return builder.toString();
    }

    @NotNull
    public static Entity[] getTargets(@NotNull CommandSender sender, @Nullable Location location, @NotNull String selector) {
        selector = Placeholder.INSTANCE.replace(getWorld(sender, location), selector);
        if (Utils.isCBXXXorLater("1.13.2")) {
            if (PackageType.HAS_NMS) {
                try {
                    return PackageType.selectEntities(sender, location, selector);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            } else {
                return Bukkit.selectEntities(sender, selector).toArray(new Entity[0]);
            }
        }
        return CommandUtils.getTargets(sender, location, selector);
    }

    private static World getWorld(@NotNull CommandSender sender, @Nullable Location location) {
        if (location != null) {
            return location.getWorld();
        }
        World world = null;
        if (sender instanceof ProxiedCommandSender) {
            sender = ((ProxiedCommandSender) sender).getCallee();
        }
        if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else if (sender instanceof SBPlayer) {
            world = ((SBPlayer) sender).getWorld();
        } else if (sender instanceof BlockCommandSender) {
            world = ((BlockCommandSender) sender).getBlock().getWorld();
        } else if (sender instanceof CommandMinecart) {
            world = ((CommandMinecart) sender).getWorld();
        }
        return world == null ? Bukkit.getWorlds().get(0) : world;
    } 

    @NotNull
    private static String getName(@NotNull Entity entity) {
        return entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
    }

    private static double getRelative(@NotNull String target, @NotNull String relative, @NotNull Entity entity) {
        int number = 0;
        if (StringUtils.isNotEmpty(target) && Calculation.REALNUMBER_PATTERN.matcher(target).matches()) {
            number = Integer.parseInt(target);
        }
        switch (relative) {
            case "x":
                return entity.getLocation().getX() + number;
            case "y":
                return entity.getLocation().getY() + number;
            case "z":
                return entity.getLocation().getZ() + number;
            default:
                return 0;
        }
    }
}