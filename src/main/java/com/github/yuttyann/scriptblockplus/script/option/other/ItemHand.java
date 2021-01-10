package com.github.yuttyann.scriptblockplus.script.option.other;

import com.github.yuttyann.scriptblockplus.file.config.SBConfig;
import com.github.yuttyann.scriptblockplus.script.option.BaseOption;
import com.github.yuttyann.scriptblockplus.script.option.Option;
import com.github.yuttyann.scriptblockplus.script.option.OptionTag;
import com.github.yuttyann.scriptblockplus.utils.ItemUtils;
import com.github.yuttyann.scriptblockplus.utils.StringUtils;
import com.github.yuttyann.scriptblockplus.utils.Utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * ScriptBlockPlus ItemHand オプションクラス
 * @author yuttyann44581
 */
@OptionTag(name = "itemhand", syntax = "@hand:")
public class ItemHand extends BaseOption {

    @Override
    @NotNull
    public Option newInstance() {
        return new ItemHand();
    }

    @Override
    protected boolean isValid() throws Exception {
        String[] array = StringUtils.split(getOptionValue(), ' ');
        String[] param = StringUtils.split(StringUtils.removeStart(array[0], Utils.MINECRAFT), ':');
        if (Calculation.REALNUMBER_PATTERN.matcher(param[0]).matches()) {
            throw new IllegalAccessException("Numerical values can not be used");
        }
        Material material = ItemUtils.getMaterial(param[0]);
        int damage = param.length > 1 ? Integer.parseInt(param[1]) : 0;
        int amount = Integer.parseInt(array[1]);
        String create = array.length > 2 ? StringUtils.createString(array, 2) : null;
        String name = StringUtils.isEmpty(create) ? material.name() : StringUtils.setColor(create);

        Player player = getPlayer();
        Stream<ItemStack> handItems = getHandItems(player);
        if (handItems.noneMatch(i -> i.getAmount() > amount && ItemUtils.getDamage(i) == damage && ItemUtils.isItem(i, material, name))) {
            SBConfig.ERROR_HAND.replace(material, amount, damage, name).send(player);
            return false;
        }
        return true;
    }

    @NotNull
    private Stream<ItemStack> getHandItems(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        return Stream.of(new ItemStack[] { inventory.getItemInMainHand(), inventory.getItemInOffHand() });
    }
}