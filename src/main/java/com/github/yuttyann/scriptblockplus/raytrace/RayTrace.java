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
package com.github.yuttyann.scriptblockplus.raytrace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.utils.NMSHelper;
import com.github.yuttyann.scriptblockplus.utils.Utils;


import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus RayTrace クラス
 * @author yuttyann44581
 */
public final class RayTrace {

    private final Vector start;
    private final Vector direction;

    public RayTrace(@NotNull Player player) {
        this(player.getEyeLocation().toVector(), player.getEyeLocation().getDirection());
    }

    public RayTrace(@NotNull Vector start, @NotNull Vector direction) {
        this.start = start;
        this.direction = direction;
    }

    @Nullable
    public static RayResult rayTraceBlocks(@NotNull Player player, final double distance) {
        if (Utils.isCBXXXorLater("1.13.2")) {
            RayTraceResult rayTraceResult = player.rayTraceBlocks(distance, FluidCollisionMode.NEVER);
            if (rayTraceResult == null || rayTraceResult.getHitBlock() == null) {
                return null;
            }
            return new RayResult(rayTraceResult.getHitBlock(), rayTraceResult.getHitBlockFace());
        } else {
            if (PackageType.HAS_NMS) {
                try {
                    return NMSHelper.rayTraceBlocks(player, distance);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            } else {
                // 疑似的に再現、ブロックの側面取得の精度は劣る
                Set<Block> blocks = rayTraceBlocks(player, distance, 0.05D, true);
                if (blocks.size() > 1) {
                    Block old = null, now = null;
                    Iterator<Block> iterator = blocks.iterator();
                    Location location = player.getEyeLocation();
                    while (iterator.hasNext()) {
                        old = now;
                        now = iterator.next();
                        if (BlockCoords.compare(location, now.getLocation())) {
                            continue;
                        }
                        if (old != null && now.getType().isOccluding()) {
                            BlockFace blockFace = now.getFace(old);
                            return new RayResult(now, blockFace);
                        }
                    }   
                }
            }
            return null;
        }
    }

    @NotNull
    public static Set<Block> rayTraceBlocks(@NotNull Player player, final double distance, final double accuracy, final boolean square) {
        World world = player.getWorld();
        Set<Block> blocks = new LinkedHashSet<Block>();
        RayTrace rayTrace = new RayTrace(player);
        List<Vector> positions = rayTrace.traverse(distance, accuracy);
        SBBoundingBox boundingBox = new SBBoundingBox();
        for (int i = 0, l = positions.size(); i < l; i++) {
            Vector position = positions.get(i);
            Location location = position.toLocation(world);
            boundingBox.setBlock(location.getBlock(), square);
            if(rayTrace.intersects(boundingBox, distance, accuracy)){
                blocks.add(location.getBlock());
            }
        }
        return blocks;
    }

    @NotNull
    public Vector getPostion(final double distance) {
        Vector vector1 = new Vector(start.getX(), start.getY(), start.getZ());
        Vector vector2 = new Vector(direction.getX(), direction.getY(), direction.getZ());
        return vector1.add(vector2.multiply(distance));
    }

    public boolean isOnLine(@NotNull Vector position) {
        double t = (position.getX() - start.getX()) / direction.getX();
        if (position.getBlockY() == start.getY() + (t * direction.getY()) && position.getBlockZ() == start.getZ() + (t * direction.getZ())) {
            return true;
        }
        return false;
    }

    @NotNull
    public List<Vector> traverse(final double distance, final double accuracy) {
        List<Vector> positions = new ArrayList<Vector>();
        for (double d = 0.0D; d <= distance; d += accuracy) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    @Nullable
    public Vector positionOfIntersection(@NotNull Vector min, @NotNull Vector max, final double distance, final double accuracy) {
        return positionOfIntersection(new SBBoundingBox(min, max), distance, accuracy);
    }

    public boolean intersects(@NotNull Vector min, @NotNull Vector max, final double distance, final double accuracy) {
        return intersects(new SBBoundingBox(min, max), distance, accuracy);
    }

    @Nullable
    public Vector positionOfIntersection(@NotNull SBBoundingBox boundingBox, final double distance, final double accuracy) {
        List<Vector> positions = traverse(distance, accuracy);
        for (int i = 0, l = positions.size(); i < l; i++) {
            Vector position = positions.get(i);
            if (intersects(position, boundingBox)) {
                return position;
            }
        }
        return null;
    }

    public boolean intersects(@NotNull SBBoundingBox boundingBox, final double distance, final double accuracy) {
        List<Vector> positions = traverse(distance, accuracy);
        for (int i = 0, l = positions.size(); i < l; i++) {
            if (intersects(positions.get(i), boundingBox)) {
                return true;
            }
        }
        return false;
    }

    public static boolean intersects(@NotNull Vector position, @NotNull SBBoundingBox boundingBox) {
        if (position.getX() < boundingBox.getMinX() || position.getX() > boundingBox.getMaxX()) {
            return false;
        } else if (position.getY() < boundingBox.getMinY() || position.getY() > boundingBox.getMaxY()) {
            return false;
        } else if (position.getZ() < boundingBox.getMinZ() || position.getZ() > boundingBox.getMaxZ()) {
            return false;
        }
        return true;
    }
}