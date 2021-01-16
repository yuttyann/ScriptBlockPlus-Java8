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
package com.github.yuttyann.scriptblockplus.listener;

import com.github.yuttyann.scriptblockplus.event.BlockClickEvent;
import com.github.yuttyann.scriptblockplus.item.ItemAction;
import com.github.yuttyann.scriptblockplus.raytrace.RayResult;
import com.github.yuttyann.scriptblockplus.raytrace.RayTrace;
import com.github.yuttyann.scriptblockplus.player.ObjectMap;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ScriptBlockPlus InteractListener クラス
 * 
 * @author yuttyann44581
 */
public class InteractListener implements Listener {

    private static final String KEY_ENTITY = Utils.randomUUID();
    private static final String KEY_ANIMATION = Utils.randomUUID();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING || player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }
        ObjectMap objectMap = SBPlayer.fromPlayer(player).getObjectMap();
        if (objectMap.getBoolean(KEY_ANIMATION)) {
            objectMap.put(KEY_ANIMATION, false);
            return;
        }
        EquipmentSlot hand = EquipmentSlot.HAND;
        ItemStack item = player.getInventory().getItemInMainHand();
        RayResult rayResult = RayTrace.rayTraceBlocks(player, 4.5D);
        if (rayResult == null) {
            callEvent(new PlayerInteractEvent(player, Action.LEFT_CLICK_AIR, item, null, BlockFace.SOUTH, hand), true);
        } else {
            Block block = rayResult.getHitBlock();
            BlockFace blockFace = rayResult.getHitBlockFace();
            callEvent(new PlayerInteractEvent(player, Action.LEFT_CLICK_BLOCK, item, block, blockFace, hand), true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action == Action.PHYSICAL) {
            return;
        }
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.ADVENTURE) {
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                return;
            }
            ObjectMap objectMap = SBPlayer.fromPlayer(player).getObjectMap();
            if (action == Action.RIGHT_CLICK_BLOCK && !objectMap.getBoolean(KEY_ANIMATION)) {
                objectMap.put(KEY_ANIMATION, true);
            }
        }
        callEvent(event, false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        SBPlayer.fromPlayer(event.getPlayer()).getObjectMap().put(KEY_ENTITY, true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            SBPlayer.fromPlayer(event.getPlayer()).getObjectMap().put(KEY_ANIMATION, true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() != GameMode.ADVENTURE) {
            SBPlayer.fromPlayer(event.getPlayer()).getObjectMap().put(KEY_ANIMATION, false);
        }
    }

    private void callEvent(@NotNull PlayerInteractEvent interactEvent, boolean isAnimation) {
        SBPlayer sbPlayer = SBPlayer.fromPlayer(interactEvent.getPlayer());
        ObjectMap objectMap = sbPlayer.getObjectMap();
        try {
            BlockClickEvent blockEvent = new BlockClickEvent(interactEvent, isAnimation);
            if (objectMap.getBoolean(KEY_ENTITY) && interactEvent.getAction() == Action.LEFT_CLICK_AIR) {
                return;
            }
            if (blockEvent.getHand() == EquipmentSlot.HAND) {
                AtomicBoolean invalid = new AtomicBoolean(false);
                if (ItemAction.callRun(sbPlayer.getPlayer(), blockEvent.getItem(), blockEvent.getLocation(), blockEvent.getAction())) {
                    invalid.set(true);
                } else if (blockEvent.getAction().name().endsWith("_CLICK_BLOCK")) {
                    sbPlayer.getScriptEdit().ifPresent(s -> invalid.set(s.perform(sbPlayer, blockEvent.getLocation())));
                }
                blockEvent.setInvalid(invalid.get());
            }
            Bukkit.getPluginManager().callEvent(blockEvent);
            if (blockEvent.isCancelled() || ItemAction.has(sbPlayer.getPlayer(), blockEvent.getItem(), true)) {
                interactEvent.setCancelled(true);
            }
        } finally {
            objectMap.remove(KEY_ENTITY);
        }
    }
}