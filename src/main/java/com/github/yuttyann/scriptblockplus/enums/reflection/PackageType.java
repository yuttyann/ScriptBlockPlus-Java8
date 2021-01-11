package com.github.yuttyann.scriptblockplus.enums.reflection;

import com.github.yuttyann.scriptblockplus.raytrace.RayResult;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * ScriptBlockPlus PackageType 列挙型
 * @author yuttyann44581
 */
public enum PackageType {
    NMS("net.minecraft.server." + Utils.getPackageVersion()),
    CB("org.bukkit.craftbukkit." + Utils.getPackageVersion()),
    CB_BLOCK(CB, "block"),
    CB_CHUNKIO(CB, "chunkio"),
    CB_COMMAND(CB, "command"),
    CB_CONVERSATIONS(CB, "conversations"),
    CB_ENCHANTMENS(CB, "enchantments"),
    CB_ENTITY(CB, "entity"),
    CB_EVENT(CB, "event"),
    CB_GENERATOR(CB, "generator"),
    CB_HELP(CB, "help"),
    CB_INVENTORY(CB, "inventory"),
    CB_MAP(CB, "map"),
    CB_METADATA(CB, "metadata"),
    CB_POTION(CB, "potion"),
    CB_PROJECTILES(CB, "projectiles"),
    CB_SCHEDULER(CB, "scheduler"),
    CB_SCOREBOARD(CB, "scoreboard"),
    CB_UPDATER(CB, "updater"),
    CB_UTIL(CB, "util");

    private enum ReturnType {
        CLASS("class_"),
        FIELD("field_"),
        METHOD("method_"),
        CONSTRUCTOR("constructor_");

        private final String name;

        ReturnType(@NotNull String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static final boolean HAS_NMS;

    static {
        boolean hasNMS;
        try {
            Class.forName(NMS + ".Entity");
            hasNMS = true;
        } catch (ClassNotFoundException e) {
            hasNMS = false;
        }
        HAS_NMS = hasNMS;
    }

    private static final Map<String, Object> CACHE = new HashMap<>();

    private final String path;

    PackageType(@NotNull String path) {
        this.path = path;
    }

    PackageType(@NotNull PackageType parent, @NotNull String path) {
        this(parent + "." + path);
    }

    public static void clear() {
        CACHE.clear();
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @Override
    @NotNull
    public String toString() {
        return path;
    }

    public void setFieldValue(@NotNull String className, @NotNull String fieldName, @Nullable Object instance, @Nullable Object value) throws ReflectiveOperationException {
        getField(false, className, fieldName).set(instance, value);
    }

    public void setFieldValue(boolean declared, @NotNull String className, @NotNull String fieldName, @Nullable Object instance, @Nullable Object value) throws ReflectiveOperationException {
        if (StringUtils.isEmpty(className)) {
            className = instance.getClass().getSimpleName();
        }
        getField(declared, className, fieldName).set(instance, value);
    }

    @Nullable
    public Field getField(@NotNull String className, @NotNull String fieldName) throws ReflectiveOperationException {
        return getField(false, className, fieldName);
    }

    @Nullable
    public Field getField(boolean declared, @NotNull String className, @NotNull String fieldName) throws ReflectiveOperationException {
        String key = createKey(ReturnType.FIELD, className, fieldName, null);
        Field field = (Field) CACHE.get(key);
        if (field == null) {
            if (declared) {
                field = getClass(className).getDeclaredField(fieldName);
                field.setAccessible(true);
            } else {
                field = getClass(className).getField(fieldName);
            }
            CACHE.put(key, field);
        }
        return field;
    }

    @Nullable
    public Object invokeMethod(@Nullable Object instance, @NotNull String className, @NotNull String methodName) throws ReflectiveOperationException {
        return invokeMethod(false, instance, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Nullable
    public Object invokeMethod(@Nullable Object instance, @NotNull String className, @NotNull String methodName, @Nullable Object... arguments) throws ReflectiveOperationException {
        return invokeMethod(false, instance, className, methodName, arguments);
    }

    @Nullable
    public Object invokeMethod(boolean declared, @Nullable Object instance, @NotNull String className, @NotNull String methodName, @Nullable Object... arguments) throws ReflectiveOperationException {
        if (StringUtils.isEmpty(className)) {
            className = instance.getClass().getSimpleName();
        }
        if (arguments == null) {
            arguments = ArrayUtils.EMPTY_OBJECT_ARRAY;
        }
        return getMethod(declared, className, methodName, ClassType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    @Nullable
    public Method getMethod(@NotNull String className, @NotNull String methodName) throws ReflectiveOperationException {
        return getMethod(false, className, methodName, ArrayUtils.EMPTY_CLASS_ARRAY);
    }

    @Nullable
    public Method getMethod(@NotNull String className, @NotNull String methodName, @Nullable Class<?>... parameterTypes) throws ReflectiveOperationException {
        return getMethod(false, className, methodName, parameterTypes);
    }

    @Nullable
    public Method getMethod(boolean declared, @NotNull String className, @NotNull String methodName, @Nullable Class<?>... parameterTypes) throws ReflectiveOperationException {
        if (parameterTypes == null) {
            parameterTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        String key = createKey(ReturnType.METHOD, className, methodName, parameterTypes);
        Method method = (Method) CACHE.get(key);
        if (method == null) {
            if (declared) {
                method = getClass(className).getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
            } else {
                method = getClass(className).getMethod(methodName, parameterTypes);
            }
            CACHE.put(key, method);
        }
        return method;
    }

    @Nullable
    public Object newInstance(@NotNull String className) throws ReflectiveOperationException {
        return newInstance(false, className, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    @Nullable
    public Object newInstance(@NotNull String className, @Nullable Object... arguments) throws ReflectiveOperationException {
        return newInstance(false, className, arguments);
    }

    @Nullable
    public Object newInstance(boolean declared, @NotNull String className, @Nullable Object... arguments) throws ReflectiveOperationException {
        if (arguments == null || arguments.length == 0) {
            return getClass(className).getConstructor(ArrayUtils.EMPTY_CLASS_ARRAY).newInstance();
        }
        return getConstructor(declared, className, ClassType.getPrimitive(arguments)).newInstance(arguments);
    }

    @Nullable
    public Constructor<?> getConstructor(@NotNull String className) throws ReflectiveOperationException {
        return getConstructor(false, className, ArrayUtils.EMPTY_CLASS_ARRAY);
    }

    @Nullable
    public Constructor<?> getConstructor(@NotNull String className, @Nullable Class<?>... parameterTypes) throws ReflectiveOperationException {
        return getConstructor(false, className, parameterTypes);
    }

    @Nullable
    public Constructor<?> getConstructor(boolean declared, @NotNull String className, @Nullable Class<?>... parameterTypes) throws ReflectiveOperationException {
        if (parameterTypes == null) {
            parameterTypes = ArrayUtils.EMPTY_CLASS_ARRAY;
        }
        String key = createKey(ReturnType.CONSTRUCTOR, className, null, parameterTypes);
        Constructor<?> constructor = (Constructor<?>) CACHE.get(key);
        if (constructor == null) {
            if (declared) {
                constructor = getClass(className).getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
            } else {
                constructor = getClass(className).getConstructor(parameterTypes);
            }
            CACHE.put(key, constructor);
        }
        return constructor;
    }

    @Nullable
    public Enum<?> getEnumValueOf(@NotNull String className, @NotNull String name) throws ReflectiveOperationException {
        Class<?> clazz = getClass(className);
        return clazz.isEnum() ? (Enum<?>) getMethod(className, "valueOf", String.class).invoke(null, name) : null;
    }

    @Nullable
    public Class<?> getClass(@NotNull String className) throws IllegalArgumentException, ClassNotFoundException {
        if (!HAS_NMS) {
            throw new UnsupportedOperationException("NMS not found.");
        }
        if (StringUtils.isEmpty(className)) {
            throw new IllegalArgumentException();
        }
        String pass = this + "." + className;
        String key = ReturnType.CLASS + pass;
        Class<?> clazz = (Class<?>) CACHE.get(key);
        if (clazz == null) {
            clazz = Class.forName(pass);
            CACHE.put(key, clazz);
        }
        return clazz;
    }

    @NotNull
    private String createKey(@NotNull ReturnType returnType, @NotNull String className, @Nullable String name, @Nullable Class<?>[] objects) {
        if (!HAS_NMS || StringUtils.isEmpty(className)) {
            return "null";
        }
        String rName = returnType.toString();
        int lastLength = objects == null ? -1 : objects.length - 1;
        if (lastLength == -1) {
            if (name != null) {
                return rName + this + "." + className + "=" + name + "[]";
            }
            return rName + this + "." + className;
        }
        StringBuilder builder = new StringBuilder();
        boolean notEmptyName = StringUtils.isNotEmpty(name);
        builder.append(rName).append(this).append('.').append(className).append(notEmptyName ? '=' : '[');
        if (notEmptyName) {
            builder.append(name).append('[');
        }
        for (int i = 0; i < objects.length; i++) {
            builder.append(objects[i] == null ? null : objects[i].getName());
            if (i == lastLength) {
                return builder.append(']').toString();
            }
            builder.append(',');
        }
        return builder.toString();
    }

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
        String chatSerializer = "IChatBaseComponent$ChatSerializer";
        Object component = NMS.invokeMethod(null, chatSerializer, "a", "{\"text\": \"" + text + "\"}");
        Class<?>[] classes = new Class<?>[] { NMS.getClass("IChatBaseComponent"), byte.class };
        Object value = (byte) 2;
        if (Utils.isCBXXXorLater("1.12")) {
            value = NMS.getEnumValueOf("ChatMessageType", "GAME_INFO");
            classes[1] = value.getClass();
        }
        Class<?> packetClass = NMS.getClass("Packet");
        Object handle = CB_ENTITY.invokeMethod(player, "CraftPlayer", "getHandle");
        Object connection = NMS.getField("EntityPlayer", "playerConnection").get(handle);
        Object packetChat = NMS.getConstructor("PacketPlayOutChat", classes).newInstance(component, value);
        NMS.getMethod("PlayerConnection", "sendPacket", packetClass).invoke(connection, packetChat);
    }

    @Nullable
    public static RayResult rayTraceBlocks(@NotNull Player player, final double distance) throws ReflectiveOperationException {
        Location eyeLocation = player.getEyeLocation();
        Object vec3d1 = toNMSVec3D(eyeLocation.toVector());
        Object vec3d2 = toNMSVec3D(eyeLocation.toVector().add(eyeLocation.getDirection().normalize().multiply(distance)));
        Object[] arguments = (Object[]) null;
        if (Utils.isCBXXXorLater("1.13")) {
            Object NEVER = NMS.getEnumValueOf("FluidCollisionOption", "NEVER");
            arguments = new Object[] { vec3d1, vec3d2, NEVER, false, false };
        } else {
            arguments = new Object[] { vec3d1, vec3d2, false };
        }
        World world = player.getWorld();
        Object nmsWorld = CB.invokeMethod(world, "CraftWorld", "getHandle");
        Object rayTrace = NMS.invokeMethod(nmsWorld, "World", "rayTrace", arguments);
        if (rayTrace != null) {
            Field enumDirection = NMS.getField("MovingObjectPosition", "direction");
            Object position = NMS.invokeMethod(rayTrace, "MovingObjectPosition", "a");
            int x = (int) NMS.invokeMethod(position, "BaseBlockPosition", "getX");
            int y = (int) NMS.invokeMethod(position, "BaseBlockPosition", "getY");
            int z = (int) NMS.invokeMethod(position, "BaseBlockPosition", "getZ");
            return new RayResult(world.getBlockAt(x, y, z), BlockFace.valueOf(((Enum<?>) enumDirection.get(rayTrace)).name()));
        }
        return null;
    }

    @NotNull
    public static Object getAxisAlignedBB(@NotNull Block block) throws ReflectiveOperationException {
        Object world = CB.invokeMethod(block.getWorld(), "CraftWorld", "getHandle");
        Object position = NMS.newInstance("BlockPosition", block.getX(), block.getY(), block.getZ());
        Object blockData = NMS.invokeMethod(world, "WorldServer", "getType", position);
        if (Utils.isCBXXXorLater("1.13")) {
            String name = Utils.getPackageVersion().equals("v1_13_R2") ? "i" : "g";
            Method getVoxelShape = NMS.getMethod("IBlockData", name, NMS.getClass("IBlockAccess"), position.getClass());
            return NMS.invokeMethod(getVoxelShape.invoke(blockData, world, position), "VoxelShape", "a");
        } else {
            String name = Utils.isCBXXXorLater("1.11") ? "b" : "a";
            Method getAxisAlignedBB = NMS.getMethod("Block", name, NMS.getClass("IBlockData"), NMS.getClass("IBlockAccess"), position.getClass());
            return getAxisAlignedBB.invoke(NMS.invokeMethod(blockData, "IBlockData", "getBlock"), blockData, world, position);
        }
    }

    @NotNull
    public static Map<String, Material> getItemRegistry() throws ReflectiveOperationException {
        Map<String, Material> items = new HashMap<>();
        Method material = CB_UTIL.getMethod("CraftMagicNumbers", "getMaterial", NMS.getClass("Item"));
        Object registory = NMS.getField("Item", "REGISTRY").get(null);
        Map<?, ?> registorySimple = (Map<?, ?>) NMS.getField(true, "RegistrySimple", "c").get(registory);
        for (Map.Entry<?, ?> entry : registorySimple.entrySet()) {
            items.put(entry.getKey().toString(), (Material) material.invoke(null, entry.getValue()));
        }
        return items;
    }

    @NotNull
    public static Object toNMSVec3D(@NotNull Vector vector) throws ReflectiveOperationException {
        return NMS.newInstance("Vec3D", vector.getX(), vector.getY(), vector.getZ());
    }
}