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
package com.github.yuttyann.scriptblockplus.manager;

import com.github.yuttyann.scriptblockplus.enums.InstanceType;
import com.github.yuttyann.scriptblockplus.script.SBInstance;
import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;

/**
 * ScriptBlockPlus SBConstructor クラス
 * @param <T> コンストラクタの型
 * @author yuttyann44581
 */
@SuppressWarnings("unchecked")
public final class SBConstructor<T> {

    private SBInstance<? extends T> sbInstance;
    private Constructor<? extends T> constructor;

    public SBConstructor(@NotNull Class<? extends T> clazz) {
        this.constructor = getConstructor(clazz);
    }

    public SBConstructor(@NotNull SBInstance<? extends T> sbInstance) {
        this.sbInstance = sbInstance;
    }

    @NotNull
    public Class<? extends T> getDeclaringClass() {
        return sbInstance == null ? constructor.getDeclaringClass() : (Class<? extends T>) sbInstance.getClass();
    }

    @NotNull
    public T getInstance() {
        return sbInstance == null ? newInstance(InstanceType.REFLECTION) : (T) sbInstance;
    }

    @NotNull
    public T newInstance(@NotNull InstanceType instanceType) {
        switch (instanceType) {
        case SBINSTANCE:
            return sbInstance == null ? newInstance(InstanceType.REFLECTION) : sbInstance.newInstance();
        case REFLECTION:
            try {
                if (constructor == null) {
                    constructor = getConstructor(getDeclaringClass());
                }
                T t = constructor.newInstance(ArrayUtils.EMPTY_OBJECT_ARRAY);
                if (!(t instanceof SBInstance)) {
                    throw new IllegalArgumentException("newInstance: " + t.getClass().getName());
                }
                if (sbInstance == null) {
                    sbInstance = (SBInstance<? extends T>) t;
                }
                return t;
            } catch (IllegalArgumentException | ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        throw new NullPointerException("Constructor not found.");
    }

    @Nullable
    private <R extends T> Constructor<R> getConstructor(@NotNull Class<R> clazz) {
        try {
            Constructor<R> constructor = clazz.getDeclaredConstructor(ArrayUtils.EMPTY_CLASS_ARRAY);
            constructor.setAccessible(true);
            return constructor;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
}