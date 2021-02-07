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
package com.github.yuttyann.scriptblockplus.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.ProxiedCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.raytrace.RayResult;

import static com.github.yuttyann.scriptblockplus.enums.reflection.PackageType.*;

/**
 * ScriptBlockPlus NMSHelper クラス
 * @author yuttyann44581
 */
public final class NMSHelper {

    public static int getMagmaCubeId() {
        if (!Utils.isCBXXXorLater("1.13")) {
            return 62;
        }
        int entityId = 0;
        try {
            Class<?> entityTypes = NMS.getClass("EntityTypes");
            for (Field field : entityTypes.getFields()) {
                if (!field.getType().equals(entityTypes)) {
                    continue;   
                }
                if (field.getName().equals("MAGMA_CUBE")) {
                    break;
                }
                entityId++;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return entityId;
    }

    public static int getSlimeSizeId() {
        if (!Utils.isCBXXXorLater("1.10")) {
            return 11;
        }
        try {
            Class<?> entitySlime = NMS.getClass("EntitySlime");
            Class<?> dataWatcherObject = NMS.getClass("DataWatcherObject");
            for (Field field : entitySlime.getDeclaredFields()) {
                if (!field.getType().equals(dataWatcherObject)) {
                    continue;
                }
                field.setAccessible(true);
                return (int) NMS.invokeMethod(field.get(null), "DataWatcherObject", "a");
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String text) throws ReflectiveOperationException {
        Object component = NMS.invokeMethod(null, "IChatBaseComponent$ChatSerializer", "a", "{\"text\": \"" + text + "\"}");
        Class<?>[] classes = new Class<?>[] { NMS.getClass("IChatBaseComponent"), byte.class };
        Object value = (byte) 2;
        if (Utils.isCBXXXorLater("1.12")) {
            classes[1] = (value = NMS.getEnumValueOf("ChatMessageType", "GAME_INFO")).getClass();
        }
        Object handle = CB_ENTITY.invokeMethod(player, "CraftPlayer", "getHandle");
        Object connection = NMS.getFieldValue("EntityPlayer", "playerConnection", handle);
        Object packetChat = NMS.getConstructor("PacketPlayOutChat", classes).newInstance(component, value);
        NMS.getMethod("PlayerConnection", "sendPacket", NMS.getClass("Packet")).invoke(connection, packetChat);
    }

    private static final double R = 0.017453292D;

    @Nullable
    public static RayResult rayTraceBlocks(@NotNull Player player, final double distance) throws ReflectiveOperationException {
        Location location = player.getLocation();
        double x = location.getX();
        double y = location.getY() + player.getEyeHeight();
        double z = location.getZ();
        float yaw = location.getYaw();
        float pitch = location.getPitch();
        float f1 = (float) Math.cos((-yaw * R) - Math.PI);
        float f2 = (float) Math.sin((-yaw * R) - Math.PI);
        float f3 = (float) -Math.cos(-pitch * R);
        float f4 = (float) Math.sin(-pitch * R);
        float f5 = f2 * f3;
        float f6 = f1 * f3;
        Object vec3d1 = newVec3D(x, y, z);
        Object vec3d2 = newVec3D(x + (f5 * distance), y + (f4 * distance), z + (f6 * distance));
        Object[] arguments = null;
        if (Utils.isCBXXXorLater("1.13")) {
            Enum<?> NEVER = NMS.getEnumValueOf("FluidCollisionOption", "NEVER");
            arguments = new Object[] { vec3d1, vec3d2, NEVER, false, false };
        } else {
            arguments = new Object[] { vec3d1, vec3d2, false };
        }
        World world = player.getWorld();
        Object nmsWorld = CB.invokeMethod(world, "CraftWorld", "getHandle");
        Object rayTrace = NMS.invokeMethod(nmsWorld, "World", "rayTrace", arguments);
        if (rayTrace != null) {
            Object direction = NMS.getFieldValue("MovingObjectPosition", "direction", rayTrace);
            Object position = NMS.invokeMethod(rayTrace, "MovingObjectPosition", "a");
            int bx = (int) NMS.invokeMethod(position, "BaseBlockPosition", "getX");
            int by = (int) NMS.invokeMethod(position, "BaseBlockPosition", "getY");
            int bz = (int) NMS.invokeMethod(position, "BaseBlockPosition", "getZ");
            return new RayResult(world.getBlockAt(bx, by, bz), BlockFace.valueOf(((Enum<?>) direction).name()));
        }
        return null;
    }

    // 定数にすることでインスタンスの生成を高速化
    private static final Object[] ARGMENT_ENTITY = { false, false };
    private static final Class<?>[] ARGMENT_ENTITY_CLASS = { boolean.class, boolean.class };

    @NotNull
    public static Entity[] selectEntities(@NotNull CommandSender sender, @NotNull Location location, @NotNull String selector) throws ReflectiveOperationException {
        Object vector = toVec3D(location.toVector());
        Object reader = MJN.newInstance("StringReader", selector);
        Object entity = NMS.newInstance(true, "ArgumentEntity", ARGMENT_ENTITY_CLASS, ARGMENT_ENTITY);
        Object wrapper = NMS.invokeMethod(getICommandListener(sender), "CommandListenerWrapper", "a", vector);
        Object parse = NMS.invokeMethod(entity, "ArgumentEntity", "parse", Utils.isCBXXXorLater("1.13.2") ? new Object[] { reader, true } : new Object[] { reader });
        List<?> nmsList = (List<?>) NMS.invokeMethod(parse, "EntitySelector", Utils.isCBXXXorLater("1.14") ? "getEntities" : "b", wrapper);
        return StreamUtils.toArray(nmsList, e -> getBukkitEntity(e), new Entity[nmsList.size()]);
    }

    @Nullable
    private static Entity getBukkitEntity(@NotNull Object nmsEntity) {
        try {
            return (Entity) NMS.invokeMethod(nmsEntity, "Entity", "getBukkitEntity");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public static Object getICommandListener(@NotNull CommandSender sender) throws ReflectiveOperationException {
        if (Utils.isCBXXXorLater("1.13.2")) {
            return CB_COMMAND.getMethod("VanillaCommandWrapper", "getListener", CommandSender.class).invoke(null, sender);
        }
        if (sender instanceof SBPlayer) {
            sender = ((SBPlayer) sender).getPlayer();
        }
        if (sender instanceof Player) {
            Object entity = CB_ENTITY.invokeMethod(sender, "CraftPlayer", "getHandle");
            return NMS.invokeMethod(entity, "Entity", "getCommandListener");
        } else if (sender instanceof BlockCommandSender) {
            return CB_COMMAND.invokeMethod(sender, "CraftBlockCommandSender", "getWrapper");
        } else if (sender instanceof CommandMinecart) {
            Object cart = CB_ENTITY.invokeMethod(sender, "CraftMinecartCommand", "getHandle");
            Object command = NMS.invokeMethod(cart, "EntityMinecartCommandBlock", "getCommandBlock");
            return NMS.invokeMethod(command, "CommandBlockListenerAbstract", "getWrapper");
        } else if (sender instanceof RemoteConsoleCommandSender) {
            Object server = NMS.invokeMethod(null, "MinecraftServer", "getServer");
            Object remote = NMS.getFieldValue("DedicatedServer", "remoteControlCommandListener", server);
            return NMS.invokeMethod(remote, "RemoteControlCommandListener", "f");
        } else if (sender instanceof ConsoleCommandSender) {
            Object server = CB.invokeMethod(sender.getServer(), "CraftServer", "getServer");
            return NMS.invokeMethod(server, "MinecraftServer", "getServerCommandListener");
        } else if (sender instanceof ProxiedCommandSender) {
            return CB_COMMAND.invokeMethod(sender, "ProxiedNativeCommandSender", "getHandle");
        }
        throw new IllegalArgumentException("Cannot make " + sender + " a vanilla command listener");
    }

    @NotNull
    public static Object getAxisAlignedBB(@NotNull Block block) throws ReflectiveOperationException {
        Object world = CB.invokeMethod(block.getWorld(), "CraftWorld", "getHandle");
        Object position = NMS.newInstance("BlockPosition", block.getX(), block.getY(), block.getZ());
        Object blockData = NMS.invokeMethod(world, "WorldServer", "getType", position);
        if (Utils.isCBXXXorLater("1.13")) {
            String name = Utils.getPackageVersion().equals("v1_13_R2") ? "i" : "g";
            Method voxelShape = NMS.getMethod("IBlockData", name, NMS.getClass("IBlockAccess"), position.getClass());
            return NMS.invokeMethod(voxelShape.invoke(blockData, world, position), "VoxelShape", "a");
        } else {
            String name = Utils.isCBXXXorLater("1.11") ? "b" : "a";
            Method axisAlignedBB = NMS.getMethod("Block", name, NMS.getClass("IBlockData"), NMS.getClass("IBlockAccess"), position.getClass());
            return axisAlignedBB.invoke(NMS.invokeMethod(blockData, "IBlockData", "getBlock"), blockData, world, position);
        }
    }

    @NotNull
    public static Map<String, Material> getItemRegistry() throws ReflectiveOperationException {
        Map<String, Material> materials = new HashMap<>();
        Object registory = NMS.getFieldValue("Item", "REGISTRY", null);
        Map<?, ?> registoryMap = (Map<?, ?>) NMS.getFieldValue(true, "RegistrySimple", "c", registory);
        Method bukkitMaterial = CB_UTIL.getMethod("CraftMagicNumbers", "getMaterial", NMS.getClass("Item"));
        for (Entry<?, ?> entry : registoryMap.entrySet()) {
            materials.put(getKey(entry.getKey()), (Material) bukkitMaterial.invoke(null, entry.getValue()));
        }
        return materials;
    }

    @NotNull
    private static String getKey(@NotNull Object minecraftKey) throws ReflectiveOperationException {
        String name = Utils.isCBXXXorLater("1.12") ? "getKey" : "a";
        return (String) NMS.invokeMethod(minecraftKey, "MinecraftKey", name);
    }

    @NotNull
    private static Object toVec3D(@NotNull Vector vector) throws ReflectiveOperationException {
        return newVec3D(vector.getBlockX() + 0.5D, vector.getBlockY(), vector.getBlockZ() + 0.5D);
    }

    @NotNull
    public static Object newVec3D(double x, double y, double z) throws ReflectiveOperationException {
        return NMS.newInstance("Vec3D", x, y, z);
    }
}