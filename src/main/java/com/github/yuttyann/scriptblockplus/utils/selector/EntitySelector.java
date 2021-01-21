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
package com.github.yuttyann.scriptblockplus.utils.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.yuttyann.scriptblockplus.enums.Argment;
import com.github.yuttyann.scriptblockplus.utils.StreamUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
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

    private static final Entity[] EMPTY_ENTITY_ARRAY = new Entity[0];

    @NotNull
    public static Entity[] getEntities(@NotNull CommandSender sender, @Nullable Location start, @NotNull String argment) {
        List<Entity> result = new ArrayList<>();
        Location location = copy(sender, start);
        Selector selector = new Selector(argment);
        ArgmentValue[] argments = selector.getArgments();
        switch (selector.getSelector()) {
            case "@p": {
                List<Player> players = location.getWorld().getPlayers();
                if (players.size() == 0) {
                    return EMPTY_ENTITY_ARRAY;
                }
                int count = 0;
                int limit = sort(getLimit(argments, 1), location, players);
                for (Player player : players) {
                    if (!StreamUtils.allMatch(argments, t -> canBeAccepted(player, location, t))) {
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
                    return EMPTY_ENTITY_ARRAY;
                }
                int count = 0;
                int limit = sort(getLimit(argments, players.size()), location, players);
                for (Player player : players) {
                    if (!StreamUtils.allMatch(argments, t -> canBeAccepted(player, location, t))) {
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
                    return EMPTY_ENTITY_ARRAY;
                }
                int count = 0;
                int limit = getLimit(argments, 1);
                List<Integer> randomInts = IntStream.range(0, players.size()).boxed().collect(Collectors.toList());
                Collections.shuffle(randomInts, new Random());
                for (int value : randomInts) {
                    Player player = players.get(value);
                    if (!StreamUtils.allMatch(argments, t -> canBeAccepted(player, location, t))) {
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
                    return EMPTY_ENTITY_ARRAY;
                }
                int count = 0;
                int limit = sort(getLimit(argments, entities.size()), location, entities);
                for (Entity entity : entities) {
                    if (!StreamUtils.allMatch(argments, t -> canBeAccepted(entity, location, t))) {
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
                if (sender instanceof Entity && StreamUtils.allMatch(argments, t -> canBeAccepted((Entity) sender, location, t))) {
                    result.add((Entity) sender);
                }
                break;
            }
            default:
                return EMPTY_ENTITY_ARRAY;
        }
        return result.size() > 0 ? result.toArray(new Entity[0]) : EMPTY_ENTITY_ARRAY;
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

    @NotNull
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

    private static int getLimit(@NotNull ArgmentValue[] argments, int other) {
        for (ArgmentValue argmentValue : argments) {
            if (argmentValue.getArgment() == Argment.C) {
                return Integer.parseInt(argmentValue.getValue());
            }
        }
        return other;
    }

    private static boolean canBeAccepted(@NotNull Entity entity, @NotNull Location location, @NotNull ArgmentValue argmentValue) {
        if (argmentValue.getValue()== null) {
            return false;
        }
        switch (argmentValue.getArgment()) {
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

    private static boolean setXYZ(@NotNull Location location, @NotNull ArgmentValue argmentValue) {
        switch (argmentValue.getArgment()) {
            case X:
                location.add(getRelative(location, "x", argmentValue.getValue()));
                break;
            case Y:
                location.add(getRelative(location, "y", argmentValue.getValue()));
                break;
            case Z:
                location.add(getRelative(location, "z", argmentValue.getValue()));
                break;
            default:
                return false;
        }
        return true;
    }

    @NotNull
    public static Vector getRelative(@NotNull Location location, @NotNull String relative, @NotNull String value) {
        if (value.startsWith("^")) {
            double number = value.length() == 1 ? 0.0D : Double.parseDouble(value.substring(1));
            switch (relative.toLowerCase(Locale.ROOT)) {
                case "x": {
                    Location empty = new Location(location.getWorld(), 0, 0, 0);
                    empty.setYaw(normalizeYaw(location.getYaw() - 90));
                    empty.setPitch(location.getPitch());
                    return empty.getDirection().normalize().multiply(number);
                }
                case "y": {
                    Location empty = new Location(location.getWorld(), 0, 0, 0);
                    empty.setYaw(location.getYaw());
                    empty.setPitch(location.getPitch() - 90);
                    return empty.getDirection().normalize().multiply(number);
                }
                case "z":
                    return location.getDirection().normalize().multiply(number);
                default:
                    return new Vector();
            }
        } else {
            double number = 0.0D;
            if (!value.startsWith("~")) {
                number = Double.parseDouble(value);
            } else if (value.length() > 1) {
                number = Double.parseDouble(value.substring(1));
            }
            switch (relative.toLowerCase(Locale.ROOT)) {
                case "x":
                    return new Vector(number, 0, 0);
                case "y":
                    return new Vector(0, number, 0);
                case "z":
                    return new Vector(0, 0, number);
                default:
                    return new Vector();
            }
        }
    }

    private static float normalizeYaw(float yaw) {
        yaw %= 360.0f;
        if (yaw >= 180.0f) {
            yaw -= 360.0f;
        } else if (yaw < -180.0f) {
            yaw += 360.0f;
        }
        return yaw;
    }
    
    private static boolean isDRange(@NotNull Entity entity, @NotNull Location location, @NotNull ArgmentValue argmentValue) {
        if (!entity.getWorld().equals(location.getWorld())) {
            return false;
        }
        double base = 0.0D, value = 0.0D;
        switch (argmentValue.getArgment()) {
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

    private static boolean isR(@NotNull Entity entity, @NotNull Location location, @NotNull ArgmentValue argmentValue) {
        if (!entity.getWorld().equals(location.getWorld())) {
            return false;
        }
        if (argmentValue.getArgment() == Argment.R) {
            return isLessThan(argmentValue, location.distance(entity.getLocation()));
        }
        return isGreaterThan(argmentValue, location.distance(entity.getLocation()));
    }

    private static boolean isRX(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        if (argmentValue.getArgment() == Argment.RX) {
            return isGreaterThan(argmentValue, entity.getLocation().getYaw());
        }
        return isLessThan(argmentValue, entity.getLocation().getYaw());
    }

    private static boolean isRY(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        if (argmentValue.getArgment() == Argment.RY) {
            return isGreaterThan(argmentValue, entity.getLocation().getPitch());
        }
        return isLessThan(argmentValue, entity.getLocation().getPitch());
    }

    private static boolean isL(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        if (entity instanceof Player) {
            if (argmentValue.getArgment() == Argment.L) {
                return isLessThan(argmentValue, ((Player) entity).getTotalExperience());
            }
            return isGreaterThan(argmentValue, ((Player) entity).getTotalExperience());
        }
        return false;
    }

    private static boolean isLessThan(@NotNull ArgmentValue argmentValue, double value) {
        return (value < Double.parseDouble(argmentValue.getValue())) != argmentValue.isInverted();
    }

    private static boolean isGreaterThan(@NotNull ArgmentValue argmentValue, double value) {
        return (value > Double.parseDouble(argmentValue.getValue())) != argmentValue.isInverted();
    }

    private static boolean isM(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        if (entity instanceof HumanEntity) {
            String value = argmentValue.getValue();
            HumanEntity human = (HumanEntity) entity;
            return argmentValue.isInverted() != (getMode(value) == human.getGameMode());
        }
        return false;
    }

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

    private static boolean isTag(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        return argmentValue.isInverted() != entity.getScoreboardTags().contains(argmentValue.getValue());
    }

    private static boolean isTeam(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
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

    private static boolean isType(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        return argmentValue.isInverted() != entity.getType().name().equalsIgnoreCase(argmentValue.getValue());
    }

    private static boolean isName(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        if (entity instanceof Player) {
            return argmentValue.isInverted() != argmentValue.getValue().equals(entity.getName());
        } else {
            return argmentValue.isInverted() != argmentValue.getValue().equals(entity.getCustomName());
        }
    }

    private static boolean isScore(@NotNull Entity entity, @NotNull ArgmentValue argmentValue) {
        String[] array = StringUtils.split(argmentValue.getValue(), '*');
        boolean isScore = argmentValue.getArgment() == Argment.SCORE;
        for (Objective objective : Bukkit.getScoreboardManager().getMainScoreboard().getObjectives()) {
            if (!objective.getName().equals(array[1])) {
                continue;
            }
            int score = objective.getScore(entity instanceof Player ? entity.getName() : entity.getUniqueId().toString()).getScore();
            if (argmentValue.isInverted() != (isScore ? score <= Integer.parseInt(array[0]) : score >= Integer.parseInt(array[0]))) {
                return true;
            }
        }
        return false;
    }
}