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

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

/**
 * ScriptBlockPlus StringUtils クラス
 * @author yuttyann44581
 */
public final class StringUtils {

    private static final Random RANDOM = new Random();

    @NotNull
    public static List<String> getScripts(@NotNull String script) throws IllegalArgumentException {
        int length = script.length();
        char[] chars = script.toCharArray();
        if (chars[0] != '[' || chars[length - 1] != ']') {
            return Collections.singletonList(script);
        }
        List<String> result = new ArrayList<>();
        int start = 0, end = 0;
        for (int i = 0, j = 0, k = 0; i < length; i++) {
            if (chars[i] == '[') {
                start++;
                if (j++ == 0) {
                    k = i;
                }
            } else if (chars[i] == ']') {
                end++;
                if (--j == 0) {
                    result.add(script.substring(k + 1, i));
                }
            }
        }
        if (start != end) {
            throw new IllegalArgumentException("Failed to load the script");
        }
        return result;
    }

    @NotNull
    public static String[] split(@Nullable String source, @NotNull char delimiter) {
        if (isEmpty(source)) {
            return ArrayUtils.EMPTY_STRING_ARRAY;   
        }
        List<String> list = new ArrayList<>();
        char[] chars = source.toCharArray();
        int start = 0;
        boolean match = false;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == delimiter) {
                if (!match) {
                    list.add(source.substring(start, i));
                    match = true;
                }
                start = i + 1;
            } else {
                match = false;
            }
        }
        if (!match) {
            if (list.size() == 0) {
                return new String[] { source };
            }
            list.add(source.substring(start, chars.length));
        }
        return list.toArray(new String[0]);
    }

    @NotNull
    public static String replace(@Nullable String source, @NotNull String search, @Nullable Object replace) {
        return isEmpty(source) ? "" : source.replace(search, replace == null ? "" : replace.toString());
    }

    @NotNull
    public static String setColor(@Nullable String source) {
        source = isEmpty(source) ? "" : ChatColor.translateAlternateColorCodes('&', source);
        return replace(source, "§rc", ChatColor.getByChar(Integer.toHexString(RANDOM.nextInt(16))));
    }

    @NotNull
    public static List<String> setListColor(@NotNull List<String> list) {
        list = new ArrayList<>(list);
        list.replaceAll(StringUtils::setColor);
        return list;
    }

    @NotNull
    public static String getColors(@NotNull String source) {
        char[] chars = source.toCharArray();
        StringBuilder builder = new StringBuilder(chars.length);
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != '§' || (i + 1) >= chars.length) {
                continue;
            }
            if (ChatColor.getByChar(Character.toLowerCase(chars[i + 1])) != null) {
                builder.append(chars[i++]).append(chars[i]);
            }
        }
        return builder.toString();
    }

    @NotNull
    public static String createString(@NotNull String[] args, int start) {
        StringJoiner joiner = new StringJoiner(" ");
        IntStream.range(start, args.length).forEach((i) -> joiner.add(args[i]));
        return joiner.toString();
    }

    @NotNull
    public static String removeStart(@NotNull String source, @NotNull String prefix) {
        return isNotEmpty(prefix) ? (startsWith(source, prefix) ? source.substring(prefix.length()) : source) : source;
    }

    public static boolean startsWith(@Nullable String source, @NotNull String prefix) {
        return isNotEmpty(source) && source.startsWith(prefix);
    }

    public static boolean isNotEmpty(@Nullable String source) {
        return !isEmpty(source);
    }

    public static boolean isEmpty(@Nullable String source) {
        return source == null || source.length() == 0;
    }
}