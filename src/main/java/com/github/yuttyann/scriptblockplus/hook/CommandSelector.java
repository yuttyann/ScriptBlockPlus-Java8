package com.github.yuttyann.scriptblockplus.hook;

import com.github.yuttyann.scriptblockplus.script.option.other.Calculation;
import com.github.yuttyann.scriptblockplus.utils.CommandUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * ScriptBlockPlus CommandSelector クラス
 * @author yuttyann44581
 */
public final class CommandSelector {

    public static final CommandSelector INSTANCE = new CommandSelector();

    private final static String SELECTOR_SUFFIX = "aeprs";
    private final static String[] SELECTOR_NAMES = { "@a", "@e", "@p", "@r", "@s" };

    private class Index {

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

    public boolean has(@NotNull String command) {
        return Stream.of(SELECTOR_NAMES).anyMatch(command::contains);
    }

    @NotNull
    public List<String> build(@NotNull CommandSender sender, @NotNull String command) {
        List<Index> indexList = new ArrayList<>();
        List<String> commandList = new ArrayList<>();
        commandList.add(parse(command.toCharArray(), sender, indexList));
        for (int i = 0; i < indexList.size(); i++) {
            String selector = indexList.get(i).substring(command);
            Entity[] entities = getTargets(sender, selector);
            if ((entities == null || entities.length == 0) && selector.startsWith("@p") && sender instanceof Player) {
                entities = new Entity[] { (Entity) sender };
            }
            if (entities == null) {
                continue;
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
                return Collections.singletonList(command);
            } else {
                int index = i;
                String name = getName(entities[0]);
                commandList.replaceAll(s -> StringUtils.replace(s, "{" + index + "}", name));
            }
        }
        return commandList;
    }

    @NotNull
    private String parse(char[] chars, @NotNull CommandSender sender, @NotNull List<Index> indexList) {
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
                    builder.append(getIntRelative(tempBuilder.toString(), xyz, (Entity) sender));
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
    private String getName(@NotNull Entity entity) {
        return (entity instanceof Player) ? entity.getName() : entity.getUniqueId().toString();
    }

    @NotNull
    private Entity[] getTargets(@NotNull CommandSender sender, @NotNull String selector) {
        if (Utils.isCBXXXorLater("1.13.2")) {
            return Bukkit.selectEntities(sender, selector).toArray(new Entity[0]);
        }
        return CommandUtils.getTargets(sender, selector);
    }

    private int getIntRelative(@NotNull String target, @NotNull String relative, @NotNull Entity entity) {
        int number = 0;
        if (StringUtils.isNotEmpty(target) && Calculation.REALNUMBER_PATTERN.matcher(target).matches()) {
            number = Integer.parseInt(target);
        }
        switch (relative) {
            case "x":
                return entity.getLocation().getBlockX() + number;
            case "y":
                return entity.getLocation().getBlockY() + number;
            case "z":
                return entity.getLocation().getBlockZ() + number;
            default:
                return 0;
        }
    }
}