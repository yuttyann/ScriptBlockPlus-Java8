package com.github.yuttyann.scriptblockplus.raytrace;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus AdvancedRayTrace クラス
 * 
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
                    return PackageType.rayTraceBlocks(player, distance);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }  
            }
            return null;
        }
    }
    
    @NotNull
    public static Set<Location> rayTraceBlocks(@NotNull Player player, final double distance, final double accuracy, final boolean square) {
        World world = player.getWorld();
        RayTrace rayTrace = new RayTrace(player);
        Set<Location> locations = new LinkedHashSet<>();
        for(Vector position : rayTrace.traverse(distance, accuracy)) {
            Location location = position.toLocation(world);
            if(rayTrace.intersects(new SBBoundingBox(location.getBlock(), square), distance, accuracy)){
                locations.add(location);
            }
        }
        return locations;
    }

    @NotNull
    public Vector getPostion(final double distance) {
        return start.clone().add(direction.clone().multiply(distance));
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
        List<Vector> positions = new ArrayList<>();
        for (double d = 0.0D; d <= distance; d += accuracy) {
            positions.add(getPostion(d));
        }
        return positions;
    }

    @Nullable
    public Vector positionOfIntersection(@NotNull Vector min, @NotNull Vector max, final double distance, final double accuracy) {
        List<Vector> positions = traverse(distance, accuracy);
        for (Vector position : positions) {
            if (intersects(position, min, max)) {
                return position;
            }
        }
        return null;
    }

    public boolean intersects(@NotNull Vector min, @NotNull Vector max, final double distance, final double accuracy) {
        List<Vector> positions = traverse(distance, accuracy);
        for (Vector position : positions) {
            if (intersects(position, min, max)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public Vector positionOfIntersection(@NotNull SBBoundingBox boundingBox, final double distance, final double accuracy) {
        List<Vector> positions = traverse(distance, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMin(), boundingBox.getMax())) {
                return position;
            }
        }
        return null;
    }

    public boolean intersects(@NotNull SBBoundingBox boundingBox, final double distance, final double accuracy) {
        List<Vector> positions = traverse(distance, accuracy);
        for (Vector position : positions) {
            if (intersects(position, boundingBox.getMin(), boundingBox.getMax())) {
                return true;
            }
        }
        return false;
    }

    public static boolean intersects(@NotNull Vector position, @NotNull Vector min, @NotNull Vector max) {
        if (position.getX() < min.getX() || position.getX() > max.getX()) {
            return false;
        } else if (position.getY() < min.getY() || position.getY() > max.getY()) {
            return false;
        } else if (position.getZ() < min.getZ() || position.getZ() > max.getZ()) {
            return false;
        }
        return true;
    }
}