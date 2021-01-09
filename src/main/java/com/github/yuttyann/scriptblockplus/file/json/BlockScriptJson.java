package com.github.yuttyann.scriptblockplus.file.json;

import com.github.yuttyann.scriptblockplus.file.Json;
import com.github.yuttyann.scriptblockplus.file.SBLoader;
import com.github.yuttyann.scriptblockplus.file.json.annotation.JsonOptions;
import com.github.yuttyann.scriptblockplus.file.json.element.BlockScript;
import com.github.yuttyann.scriptblockplus.file.json.element.ScriptParam;
import com.github.yuttyann.scriptblockplus.script.ScriptKey;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/**
 * ScriptBlockPlus BlockScriptJson クラス
 * @author yuttyann44581
 */
@JsonOptions(path = "json/blockscript", file = "{id}.json")
public class BlockScriptJson extends Json<BlockScript> {

    public BlockScriptJson(@NotNull ScriptKey scriptKey) {
        super(scriptKey.getName());
    }

    public static boolean has(@NotNull Location location, @NotNull ScriptKey scriptKey) {
        return has(location, new BlockScriptJson(scriptKey));
    }

    public static boolean has(@NotNull Location location, @NotNull BlockScriptJson scriptJson) {
        return scriptJson.exists() && scriptJson.load().has(location);
    }

    @NotNull
    public ScriptKey getScriptKey() {
        return ScriptKey.valueOf(getId());
    }

    @Override
    @NotNull
    public BlockScript newInstance(@NotNull Object[] args) {
        return new BlockScript(getScriptKey());
    }

    public static void convart(@NotNull ScriptKey scriptKey) {
        // YAML形式のファイルからデータを読み込むクラス
        SBLoader scriptLoader = new SBLoader(scriptKey);
        if (!scriptLoader.getFile().exists()) {
            return;
        }
        // JSONを作成
        BlockScriptJson scriptJson = new BlockScriptJson(scriptKey);
        BlockScript blockScript = scriptJson.load();
        scriptLoader.forEach(s -> {
            // 移行の為、パラメータを設定する
            List<UUID> author = s.getAuthors();
            if (author.size() == 0) {
                return;
            }
            ScriptParam scriptParam = blockScript.get(s.getLocation());
            scriptParam.setAuthor(new LinkedHashSet<>(author));
            scriptParam.setScript(s.getScripts());
            scriptParam.setLastEdit(s.getLastEdit());
            scriptParam.setAmount(s.getAmount());
        });
        scriptJson.saveFile();

        // 移行完了後にファイルとディレクトリを削除する
        scriptLoader.getFile().delete();
        File parent = scriptLoader.getFile().getParentFile();
        if (parent.isDirectory() && parent.list().length == 0) {
            parent.delete();
        }
    }
}