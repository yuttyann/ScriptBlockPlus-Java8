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
package com.github.yuttyann.scriptblockplus.item;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.event.RunItemEvent;
import com.github.yuttyann.scriptblockplus.utils.ItemUtils;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.unmodifiable.UnmodifiableItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * ScriptBlockPlus ItemAction クラス
 * @author yuttyann44581
 */
public abstract class ItemAction implements Cloneable {

    private static final Set<ItemAction> ITEMS = new HashSet<>();

    private final ItemStack item;

    public ItemAction(@NotNull ItemStack item) {
        this.item = new UnmodifiableItemStack(item);
    }

    protected abstract void run(@NotNull RunItem runItem);

    protected abstract void slot(@NotNull ChangeSlot changeSlot);

    public boolean hasPermission(@NotNull Permissible permissible) {
        return true;
    }

    @NotNull
    public ItemStack getItem() {
        return item;
    }

    @NotNull
    public static Set<ItemAction> getItems() {
        return ITEMS;
    }

    public static <T extends ItemAction> void register(@NotNull T itemAction) {
        ITEMS.add(itemAction);
    }

    public static boolean has(@NotNull Permissible permissible, @Nullable ItemStack item, boolean permission) {
        Optional<ItemAction> itemAction = StreamUtils.filterFirst(ITEMS, i -> i.compare(item));
        return itemAction.filter(i -> !permission || i.hasPermission(permissible)).isPresent();
    }

    public static boolean callRun(@NotNull Player player, @Nullable ItemStack item, @Nullable Location location, @NotNull Action action) {
        Optional<ItemAction> itemAction = ITEMS.stream().filter(i -> i.compare(item)).filter(i -> i.hasPermission(player)).findFirst();
        if (itemAction.isPresent()) {
            RunItem runItem = new RunItem(item, player, action, location == null ? null : BlockCoords.of(location));
            RunItemEvent runItemEvent = new RunItemEvent(runItem);
            Bukkit.getPluginManager().callEvent(runItemEvent);
            if (!runItemEvent.isCancelled()) {
                itemAction.get().clone().run(runItem);
            }
            return true;
        }
        return false;
    }

    public static void callSlot(@NotNull Player player, @Nullable ItemStack item, int newSlot, int oldSlot) {
        Stream<ItemAction> itemAction = ITEMS.stream().filter(i -> i.compare(item)).filter(i -> i.hasPermission(player));
        itemAction.findFirst().ifPresent(i -> i.clone().slot(new ChangeSlot(player, newSlot, oldSlot)));
    }

    @Override
    @NotNull
    public ItemAction clone() {
        try {
            return (ItemAction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    public boolean compare(@Nullable ItemStack item) {
        return item != null && ItemUtils.compare(this.item, item.getType(), ItemUtils.getName(item));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof ItemAction)) {
            return false;
        }
        return compare(((ItemAction) obj).item);
    }

    @Override
    public int hashCode() {
        return item.hashCode();
    }
}