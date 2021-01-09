package com.github.yuttyann.scriptblockplus.script.option.vault;

import com.github.yuttyann.scriptblockplus.hook.plugin.VaultPermission;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;

import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus PermRemove オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "perm_remove", syntax = "@permREMOVE:")
public class PermRemove extends BaseOption {

    @Override
    @NotNull
    public Option newInstance() {
        return new PermRemove();
    }

    @Override
    protected boolean isValid() throws Exception {
        VaultPermission vaultPermission = VaultPermission.INSTANCE;
        if (!vaultPermission.isEnabled() || vaultPermission.isSuperPerms()) {
            throw new UnsupportedOperationException();
        }
        String[] array = StringUtils.split(getOptionValue(), '/');
        String world = array.length > 1 ? array[0] : null;
        String permission = array.length > 1 ? array[1] : array[0];
        vaultPermission.playerRemove(world, getPlayer(), permission);
        return true;
    }
}