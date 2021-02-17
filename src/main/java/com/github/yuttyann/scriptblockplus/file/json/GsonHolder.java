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
package com.github.yuttyann.scriptblockplus.file.json;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import com.github.yuttyann.scriptblockplus.file.json.annotation.Alternate;
import com.github.yuttyann.scriptblockplus.file.json.legacy.LegacyEnumFactory;
import com.github.yuttyann.scriptblockplus.file.json.legacy.LegacyReflectiveFactory;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapters;

import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus GsonHolder クラス
 * <p>
 * {@link SerializedName#alternate()}が存在しない古いバージョンの場合は、
 * <p>
 * {@link Alternate#value()}を併用することで、同じ動作をさせることができます。
 * @author yuttyann44581
 */
public final class GsonHolder {

    private static final boolean LEGACY_GSON;

    static {
        Method[] methods = SerializedName.class.getMethods();
        LEGACY_GSON = !StreamUtils.anyMatch(methods, m -> m.getName().equals("alternate"));
    }

    private final GsonBuilder gsonBuilder;

    private Gson gson;

    /**
     * コンストラクタ
     * @param gsonBuilder - {@link GsonBuilder}
     */
    public GsonHolder(@NotNull GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
    }

    /**
     * {@link Gson}を取得します。
     * @return {@link Gson}
     */
    @NotNull
    public Gson getGson() {
        if (gson == null) {
            update();
        }
        return gson;
    }

    /**
     * 設定を更新します。
     */
    public synchronized void update() {
        this.gson = gsonBuilder.create();
        if (LEGACY_GSON) {
            legacy();
        }
    }

    /**
     * 処理の後に設定を更新します。
     * @param action - 処理
     */
    public void builder(@NotNull Consumer<GsonBuilder> action) {
        try {
            action.accept(gsonBuilder);
        } finally {
            update();
        }
    }

    /**
     * 以下の{@link TypeAdapterFactory}を置換する。
     * <p>
     * {@link ReflectiveTypeAdapterFactory} ---> {@link LegacyReflectiveFactory}
     * <p>
     * {@link TypeAdapters#ENUM_FACTORY} ---> {@link LegacyEnumFactory#INSTANCE}
     */
    @SuppressWarnings("unchecked")
    private void legacy() {
        try {
            Field factories = Gson.class.getDeclaredField("factories");
            factories.setAccessible(true);
            Object unmodifiable = factories.get(gson);
            Field innerList = unmodifiable.getClass().getSuperclass().getDeclaredField("list");
            innerList.setAccessible(true);
            List<TypeAdapterFactory> typeFactoryList = (List<TypeAdapterFactory>) innerList.get(unmodifiable);
            typeFactoryList.removeIf(t -> {
                Class<? extends TypeAdapterFactory> factory = t.getClass();
                return factory.equals(ReflectiveTypeAdapterFactory.class) || factory.equals(TypeAdapters.ENUM_FACTORY.getClass());
            });
            Field constructor = Gson.class.getDeclaredField("constructorConstructor");
            constructor.setAccessible(true);
            Field fieldNaming = GsonBuilder.class.getDeclaredField("fieldNamingPolicy");
            fieldNaming.setAccessible(true);
            Field excluder = GsonBuilder.class.getDeclaredField("excluder");
            excluder.setAccessible(true);
            ConstructorConstructor argment1 = (ConstructorConstructor) constructor.get(gson);
            FieldNamingStrategy argment2 = (FieldNamingStrategy) fieldNaming.get(gsonBuilder);
            Excluder argment3 = (Excluder) excluder.get(gsonBuilder);
            typeFactoryList.add(LegacyEnumFactory.INSTANCE);
            typeFactoryList.add(new LegacyReflectiveFactory(argment1, argment2, argment3));
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }
}