package com.github.yuttyann.scriptblockplus.item.action;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.enums.Permission;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.item.ChangeSlot;
import com.github.yuttyann.scriptblockplus.item.ItemAction;
import com.github.yuttyann.scriptblockplus.item.RunItem;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.region.CuboidRegion;
import com.github.yuttyann.scriptblockplus.utils.ItemUtils;

import org.bukkit.Location;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus BlockSelector クラス
 * @author yuttyann44581
 */
public class BlockSelector extends ItemAction {

    public BlockSelector() {
        super(ItemUtils.getBlockSelector());
    }

    @Override
    public boolean hasPermission(@NotNull Permissible permissible) {
        return Permission.TOOL_BLOCK_SELECTOR.has(permissible);
    }

    @Override
    public void slot(@NotNull ChangeSlot changeSlot) {

    }

    @Override
    public void run(@NotNull RunItem runItem) {
        Location location = runItem.getLocation();
        SBPlayer sbPlayer = SBPlayer.fromPlayer(runItem.getPlayer());
        CuboidRegion region = ((CuboidRegion) sbPlayer.getRegion());
        switch (runItem.getAction()) {
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                if (runItem.isSneaking()) {
                    region.setVector1((location = sbPlayer.getLocation()).toVector());
                } else if (!runItem.isAIR() && location != null) {
                    region.setVector1(location.toVector());
                }
                if (location != null) {
                    region.setWorld(location.getWorld());
                    SBConfig.SELECTOR_POS1.replace(region.getName(), BlockCoords.getCoords(location)).send(sbPlayer);
                }
                break;
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                if (runItem.isSneaking()) {
                    region.setVector2((location = sbPlayer.getLocation()).toVector());
                } else if (!runItem.isAIR() && location != null) {
                    region.setVector2(location.toVector());
                }
                if (location != null) {
                    region.setWorld(location.getWorld());
                    SBConfig.SELECTOR_POS2.replace(region.getName(), BlockCoords.getCoords(location)).send(sbPlayer);
                }
                break;
            default:
        }
    }
}