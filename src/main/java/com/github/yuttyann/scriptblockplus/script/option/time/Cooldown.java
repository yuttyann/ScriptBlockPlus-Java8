package com.github.yuttyann.scriptblockplus.script.option.time;

import com.github.yuttyann.scriptblockplus.file.json.PlayerTempJson;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * ScriptBlockPlus Cooldown オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "cooldown", syntax = "@cooldown:")
public class Cooldown extends TimerOption {

    @Override
    @NotNull
    public Option newInstance() {
        return new Cooldown();
    }

    @Override
    protected boolean isValid() throws Exception {
        if (inCooldown()) {
            return false;
        }
        long[] params = new long[] { System.currentTimeMillis(), Integer.parseInt(getOptionValue()) * 1000L, 0L };
        params[2] = params[0] + params[1];

        PlayerTempJson tempJson = new PlayerTempJson(getFileUniqueId());
        tempJson.load().getTimerTemp().add(new TimerTemp(getFileUniqueId(), getLocation(), getScriptKey()).set(params));
        tempJson.saveFile();
        return true;
    }

    @Override
    @NotNull
    protected Optional<TimerTemp> getTimerTemp() {
        Set<TimerTemp> timers = new PlayerTempJson(getFileUniqueId()).load().getTimerTemp();
        return get(timers, new TimerTemp(getFileUniqueId(), getLocation(), getScriptKey()));
    }
}