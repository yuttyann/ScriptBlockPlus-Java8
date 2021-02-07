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
package com.github.yuttyann.scriptblockplus.player;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.region.Region;
import com.github.yuttyann.scriptblockplus.script.SBClipboard;
import com.github.yuttyann.scriptblockplus.script.ScriptEdit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * ScriptBlockPlus SBPlayer インターフェース
 * @author yuttyann44581
 */
public interface SBPlayer extends CommandSender {

    /**
     * {@link ScriptBlock}の{@link SBPlayer}を取得します。
     * @param player - {@link Bukkit}の{@link OfflinePlayer}
     * @return {@link SBPlayer} - プレイヤー
     */
    @NotNull
    public static SBPlayer fromPlayer(@NotNull OfflinePlayer player) {
        return fromUUID(player.getUniqueId());
    }

    /**
     * {@link ScriptBlock}の{@link SBPlayer}を取得します。
     * @param uuid - プレイヤーの{@link UUID}
     * @return {@link SBPlayer} - プレイヤー
     */
    @NotNull
    public static SBPlayer fromUUID(@NotNull UUID uuid) {
        BaseSBPlayer sbPlayer = BaseSBPlayer.getSBPlayers().get(uuid);
        if (sbPlayer == null) {
            BaseSBPlayer.getSBPlayers().put(uuid, sbPlayer = new BaseSBPlayer(uuid));
        }
        return sbPlayer;
    }

    /**
     * プレイヤーがオンラインの場合にtrueを返します。
     * @return {@link Boolean} - プレイヤーがオンラインの場合は{@code true}
     */
    boolean isOnline();

    /**
     * {@link Bukkit}の{@link Player}を取得します。
     * @return {@link Player} - プレイヤー
     */
    @NotNull
    Player getPlayer();

    /**
     * {@link Bukkit}の{@link OfflinePlayer}を取得します。
     * @return {@link OfflinePlayer} - オフラインプレイヤー
     */
    @NotNull
    OfflinePlayer getOfflinePlayer();

    /**
     * プレイヤーのインベントリを取得します。
     * @return {@link PlayerInventory} - インベントリ
     */
    @NotNull
    PlayerInventory getInventory();

    /**
     * プレイヤーの{@link UUID}を取得します。
     * @return {@link UUID} - プレイヤーの{@link UUID}
     */
    @NotNull
    UUID getUniqueId();

    /**
     * プレイヤーが存在しているワールドを取得します。
     * @return {@link World} - ワールド
     */
    @NotNull
    World getWorld();

    /**
     * プレイヤーの座標を取得します。
     * @return {@link Location} - プレイヤーの座標
     */
    @NotNull
    Location getLocation();

    /**
     * プレイヤーのメインハンドのアイテムを取得します。
     * @return {@link ItemStack} - メインハンドアイテム
     */
    @Nullable
    ItemStack getItemInMainHand();

    /**
     * プレイヤーのオフハンドのアイテムを取得します。
     * @return {@link ItemStack} - オフハンドアイテム
     */
    @Nullable
    ItemStack getItemInOffHand();

    /**
     * ワールドの選択範囲を取得します。
     * @return {@link Region} - 選択範囲
     */
    @NotNull
    Region getRegion();

    /**
     * {@link SBPlayer}の{@link ObjectMap}を取得します。
     * <p>
     * プレイヤー別のデータを格納しています。
     * @return {@link ObjectMap} - データ構造
     */
    @NotNull
    ObjectMap getObjectMap();

    /**
     * {@link ScriptEdit}をセットします。
     * @param scriptEdit - {@link ScriptEdit}
     */
    void setScriptEdit(@Nullable ScriptEdit scriptEdit);

    /**
     * {@link SBClipboard}をセットします。
     * @param sbClipboard - {@link SBClipboard}
     */
    void setSBClipboard(@Nullable SBClipboard sbClipboard);

    /**
     * {@link BlockCoords}をセットします。
     * @param blockCoords - {@link BlockCoords}
     */
    void setOldBlockCoords(@Nullable BlockCoords blockCoords);

    /**
     * {@link ScriptEdit}を取得します。
     * @return {@link Optional}&lt;{@link ScriptEdit}&gt;
     */
    @NotNull
    Optional<ScriptEdit> getScriptEdit();

    /**
     * {@link SBClipboard}を取得します。
     * @return {@link Optional}&lt;{@link SBClipboard}&gt;
     */
    @NotNull
    Optional<SBClipboard> getSBClipboard();

    /**
     * {@link BlockCoords}を取得します。
     * @return {@link Optional}&lt;{@link BlockCoords}&gt;
     */
    @NotNull
    Optional<BlockCoords> getOldBlockCoords();
}