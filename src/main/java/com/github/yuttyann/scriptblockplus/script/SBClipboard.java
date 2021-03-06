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
package com.github.yuttyann.scriptblockplus.script;

import com.github.yuttyann.scriptblockplus.BlockCoords;
import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.file.json.derived.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.derived.PlayerCountJson;
import com.github.yuttyann.scriptblockplus.file.json.derived.PlayerTimerJson;
import com.github.yuttyann.scriptblockplus.file.json.element.BlockScript;
import com.github.yuttyann.scriptblockplus.player.SBPlayer;
import com.github.yuttyann.scriptblockplus.utils.Utils;
import com.github.yuttyann.scriptblockplus.utils.unmodifiable.UnmodifiableBlockCoords;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * ScriptBlockPlus SBClipboard クラス
 * @author yuttyann44581
 */
public final class SBClipboard {

    private final SBPlayer sbPlayer;
    private final ScriptKey scriptKey;
    private final BlockCoords blockCoords;
    private final BlockScriptJson scriptJson;

    private final Set<UUID> author;
    private final List<String> script;
    private final String selector;
    private final int amount;

    public SBClipboard(@NotNull SBPlayer sbPlayer, @NotNull BlockCoords blockCoords, @NotNull BlockScriptJson scriptJson) {
        this.sbPlayer = sbPlayer;
        this.scriptKey = scriptJson.getScriptKey();
        this.blockCoords = new UnmodifiableBlockCoords(blockCoords);
        this.scriptJson = scriptJson;

        BlockScript blockScript = scriptJson.load(blockCoords);
        this.author = Sets.newLinkedHashSet(blockScript.getAuthors());
        this.script = Lists.newArrayList(blockScript.getScripts());
        this.selector = blockScript.getSelector();
        this.amount = blockScript.getAmount();
    }

    @NotNull
    public BlockScriptJson getBlockScriptJson() {
        return scriptJson;
    }

    @NotNull
    public BlockCoords getBlockCoords() {
        return blockCoords;
    }

    @NotNull
    public SBPlayer getSBPlayer() {
        return sbPlayer;
    }

    @NotNull
    public Set<UUID> getAuthor() {
        return author;
    }

    @NotNull
    public List<String> getScript() {
        return script;
    }

    @Nullable
    public String getSelector() {
        return selector;
    }

    public int getAmount() {
        return amount;
    }

    public void save() {
        scriptJson.saveJson();
    }

    public boolean copy() {
        if (!BlockScriptJson.contains(blockCoords, scriptJson)) {
            SBConfig.ERROR_SCRIPT_FILE_CHECK.send(sbPlayer);
            return false;
        }
        try {
            sbPlayer.setSBClipboard(this);
            SBConfig.SCRIPT_COPY.replace(scriptKey).send(sbPlayer);
            SBConfig.CONSOLE_SCRIPT_EDIT.replace(scriptKey, blockCoords).console();
        } finally {
            sbPlayer.setScriptEdit(null);
        }
        return true;
    }

    public boolean paste(@NotNull BlockCoords blockCoords, boolean overwrite) {
        if (BlockScriptJson.contains(blockCoords, scriptJson) && !overwrite) {
            return false;
        }
        try {
            BlockScript scriptParam = scriptJson.load(blockCoords);
            scriptParam.setAuthors(author);
            scriptParam.getAuthors().add(sbPlayer.getUniqueId());
            scriptParam.setScripts(script);
            scriptParam.setLastEdit(Utils.getFormatTime(Utils.DATE_PATTERN));
            scriptParam.setSelector(selector);
            scriptParam.setAmount(amount);
            scriptJson.saveJson();
            PlayerTimerJson.removeAll(scriptKey, blockCoords);
            PlayerCountJson.removeAll(scriptKey, blockCoords);
            SBConfig.SCRIPT_PASTE.replace(scriptKey).send(sbPlayer);
            SBConfig.CONSOLE_SCRIPT_EDIT.replace(scriptKey, blockCoords).console();
        } finally {
            sbPlayer.setScriptEdit(null);
            sbPlayer.setSBClipboard(null);
        }
        return true;
    }
}