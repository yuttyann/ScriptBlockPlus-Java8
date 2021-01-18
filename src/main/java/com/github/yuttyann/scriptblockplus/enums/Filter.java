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
package com.github.yuttyann.scriptblockplus.enums;

import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus Filter 列挙型
 * @author yuttyann44581
 */
public enum Filter {
    OP("op="),
    PERM("perm="),
    LIMIT("limit="),
    NONE("");

    private static final Filter[] FILTERS = { OP, PERM, LIMIT };

    private final String prefix;

    Filter(@NotNull String prefix) {
        this.prefix = prefix;
    }

    @NotNull
    public static Filter[] getFilters() {
        Filter[] filters = new Filter[FILTERS.length];
        System.arraycopy(FILTERS, 0, filters, 0, FILTERS.length);
        return filters;
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    @NotNull
    public String substring(@NotNull String source) {
        return source.substring(prefix.length(), source.length());
    }
}