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
package com.github.yuttyann.scriptblockplus.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.yuttyann.scriptblockplus.enums.splittype.Argment;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * ScriptBlockPlus EntitySelector クラス
 * <p>
 * NMSを使用せずに"Minecraft 1.12.2"までのセレクターを再現します。
 * <p>
 * zombiestriker(PsudoCommands)様から一部メソッドを引用させていただきました。
 * @author yuttyann44581
 */
public final class EntitySelector {

    @NotNull
    public static List<Entity> getEntities(@NotNull CommandSender sender, @Nullable Location start, @NotNull String selector) {
        List<Entity> result = new ArrayList<>();
        Location location = setCenter(copy(sender, start));
        Split split = new Split(selector, "@", "[", "]");
        SplitValue[] splitValues = split.getValues(Argment.values());
        switch (split.getName()) {
            case "@p": {
                List<Player> players = location.getWorld().getPlayers();
                if (players.size() == 0) {
                    return Collections.emptyList();
                }
                int count = 0;
                int limit = sort(getLimit(splitValues, 1), location, players);
                for (Player player : players) {
                    if (!StreamUtils.allMatch(splitValues, t -> canBeAccepted(player, location, t))) {
                        continue;
                    }
                    if (limit <= count) {
                        break;
                    }
                    count++;
                    result.add(player);
                }
                break;
            }
            case "@a": {
                List<Player> players = location.getWorld().getPlayers();
                if (players.size() == 0) {
                    return Collections.emptyList();
                }
                int count = 0;
                int limit = sort(getLimit(splitValues, players.size()), location, players);
                for (Player player : players) {
                    if (!StreamUtils.allMatch(splitValues, t -> canBeAccepted(player, location, t))) {
                        continue;
                    }
                    if (limit <= count) {
                        break;
                    }
                    count++;
                    result.add(player);
                }
                break;
            }
            case "@r": {
                List<Player> players = location.getWorld().getPlayers();
                if (players.size() == 0) {
                    return Collections.emptyList();
                }
                int count = 0;
                int limit = getLimit(splitValues, 1);
                List<Integer> randomInts = IntStream.range(0, players.size()).boxed().collect(Collectors.toList());
                Collections.shuffle(randomInts, new Random());
                for (int value : randomInts) {
                    Player player = players.get(value);
                    if (!StreamUtils.allMatch(splitValues, t -> canBeAccepted(player, location, t))) {
                        continue;
                    }
                    if (limit <= count) {
                        break;
                    }
                    count++;
                    result.add(player);
                }
                break;
            }
            case "@e": {
                List<Entity> entities = location.getWorld().getEntities();
                if (entities.size() == 0) {
                    return Collections.emptyList();
                }
                int count = 0;
                int limit = sort(getLimit(splitValues, entities.size()), location, entities);
                for (Entity entity : entities) {
                    if (!StreamUtils.allMatch(splitValues, t -> canBeAccepted(entity, location, t))) {
                        continue;
                    }
                    if (limit <= count) {
                        break;
                    }
                    count++;
                    result.add(entity);
                }
                break;
            }
            case "@s": {
                if (sender instanceof Entity && StreamUtils.allMatch(splitValues, t -> canBeAccepted((Entity) sender, location, t))) {
                    result.add((Entity) sender);
                }
                break;
            }
            default:
                return Collections.emptyList();
        }
        return result.size() > 0 ? result : Collections.emptyList();
    }

    @NotNull
    private static Location setCenter(@NotNull Location location) {
        location.setX(location.getBlockX() + 0.5D);
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ() + 0.5D);
        return location;
    }

    @NotNull
    public static Location copy(@NotNull CommandSender sender, @Nullable Location location) {
        if (location == null) {
            if (sender instanceof Entity) {
                location = ((Entity) sender).getLocation();
            } else if (sender instanceof BlockCommandSender) {
                location = ((BlockCommandSender) sender).getBlock().getLocation().add(0.5D, 0.0D, 0.5D);
            }
        }
        Validate.notNull(location, "location");
        return new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    private static int sort(int limit, @NotNull Location location, @NotNull List<? extends Entity> entities) {
        Comparator<Double> order = null;
        if (limit >= 0) {
            order = Comparator.naturalOrder();
        } else {
            order = Comparator.reverseOrder();
            limit = -limit;
        }
        entities.sort(Comparator.comparing((Entity e) -> e.getLocation().distance(location), order).thenComparing(Entity::getTicksLived));
        return limit;
    }

    private static int getLimit(@NotNull SplitValue[] splitValues, int other) {
        for (SplitValue argmentValue : splitValues) {
            if (argmentValue.getType() == Argment.C) {
                return Integer.parseInt(argmentValue.getValue());
            }
        }
        return other;
    }

    private static boolean canBeAccepted(@NotNull Entity entity, @NotNull Location location, @NotNull SplitValue argmentValue) {
        if (argmentValue.getValue()== null) {
            return false;
        }
        switch ((Argment) argmentValue.getType()) {
            case C:
                return true;
            case X:
            case Y:
            case Z:
                return setXYZ(location, argmentValue);
            case DX:
            case DY:
            case DZ:
                return isDRange(entity, location, argmentValue);
            case R:
            case RM:
                return isR(entity, location, argmentValue);
            case RX:
            case RXM:
                return isRX(entity, argmentValue);
            case RY:
            case RYM:
                return isRY(entity, argmentValue);
            case L:
            case LM:
                return isL(entity, argmentValue);
            case M:
                return isM(entity, argmentValue);
            case TAG:
                return isTag(entity, argmentValue);
            case TEAM:
                return isTeam(entity, argmentValue);
            case TYPE:
                return isType(entity, argmentValue);
            case NAME:
                return isName(entity, argmentValue);
            case SCORE:
            case SCORE_MIN:
                return isScore(entity, argmentValue);
            default:
                return false;
        }
    }

    private static boolean setXYZ(@NotNull Location location, @NotNull SplitValue argmentValue) {
        switch ((Argment) argmentValue.getType()) {
            case X:
                setLocation(location, "x", argmentValue.getValue());
                break;
            case Y:
                setLocation(location, "y", argmentValue.getValue());
                break;
            case Z:
                setLocation(location, "z", argmentValue.getValue());
                break;
            default:
                return false;
        }
        return true;
    }

    public static void setLocation(@NotNull Location location, @NotNull String axes, @NotNull String value) {
        if (value.startsWith("^")) {
            double number = value.length() == 1 ? 0.0D : Double.parseDouble(value.substring(1));
            switch (axes.toLowerCase(Locale.ROOT)) {
                case "x": {
                    Location empty = new Location(location.getWorld(), 0.0D, 0.0D, 0.0D);
                    empty.setYaw(normalizeYaw(location.getYaw() - 90.0F));
                    empty.setPitch(location.getPitch());
                    location.add(empty.getDirection().normalize().multiply(number));
                    break;
                }
                case "y": {
                    Location empty = new Location(location.getWorld(), 0.0D, 0.0D, 0.0D);
                    empty.setYaw(location.getYaw());
                    empty.setPitch(location.getPitch() - 90.0F);
                    location.add(empty.getDirection().normalize().multiply(number));
                    break;
                }
                case "z":
                    location.add(location.getDirection().normalize().multiply(number));
                    break;
            }
        } else if (value.startsWith("~")) {
            double number = value.length() == 1 ? 0.0D : Double.parseDouble(value.substring(1));
            switch (axes.toLowerCase(Locale.ROOT)) {
                case "x":
                    location.add(number, 0.0D, 0.0D);
                    break;
                case "y":
                    location.add(0.0D, number, 0.0D);
                    break;
                case "z":
                    location.add(0.0D, 0.0D, number);
                    break;
            }
        } else {
            double number = Double.parseDouble(value);
            switch (axes.toLowerCase(Locale.ROOT)) {
                case "x":
                    location.setX(number);
                    break;
                case "y":
                    location.setY(number);
                    break;
                case "z":
                    location.setZ(number);
                    break;
            }
        }
    }

    private static float normalizeYaw(float yaw) {
        yaw %= 360.0F;
        if (yaw >= 180.0F) {
            yaw -= 360.0F;
        } else if (yaw < -180.0F) {
            yaw += 360.0F;
        }
        return yaw;
    }
    
    private static boolean isDRange(@NotNull Entity entity, @NotNull Location location, @NotNull SplitValue argmentValue) {
        if (!entity.getWorld().equals(location.getWorld())) {
            return false;
        }
        double base = 0.0D, value = 0.0D;
        switch ((Argment) argmentValue.getType()) {
            case DX:
                base = location.getX();
                value = entity.getLocation().getX();
                break;
            case DY:
                base = location.getY();
                value = entity.getLocation().getY();
                break;
            case DZ:
                base = location.getZ();
                value = entity.getLocation().getZ();
                break;
            default:
                return false;
        }
        return value > (base - 0.35D) && value < (base + Double.parseDouble(argmentValue.getValue()) + 1.35D);
    }

    private static boolean isR(@NotNull Entity entity, @NotNull Location location, @NotNull SplitValue argmentValue) {
        if (!entity.getWorld().equals(location.getWorld())) {
            return false;
        }
        if (argmentValue.getType() == Argment.R) {
            return isLessThan(argmentValue, location.distance(entity.getLocation()));
        }
        return isGreaterThan(argmentValue, location.distance(entity.getLocation()));
    }

    private static boolean isRX(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        if (argmentValue.getType() == Argment.RX) {
            return isGreaterThan(argmentValue, entity.getLocation().getYaw());
        }
        return isLessThan(argmentValue, entity.getLocation().getYaw());
    }

    private static boolean isRY(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        if (argmentValue.getType() == Argment.RY) {
            return isGreaterThan(argmentValue, entity.getLocation().getPitch());
        }
        return isLessThan(argmentValue, entity.getLocation().getPitch());
    }

    private static boolean isL(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        if (entity instanceof Player) {
            if (argmentValue.getType() == Argment.L) {
                return isLessThan(argmentValue, ((Player) entity).getTotalExperience());
            }
            return isGreaterThan(argmentValue, ((Player) entity).getTotalExperience());
        }
        return false;
    }

    private static boolean isLessThan(@NotNull SplitValue argmentValue, double value) {
        return argmentValue.isInverted() != value < Double.parseDouble(argmentValue.getValue());
    }

    private static boolean isGreaterThan(@NotNull SplitValue argmentValue, double value) {
        return argmentValue.isInverted() != value > Double.parseDouble(argmentValue.getValue());
    }

    private static boolean isM(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        if (entity instanceof HumanEntity) {
            String value = argmentValue.getValue();
            HumanEntity human = (HumanEntity) entity;
            return argmentValue.isInverted() != (getMode(value) == human.getGameMode());
        }
        return false;
    }

    @Nullable
    private static GameMode getMode(@NotNull String value) {
        if (value.equalsIgnoreCase("0") || value.equalsIgnoreCase("s") || value.equalsIgnoreCase("survival")) {
            return GameMode.SURVIVAL;
        }
        if (value.equalsIgnoreCase("1") || value.equalsIgnoreCase("c") || value.equalsIgnoreCase("creative")) {
            return GameMode.CREATIVE;
        }
        if (value.equalsIgnoreCase("2") || value.equalsIgnoreCase("a") || value.equalsIgnoreCase("adventure")) {
            return GameMode.ADVENTURE;
        }
        if (value.equalsIgnoreCase("3") || value.equalsIgnoreCase("sp") || value.equalsIgnoreCase("spectator")) {
            return GameMode.SPECTATOR;
        }
        return null;
    }

    private static boolean isTag(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        return argmentValue.isInverted() != entity.getScoreboardTags().contains(argmentValue.getValue());
    }

    private static boolean isTeam(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            if (!team.getName().equals(argmentValue.getValue())) {
                continue;
            }
            String entry = entity instanceof Player ? entity.getName() : entity.getUniqueId().toString();
            if (argmentValue.isInverted() != team.getEntries().contains(entry)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isType(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        return argmentValue.isInverted() != (entity.getType() == getEntityType(argmentValue.getValue()));
    }

    @NotNull
    @SuppressWarnings("deprecation")
    public static EntityType getEntityType(@NotNull String name) {
        name = StringUtils.removeStart(name, Utils.MINECRAFT);
        name = name.replaceAll("\\s+", "_").replaceAll("\\W", "");
        for (EntityType entityType : EntityType.values()) {
            if (name.equalsIgnoreCase(entityType.name()) || name.equalsIgnoreCase(entityType.getName())) {
                return entityType;
            }
        }
        return null;
    }

    private static boolean isName(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        if (entity instanceof Player) {
            return argmentValue.isInverted() != argmentValue.getValue().equals(entity.getName());
        } else {
            return argmentValue.isInverted() != argmentValue.getValue().equals(entity.getCustomName());
        }
    }

    private static boolean isScore(@NotNull Entity entity, @NotNull SplitValue argmentValue) {
        List<String> split = StringUtils.split(argmentValue.getValue(), '*');
        boolean scoreArgment = argmentValue.getType() == Argment.SCORE;
        for (Objective objective : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
            if (!objective.getName().equals(split.get(1))) {
                continue;
            }
            int score = objective.getScore(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()).getScore();
            if (argmentValue.isInverted() != (scoreArgment ? score <= Integer.parseInt(split.get(1)) : score >= Integer.parseInt(split.get(0)))) {
                return true;
            }
        }
        return false;
    }
}