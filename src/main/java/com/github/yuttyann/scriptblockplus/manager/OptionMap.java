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

import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionIndex;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * ScriptBlockPlus OptionMap クラス
 * @author yuttyann44581
 */
public final class OptionMap {

    private static final Field ORDINAL;

    static {
        Field field = null;
        try {
            field = Option.class.getDeclaredField("ordinal");
            field.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        } finally {
            ORDINAL = field;
        }
    }

    private final List<String> SYNTAXES = new ArrayList<>();
    private final Map<String, SBInstance<Option>> SBINSTANCES = new HashMap<>();

    public void clear() {
        SYNTAXES.clear();
        SBINSTANCES.clear();
    }

    @NotNull
    public Option getOption(@NotNull String syntax) {
        SBInstance<Option> sbInstance = getInstance(syntax);
        if (sbInstance == null) {
            throw new NullPointerException("Option[" + syntax + "] does not exist");
        }
        return sbInstance.get();
    }

    @Nullable
    public SBInstance<Option> getInstance(@NotNull String syntax) {
        for (int i = 0, l = SYNTAXES.size(); i < l; i++) {
            String value = SYNTAXES.get(i);
            if (syntax.indexOf(value) == 0) {
                return SBINSTANCES.get(value);
            }
        }
        return null;
    }

    @Nullable
    public SBInstance<Option> put(@NotNull SBInstance<Option> sbInstance) {
        String syntax = sbInstance.get().getSyntax();
        if (!SBINSTANCES.containsKey(syntax)) {
            SYNTAXES.add(syntax);
        }
        return SBINSTANCES.put(syntax, sbInstance);
    }

    @Nullable
    public SBInstance<Option> put(@NotNull OptionIndex optionIndex, @NotNull SBInstance<Option> sbInstance) {
        String syntax = sbInstance.get().getSyntax();
        if (!SBINSTANCES.containsKey(syntax)) {
            switch (optionIndex.getIndexType()) {
                case TOP:
                    SYNTAXES.add(0, syntax);
                    break;
                case LAST:
                    SYNTAXES.add(syntax);
                    break;
                default:
                    int index = SYNTAXES.indexOf(optionIndex.getOptionTag().syntax());
                    int amount = optionIndex.getIndexType().getAmount();
                    SYNTAXES.add(Math.min(Math.max(index + amount, 0), SYNTAXES.size()), syntax);
                    break;
            }
        }
        return SBINSTANCES.put(syntax, sbInstance);
    }

    @Nullable
    public SBInstance<Option> remove(@NotNull Object key) {
        SYNTAXES.remove(key);
        return SBINSTANCES.remove(key);
    }

    @NotNull
    public List<SBInstance<Option>> list() {
        List<SBInstance<Option>> list = new ArrayList<>(values());
        list.sort((c1, c2) -> c1.get().compareTo(c2.get()));
        return list;
    }

    @NotNull
    public Collection<SBInstance<Option>> values() {
        return SBINSTANCES.values();
    }

    public synchronized void updateOrdinal() {
        try {
            for (int i = 0, l = SYNTAXES.size(); i < l; i++) {
                ORDINAL.setInt(SBINSTANCES.get(SYNTAXES.get(i)).get(), i);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}