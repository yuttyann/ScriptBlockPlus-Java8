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