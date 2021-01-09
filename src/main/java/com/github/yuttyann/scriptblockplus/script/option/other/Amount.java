package com.github.yuttyann.scriptblockplus.script.option.other;

import com.github.yuttyann.scriptblockplus.file.json.BlockScriptJson;
import com.github.yuttyann.scriptblockplus.file.json.PlayerCountJson;
import com.github.yuttyann.scriptblockplus.file.json.element.ScriptParam;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.script.option.time.TimerOption;
import org.jetbrains.annotations.NotNull;

/**
 * ScriptBlockPlus Amount オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "amount", syntax = "@amount:")
public class Amount extends BaseOption {

    @Override
    @NotNull
    public Option newInstance() {
        return new Amount();
    }

    @Override
    protected boolean isValid() throws Exception {
        BlockScriptJson scriptJson = new BlockScriptJson(getScriptKey());
        ScriptParam scriptParam = scriptJson.load().get(getLocation());
        if (scriptParam.getAmount() == -1) {
            scriptParam.setAmount(Integer.parseInt(getOptionValue()));
        }
        scriptParam.subtractAmount(1);
        if (scriptParam.getAmount() <= 0) {
            scriptJson.load().remove(getLocation());
            TimerOption.removeAll(getLocation(), getScriptKey());
            PlayerCountJson.clear(getLocation(), getScriptKey());
        }
        scriptJson.saveFile();
        return true;
    }
}