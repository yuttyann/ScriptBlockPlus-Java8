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

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * ScriptBlockPlus StreamUtils クラス
 * @author yuttyann44581
 */
public final class StreamUtils {

    @NotNull
    public static <T, R> R[] toArray(@NotNull Collection<T> collection, @NotNull Function<T, R> mapper, @NotNull R[] array) {
        Iterator<T> iterator = collection.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            array[i] = mapper.apply(iterator.next());
        }
        return array;
    }

    public static <T> void fForEach(@NotNull T[] array, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {
        forEach(array, t -> filter(t, filter, action));
    }

    public static <T> void fForEach(@NotNull Collection<T> collection, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {
        collection.forEach(t -> filter(t, filter, action));
    }

    public static <T> void forEach(@NotNull T[] array, @NotNull Consumer<T> action) {
        for (T t : array) {
            action.accept(t);
        }
    }

    public static <T> boolean anyMatch(@NotNull T[] array, @NotNull Predicate<T> filter) {
        for (T t : array) {
            if (filter.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean anyMatch(@NotNull Collection<T> collection, @NotNull Predicate<T> filter) {
        for (T t : collection) {
            if (filter.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> void filter(@NotNull T t, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {
        if (filter.test(t)) {
            action.accept(t);
        }
    }

    public static <T> void filterNot(@NotNull T t, @NotNull Predicate<T> filter, @NotNull Consumer<T> action) {
        if (!filter.test(t)) {
            action.accept(t);
        }
    }
    
    public static <T> void ifAction(@NotNull boolean value, @NotNull Runnable runnable) {
        if (value) {
            runnable.run();
        }
    }
}