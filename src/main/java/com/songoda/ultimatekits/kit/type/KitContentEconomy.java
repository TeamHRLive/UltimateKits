package com.songoda.ultimatekits.kit.type;

import com.songoda.arconix.api.methods.formatting.TextComponent;
import com.songoda.ultimatekits.Lang;
import com.songoda.ultimatekits.UltimateKits;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class KitContentEconomy implements KitContent {

    private double amount;

    public KitContentEconomy(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String getSerialized() {
        return UltimateKits.getInstance().getConfig().getString("Main.Currency Symbol") + amount;
    }

    @Override
    public ItemStack getItemForDisplay() {
        ItemStack parseStack = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = parseStack.getItemMeta();

        ArrayList<String> lore = new ArrayList<>();

        int index = 0;
        while (index < String.valueOf(amount).length()) {
            lore.add(TextComponent.formatText("&a" + (index == 0 ? UltimateKits.getInstance().getConfig().getString("Main.Currency Symbol") : "") + "&a" + String.valueOf(amount).substring(index, Math.min(index + 30, String.valueOf(amount).length()))));
            index += 30;
        }
        meta.setLore(lore);
        meta.setDisplayName(Lang.MONEY.getConfigValue());
        parseStack.setItemMeta(meta);
        return parseStack;
    }
}