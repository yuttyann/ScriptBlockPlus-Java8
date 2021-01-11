package com.github.yuttyann.scriptblockplus.manager;

import com.github.yuttyann.scriptblockplus.enums.InstanceType;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionIndex;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * ScriptBlockPlus OptionMap クラス
 * @author yuttyann44581
 */
@SuppressWarnings("serial")
public final class OptionMap extends HashMap<String, Option> {

    private static final Field ORDINAL;

    static {
        Field field = (Field) null;
        try {
            field = Option.class.getDeclaredField("ordinal");
            field.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        } finally {
            ORDINAL = field;
        }
    }

    private final LinkedList<String> LINKED_LIST = new LinkedList<>();

    @Nullable
    public <T extends BaseOption> Option put(@NotNull T option) {
        String syntax = option.getSyntax();
        if (!containsKey(syntax)) {
            LINKED_LIST.add(syntax);
        }
        return super.put(syntax, option);
    }

    @Nullable
    public Option put(@NotNull OptionIndex optionIndex, @NotNull Class<? extends BaseOption> optionClass) {
        String syntax = addSyntax(optionIndex, optionClass.getAnnotation(OptionTag.class).syntax());
        return super.put(syntax, new SBConstructor<>(optionClass).newInstance(InstanceType.REFLECTION));
    }

    @Override
    @Deprecated
    public Option put(@Nullable String key, @Nullable Option value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    private String addSyntax(@NotNull OptionIndex optionIndex, @NotNull String syntax) {
        if (!containsKey(syntax)) {
            switch (optionIndex.getIndexType()) {
                case TOP:
                    LINKED_LIST.addFirst(syntax);
                    break;
                case LAST:
                    LINKED_LIST.addLast(syntax);
                    break;
                default:
                    int index = LINKED_LIST.indexOf(optionIndex.getSyntax());
                    int amount = optionIndex.getIndexType().getAmount();
                    LINKED_LIST.add(Math.min(Math.max(index + amount, 0), LINKED_LIST.size()), syntax);
                    break;
            }
        }
        return syntax;
    }

    @NotNull
    public List<Option> list() {
        List<Option> list = new ArrayList<>(values());
        list.sort(Option::compareTo);
        return list;
    }

    void updateOrdinal() {
        try {
            int index = 0;
            for (String syntax : LINKED_LIST) {
                ORDINAL.setInt(super.get(syntax), index++);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}