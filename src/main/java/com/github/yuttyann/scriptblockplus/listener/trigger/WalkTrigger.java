package com.github.yuttyann.scriptblockplus.listener.trigger;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.listener.TriggerListener;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;

import org.bukkit.Location;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus WalkTrigger クラス
 * @author yuttyann44581
 */
public class WalkTrigger extends TriggerListener<PlayerMoveEvent> {

    public WalkTrigger(@NotNull ScriptBlock plugin) {
        super(plugin, ScriptKey.WALK, EventPriority.HIGH);
    }

    @Override
    @Nullable
    public Trigger create(@NotNull PlayerMoveEvent event) {
        SBPlayer sbPlayer = SBPlayer.fromPlayer(event.getPlayer());
        BlockCoords blockCoords = new BlockCoords(sbPlayer.getLocation()).subtract(0, 1, 0);
        if (blockCoords.equals(sbPlayer.getOldBlockCoords().orElse(null))) {
            return null;
        } else {
            sbPlayer.setOldBlockCoords(blockCoords);
        }
        Location location = blockCoords.toLocation();
        return new Trigger(sbPlayer.getPlayer(), location.getBlock(), event);
    }
}