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
package com.github.yuttyann.scriptblockplus.command;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.ScriptBlock;
import com.github.yuttyann.scriptblockplus.enums.ActionType;
import com.github.yuttyann.scriptblockplus.enums.Permission;
import com.github.yuttyann.scriptblockplus.enums.Filter;
import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.file.Json;
import com.github.yuttyann.scriptblockplus.file.SBFiles;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.file.config.YamlConfig;
import com.github.yuttyann.scriptblockplus.file.json.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.element.BlockScript;
import com.github.yuttyann.scriptblockplus.file.json.element.ScriptParam;
import com.github.yuttyann.scriptblockplus.item.ItemAction;
import com.github.yuttyann.scriptblockplus.manager.OptionManager;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.region.CuboidRegionPaste;
import com.github.yuttyann.scriptblockplus.region.CuboidRegionRemove;
import com.github.yuttyann.scriptblockplus.region.Region;
import com.github.yuttyann.scriptblockplus.script.SBClipboard;
import com.github.yuttyann.scriptblockplus.script.ScriptEdit;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import com.github.yuttyann.scriptblockplus.utils.*;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ScriptBlockPlus ScriptBlockPlusCommand コマンドクラス
 * @author yuttyann44581
 */
public final class ScriptBlockPlusCommand extends BaseCommand {

    public ScriptBlockPlusCommand(@NotNull ScriptBlock plugin) {
        super(plugin);
    }

    @Override
    public boolean isAliases() {
        return true;
    }

    @NotNull
    @Override
    public CommandData[] getUsages() {
        String[] typeNodes = Permission.getTypeNodes(true);
        return new CommandData[] { new CommandData(SBConfig.TOOL_COMMAND.getValue(), Permission.COMMAND_TOOL.getNode()),
                new CommandData(SBConfig.RELOAD_COMMAND.getValue(), Permission.COMMAND_RELOAD.getNode()),
                new CommandData(SBConfig.BACKUP_COMMAND.getValue(), Permission.COMMAND_BACKUP.getNode()),
                new CommandData(SBConfig.CHECKVER_COMMAND.getValue(), Permission.COMMAND_CHECKVER.getNode()),
                new CommandData(SBConfig.DATAMIGR_COMMAND.getValue(), Permission.COMMAND_DATAMIGR.getNode()),
                new CommandData(SBConfig.EXPORT_COMMAND.getValue(), Permission.COMMAND_EXPORT.getNode()),
                new CommandData(SBConfig.CREATE_COMMAND.getValue(), typeNodes),
                new CommandData(SBConfig.ADD_COMMAND.getValue(), typeNodes),
                new CommandData(SBConfig.REMOVE_COMMAND.getValue(), typeNodes),
                new CommandData(SBConfig.VIEW_COMMAND.getValue(), typeNodes),
                new CommandData(SBConfig.RUN_COMMAND.getValue(), typeNodes),
                new CommandData(SBConfig.REDSTONE_COMMAND.getValue(), typeNodes),
                new CommandData(SBConfig.SELECTOR_PASTE_COMMAND.getValue(), Permission.COMMAND_SELECTOR.getNode()),
                new CommandData(SBConfig.SELECTOR_REMOVE_COMMAND.getValue(), Permission.COMMAND_SELECTOR.getNode()) };
    }

    @Override
    public boolean runCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            String[] args) {
        int length = args.length;
        if (length == 1) {
            if (equals(args[0], "tool")) {
                return doTool(sender);
            } else if (equals(args[0], "reload")) {
                return doReload(sender);
            } else if (equals(args[0], "backup")) {
                try {
                    return doBackup(sender);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (equals(args[0], "checkver")) {
                return doCheckVer(sender);
            } else if (equals(args[0], "datamigr")) {
                return doDataMigr(sender);
            }
        }
        if (length == 2) {
            if (equals(args[0], "export") && equals(args[1], "sound", "material")) {
                return doExport(sender, args);
            } else if (equals(args[0], ScriptKey.types()) && equals(args[1], "remove", "view")) {
                return setAction(sender, args);
            } else if (equals(args[0], "selector") && equals(args[1], "paste", "remove")) {
                return doSelector(sender, args);
            }
        }
        if (length == 3 && equals(args[0], ScriptKey.types()) && equals(args[1], "redstone")
                && equals(args[2], "false")) {
            return setAction(sender, args);
        }
        if (length > 3 && equals(args[0], ScriptKey.types()) && equals(args[1], "redstone")
                && equals(args[2], "true")) {
            return setAction(sender, args);
        }
        if (length > 2) {
            if (length < 5 && equals(args[0], "selector") && equals(args[1], "paste")) {
                return doSelector(sender, args);
            } else if (equals(args[0], ScriptKey.types())) {
                if (length == 6 && equals(args[1], "run")) {
                    return doRun(sender, args);
                } else if (equals(args[1], "create", "add")) {
                    return setAction(sender, args);
                }
            }
        }
        return false;
    }

    private boolean doExport(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, Permission.COMMAND_EXPORT, false) || !equals(args[1], "sound", "material")) {
            return false;
        }
        String type = args[1].toLowerCase(Locale.ROOT);
        String path = "export" + SBFiles.S + type + "_v" + Utils.getServerVersion() + "_.txt";
        File file = new File(getPlugin().getDataFolder(), path);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        new Thread(() -> {
            SBConfig.EXPORT_START.replace(type).send(sender);
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8))) {
                for (Enum<?> value : type.equals("sound") ? Sound.values() : Material.values()) {
                    writer.write(value.name());
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                SBConfig.EXPORT_END.replace(type).send(sender);
            }
        }).start();
        return true;
    }

    private boolean doTool(@NotNull CommandSender sender) {
        if (!hasPermission(sender, Permission.COMMAND_TOOL)) {
            return false;
        }
        Player player = (Player) sender;
        PlayerInventory inventory = player.getInventory();
        ItemAction.getItems().forEach(i -> inventory.addItem(i.getItem().clone()));
        Utils.updateInventory(player);
        SBConfig.GIVE_TOOL.send(player);
        return true;
    }

    private boolean doReload(@NotNull CommandSender sender) {
        if (!hasPermission(sender, Permission.COMMAND_RELOAD, false)) {
            return false;
        }
        Json.clear();
        SBFiles.reload();
        NameFetcher.clear();
        PackageType.clear();
        setUsage(getUsages());
        SBConfig.ALL_FILE_RELOAD.send(sender);
        return true;
    }

    private boolean doBackup(@NotNull CommandSender sender) throws IOException {
        if (!hasPermission(sender, Permission.COMMAND_BACKUP, false)) {
            return false;
        }
        File dataFolder = ScriptBlock.getInstance().getDataFolder();
        if (!dataFolder.exists() || FileUtils.isEmpty(dataFolder)) {
            SBConfig.ERROR_PLUGIN_BACKUP.send(sender);
            return true;
        }
        File backup = new File(dataFolder, "backup");
        Path target = new File(backup, Utils.getFormatTime("yyyy-MM-dd HH-mm-ss")).toPath();
        Path source = dataFolder.toPath();

        // フォルダをコピー（再帰）
        try {
            FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
                    if (!path.toString().contains(SBFiles.S + "backup" + SBFiles.S)) {
                        Path targetFile = target.resolve(source.relativize(path));
                        Path parentDir = targetFile.getParent();
                        Files.createDirectories(parentDir);
                        Files.copy(path, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }
            };
            Files.walkFileTree(source, fileVisitor);
        } finally {
            SBConfig.PLUGIN_BACKUP.send(sender);
        }
        return true;
    }

    private boolean doCheckVer(@NotNull CommandSender sender) {
        if (!hasPermission(sender, Permission.COMMAND_CHECKVER, false)) {
            return false;
        }
        ScriptBlock.getInstance().checkUpdate(sender, true);
        return true;
    }

    private boolean doDataMigr(@NotNull CommandSender sender) {
        if (!hasPermission(sender, Permission.COMMAND_DATAMIGR)) {
            return false;
        }
        String path = "plugins" + SBFiles.S + "ScriptBlock" + SBFiles.S + "BlocksData" + SBFiles.S;
        File interactFile = new File(path + "interact_Scripts.yml");
        File walkFile = new File(path + "walk_Scripts.yml");
        if (!walkFile.exists() && !interactFile.exists()) {
            SBConfig.NOT_SCRIPT_BLOCK_FILE.send(sender);
        } else {
            SBConfig.DATAMIGR_START.send(sender);
            UUID uuid = ((Player) sender).getUniqueId();
            new Thread(() -> {
                try {
                    convart(uuid, interactFile, ScriptKey.INTERACT);
                    convart(uuid, walkFile, ScriptKey.WALK);
                } finally {
                    SBConfig.DATAMIGR_END.send(sender);
                }
            }).start();
        }
        return true;
    }

    private void convart(@NotNull UUID uuid, @NotNull File file, @NotNull ScriptKey scriptKey) {
        if (!file.exists()) {
            return;
        }
        YamlConfig scriptFile = YamlConfig.load(getPlugin(), file, false);
        BlockScriptJson scriptJson = new BlockScriptJson(scriptKey);
        BlockScript blockScript = scriptJson.load();
        for (String name : scriptFile.getKeys()) {
            World world = Utils.getWorld(name);
            for (String coords : scriptFile.getKeys(name)) {
                List<String> script = scriptFile.getStringList(name + "." + coords);
                script.replaceAll(s -> StringUtils.replace(s, "@cooldown:", "@oldcooldown:"));
                if (script.size() > 0 && script.get(0).startsWith("Author:")) {
                    script.remove(0);
                }
                ScriptParam scriptParam = blockScript.get(BlockCoords.fromString(world, coords));
                scriptParam.getAuthor().add(uuid);
                scriptParam.setLastEdit(Utils.getFormatTime(Utils.DATE_PATTERN));
                scriptParam.setScript(script);
            }
        }
        scriptJson.saveFile();
    }

    private boolean doRun(@NotNull CommandSender sender, @NotNull String[] args) {
        ScriptKey scriptKey = ScriptKey.valueOf(args[0]);
        if (!isPlayer(sender) || !Permission.has(sender, scriptKey, true)) {
            return false;
        }
        int x = Integer.parseInt(args[3]), y = Integer.parseInt(args[4]), z = Integer.parseInt(args[5]);
        ScriptBlock.getInstance().getAPI().read((Player) sender, new Location(Utils.getWorld(args[2]), x, y, z), scriptKey, 0);
        return true;
    }

    private boolean setAction(@NotNull CommandSender sender, @NotNull String[] args) {
        ScriptKey scriptKey = ScriptKey.valueOf(args[0]);
        if (!isPlayer(sender) || !Permission.has(sender, scriptKey, true)) {
            return false;
        }
        SBPlayer sbPlayer = SBPlayer.fromPlayer((Player) sender);
        if (sbPlayer.getScriptEdit().isPresent()) {
            SBConfig.ERROR_ACTION_DATA.send(sbPlayer);
            return true;
        }
        ActionType actionType = ActionType.valueOf(args[1].toUpperCase(Locale.ROOT));
        ScriptEdit scriptEdit = new ScriptEdit(scriptKey, actionType);
        if (actionType == ActionType.REDSTONE && equals(args[2], "true")) {
            String selector = StringUtils.createString(args, 3).trim();
            if (selector.startsWith("@s") || !CommandSelector.has(selector)) {
                selector = "@p";
            }
            scriptEdit.setValue(selector);
        } else if (actionType == ActionType.CREATE || actionType == ActionType.ADD) {
            String script = StringUtils.createString(args, 2).trim();
            if (!isScripts(script)) {
                SBConfig.ERROR_SCRIPT_CHECK.send(sbPlayer);
                return true;
            }
            scriptEdit.setValue(script);
        }
        sbPlayer.setScriptEdit(scriptEdit);
        SBConfig.SUCCESS_ACTION_DATA.replace(scriptKey.getName() + "-" + actionType.getName()).send(sbPlayer);
        return true;
    }

    private boolean doSelector(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!hasPermission(sender, Permission.COMMAND_SELECTOR)) {
            return false;
        }
        Player player = (Player) sender;
        Region region = SBPlayer.fromPlayer(player).getRegion();
        if (!region.hasPositions()) {
            SBConfig.NOT_SELECTION.send(sender);
            return true;
        }
        if (equals(args[1], "paste")) {
            SBPlayer sbPlayer = SBPlayer.fromPlayer(player);
            if (!sbPlayer.getSBClipboard().isPresent()) {
                SBConfig.ERROR_SCRIPT_FILE_CHECK.send(sender);
                return true;
            }
            boolean pasteonair = args.length > 2 && Boolean.parseBoolean(args[2]);
            boolean overwrite = args.length > 3 && Boolean.parseBoolean(args[3]);
            try {
                SBClipboard sbClipboard = sbPlayer.getSBClipboard().get();
                CuboidRegionPaste regionPaste = new CuboidRegionPaste(sbClipboard, region).paste(pasteonair, overwrite);
                String scriptKeyName = regionPaste.getScriptKey().getName();
                SBConfig.SELECTOR_PASTE.replace(scriptKeyName, regionPaste.getRegionBlocks().getCount()).send(sbPlayer);
                SBConfig.CONSOLE_SELECTOR_PASTE.replace(scriptKeyName, regionPaste.getRegionBlocks()).console();
            } finally {
                sbPlayer.setSBClipboard(null);
            }
        } else {
            CuboidRegionRemove regionRemove = new CuboidRegionRemove(region).remove();
            Set<ScriptKey> scriptKeys = regionRemove.getScriptKeys();
            if (scriptKeys.size() == 0) {
                SBConfig.ERROR_SCRIPT_FILE_CHECK.send(sender);
            } else {
                String types = scriptKeys.stream().map(ScriptKey::getName).collect(Collectors.joining(", "));
                SBConfig.SELECTOR_REMOVE.replace(types, regionRemove.getRegionBlocks().getCount()).send(player);
                SBConfig.CONSOLE_SELECTOR_REMOVE.replace(types, regionRemove.getRegionBlocks()).console();
            }
        }
        return true;
    }

    @Override
    public void tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull List<String> empty) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            Set<String> set = setCommandPermissions(sender, new LinkedHashSet<String>());
            StreamUtils.fForEach(set, s -> StringUtils.startsWith(s, prefix), empty::add);
        } else if (args.length == 2) {
            if (equals(args[0], "export")) {
                if (Permission.COMMAND_EXPORT.has(sender)) {
                    String prefix = args[1].toLowerCase(Locale.ROOT);
                    String[] answers = new String[] { "sound", "material" };
                    StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                }
            } else if (equals(args[0], "selector")) {
                if (Permission.COMMAND_SELECTOR.has(sender)) {
                    String prefix = args[1].toLowerCase(Locale.ROOT);
                    String[] answers = new String[] { "paste", "remove" };
                    StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                }
            } else if (equals(args[0], ScriptKey.types())) {
                if (Permission.has(sender, ScriptKey.valueOf(args[0]), true)) {
                    String prefix = args[1].toLowerCase(Locale.ROOT);
                    String[] answers = new String[] { "create", "add", "remove", "view", "run", "redstone" };
                    StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                }
            }
        } else if (args.length > 2) {
            if (args.length == 3 && equals(args[0], "selector") && equals(args[1], "paste")) {
                if (Permission.COMMAND_SELECTOR.has(sender)) {
                    String prefix = args[2].toLowerCase(Locale.ROOT);
                    String[] answers = new String[] { "true", "false" };
                    StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                }
            } else if (args.length == 4 && equals(args[0], "selector") && equals(args[1], "paste")) {
                if (Permission.COMMAND_SELECTOR.has(sender)) {
                    String prefix = args[3].toLowerCase(Locale.ROOT);
                    String[] answers = new String[] { "true", "false" };
                    StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                }
            } else if (equals(args[0], ScriptKey.types())) {
                if (Permission.has(sender, ScriptKey.valueOf(args[0]), true)) {
                    if (args.length == 3 && equals(args[1], "run")) {
                        List<World> worlds = Bukkit.getWorlds();
                        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
                        String[] answers = StreamUtils.toArray(worlds, World::getName, new String[worlds.size()]);
                        StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                    } else if (equals(args[1], "create", "add")) {
                        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
                        String[] answers = OptionManager.getSyntaxs();
                        Arrays.sort(answers);
                        StreamUtils.fForEach(answers, s -> s.startsWith(prefix), s -> empty.add(s.trim()));
                    } else if (args.length == 3 && equals(args[1], "redstone")) {
                        String prefix = args[2].toLowerCase(Locale.ROOT);
                        String[] answers = new String[] { "true", "false" };
                        StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                    } else if (args.length == 4 && equals(args[1], "redstone") && equals(args[2], "true")) {
                        String prefix = args[3].toLowerCase(Locale.ROOT);
                        List<String> answers = Lists.newArrayList("@a", "@e", "@p", "@r");
                        StreamUtils.forEach(Filter.getFilters(), t -> answers.add(Filter.getPrefix() + t.getSyntax() + "}"));
                        StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                    } else if (args.length == 5 && equals(args[1], "redstone") && equals(args[2], "true") && args[3].startsWith(Filter.getPrefix())) {
                        String prefix = args[4].toLowerCase(Locale.ROOT);
                        String[] answers = new String[] { "@a", "@e", "@p", "@r" };
                        StreamUtils.fForEach(answers, s -> s.startsWith(prefix), empty::add);
                    }
                }
            }
        }
    }

    @NotNull
    private Set<String> setCommandPermissions(@NotNull CommandSender sender, @NotNull Set<String> set) {
        StreamUtils.ifAction(Permission.COMMAND_TOOL.has(sender), () -> set.add("tool"));
        StreamUtils.ifAction(Permission.COMMAND_RELOAD.has(sender), () -> set.add("reload"));
        StreamUtils.ifAction(Permission.COMMAND_BACKUP.has(sender), () -> set.add("backup"));
        StreamUtils.ifAction(Permission.COMMAND_CHECKVER.has(sender), () -> set.add("checkver"));
        StreamUtils.ifAction(Permission.COMMAND_DATAMIGR.has(sender), () -> set.add("datamigr"));
        StreamUtils.ifAction(Permission.COMMAND_EXPORT.has(sender), () -> set.add("export"));
        StreamUtils.ifAction(Permission.COMMAND_SELECTOR.has(sender), () -> set.add("selector"));
        StreamUtils.fForEach(ScriptKey.values(), s -> Permission.has(sender, s, true), s -> set.add(s.getName()));
        return set;
    }

    private boolean isScripts(@NotNull String scriptLine) {
        try {
            int[] success = new int[] { 0 };
            List<String> scripts = StringUtils.getScripts(scriptLine);
            StreamUtils.fForEach(scripts, OptionManager::has, o -> success[0]++);
            if (success[0] == 0 || success[0] != scripts.size()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}