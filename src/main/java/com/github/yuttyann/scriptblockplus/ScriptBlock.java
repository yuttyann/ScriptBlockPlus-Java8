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
package com.github.yuttyann.scriptblockplus;

import com.github.yuttyann.scriptblockplus.command.BaseCommand;
import com.github.yuttyann.scriptblockplus.command.ScriptBlockPlusCommand;
import com.github.yuttyann.scriptblockplus.enums.reflection.PackageType;
import com.github.yuttyann.scriptblockplus.file.SBFiles;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.file.json.CacheJson;
import com.github.yuttyann.scriptblockplus.file.json.derived.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.legacy.ConvartList;
import com.github.yuttyann.scriptblockplus.file.json.legacy.LegacyFormatJson;
import com.github.yuttyann.scriptblockplus.hook.nms.GlowEntity;
import com.github.yuttyann.scriptblockplus.hook.plugin.VaultEconomy;
import com.github.yuttyann.scriptblockplus.hook.plugin.VaultPermission;
import com.github.yuttyann.scriptblockplus.listener.*;
import com.github.yuttyann.scriptblockplus.item.ItemAction;
import com.github.yuttyann.scriptblockplus.item.action.BlockSelector;
import com.github.yuttyann.scriptblockplus.item.action.ScriptEditor;
import com.github.yuttyann.scriptblockplus.item.action.ScriptViewer;
import com.github.yuttyann.scriptblockplus.listener.trigger.BreakTrigger;
import com.github.yuttyann.scriptblockplus.listener.trigger.HitTrigger;
import com.github.yuttyann.scriptblockplus.listener.trigger.InteractTrigger;
import com.github.yuttyann.scriptblockplus.listener.trigger.WalkTrigger;
import com.github.yuttyann.scriptblockplus.manager.APIManager;
import com.github.yuttyann.scriptblockplus.manager.OptionManager;
import com.github.yuttyann.scriptblockplus.player.BaseSBPlayer;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus ScriptBlock メインクラス
 * @author yuttyann44581
 */
public class ScriptBlock extends JavaPlugin {

    private static Scheduler scheduler;
    private static ScriptBlock scriptBlock;

    private Updater updater;

    @Override
    public void onEnable() {
        // 1.9未満のバージョンだった場合はプラグインを無効化
        if (!Utils.isCBXXXorLater("1.9")) {
            getLogger().warning("Unsupported Version: v" + Utils.getServerVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // NMSが見つからなかった場合警告
        if (!PackageType.HAS_NMS) {
            getLogger().warning("NMS(" + PackageType.NMS + ") not found.");
            getLogger().warning("Disables some functions.");
        }

        // 全ファイルの読み込み
        SBFiles.reload();

        // Vaultが導入されているのか確認
        if (VaultEconomy.INSTANCE.has()) {
            VaultEconomy.INSTANCE.setupEconomy();
            VaultPermission.INSTANCE.setupPermission();
        } else {
            SBConfig.NOT_VAULT.send();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 旧ScriptBlockが導入されていた場合は無効化
        Plugin plugin = getServer().getPluginManager().getPlugin("ScriptBlock");
        if (plugin != null) {
            getServer().getPluginManager().disablePlugin(plugin);
        }

        // 古い形式のJSONファイルを最新の物へ移行する。
        LegacyFormatJson.convart(ConvartList.create(this, "json"));

        // スクリプトの形式を".yml"から".json"へ移行
        ScriptKey.iterable().forEach(BlockScriptJson::convart);

        // キャッシュを生成(設定有効時のみ)
        CacheJson.loading();

        // ログイン中のプレイヤーの設定をオンラインへ変更
        Bukkit.getOnlinePlayers().forEach(p -> ((BaseSBPlayer) SBPlayer.fromPlayer(p)).setOnline(true));

        // リスナーの登録
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new InteractListener(), this);

        // トリガーを登録
        TriggerListener.register(new HitTrigger(this));
        TriggerListener.register(new WalkTrigger(this));
        TriggerListener.register(new BreakTrigger(this));
        TriggerListener.register(new InteractTrigger(this));

        // アイテムアクションの登録
        ItemAction.register(new ScriptViewer());
        ItemAction.register(new ScriptEditor());
        ItemAction.register(new BlockSelector());

        // オプションの更新
        OptionManager.update();

        // コマンドの登録
        BaseCommand.register("scriptblockplus", new ScriptBlockPlusCommand(this));

        // アップデート処理
        checkUpdate(Bukkit.getConsoleSender(), false);
    }

    @Override
    public void onDisable() {
        try {
            ScriptViewer.PLAYERS.clear();
            GlowEntity.DEFAULT.removeAll();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * 最新のバージョンが存在するか確認します。
     * @param sender - 送信先
     * @param latestMessage - {@code true}の場合は送信先にアップデートのメッセージを表示します。
     */
    public void checkUpdate(@NotNull CommandSender sender, boolean latestMessage) {
        if (updater == null) {
            updater = new Updater(this);
        }
        getScheduler().asyncRun(() -> {
            try {
                updater.load();
                if (!updater.run(sender) && latestMessage) {
                    SBConfig.NOT_LATEST_PLUGIN.send(sender);
                }
            } catch (Exception e) {
                e.printStackTrace();
                SBConfig.ERROR_UPDATE.send(sender);
            }
        });
    }

    /**
     * {@link ScriptBlock}のAPIを取得します。
     * @return {@link ScriptBlockAPI} - {@link ScriptBlock}のAPI
     */
    @NotNull
    public ScriptBlockAPI getAPI() {
        return new APIManager();
    }

    /**
     * {@link Scheduler}のインスタンスを取得します。
     * @return {@link Scheduler} - インスタンス
     */
    @NotNull
    public static Scheduler getScheduler() {
        return scheduler == null ? scheduler = new Scheduler(getInstance()) : scheduler;
    }

    /**
     * {@link ScriptBlock}のインスタンスを取得します。
     * @return {@link ScriptBlock} - インスタンス
     */
    @NotNull
    public static ScriptBlock getInstance() {
        return scriptBlock == null ? scriptBlock = getPlugin(ScriptBlock.class) : scriptBlock;
    }
}