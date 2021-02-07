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
package com.github.yuttyann.scriptblockplus.utils.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus ReuseIterator クラス
 * <p>
 * 繰り返し検索を行えるようにするイテラトラ
 * @param <T> 値の型
 * @author yuttyann44581
 */
public class ReuseIterator<T> implements Iterator<T> {

    private final T[] array;
    private final int length;

    private int cursor;
    private boolean hasNext;

    public ReuseIterator(@NotNull T[] array) {
        this.array = array;
        this.length = array.length;
        hasNext();
    }

    @SuppressWarnings("unchecked")
    public ReuseIterator(@NotNull Collection<T> collection) {
        this((T[]) collection.toArray());
    }

    public void reset() {
        this.cursor = 0;
        hasNext();
    }

    @Override
    public boolean hasNext() {
        return hasNext = cursor < length;
    }

    @Override
    public T next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        return array[cursor++];
    }
}