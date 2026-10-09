package com.direk.dkcore.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/** Fluent helper for menu icons. Text is non-italic by default. Author: direk james */
public final class ItemBuilder {

    private final ItemStack item;
    private Component name;
    private final List<Component> lore = new ArrayList<>();
    private boolean glow;

    private ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, Math.max(1, amount));
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material, 1);
    }

    /** Parses a material name, falling back to {@code fallback} if it's invalid. */
    public static ItemBuilder of(String material, Material fallback) {
        Material m = material == null ? null : Material.matchMaterial(material);
        return new ItemBuilder(m == null || !m.isItem() ? fallback : m, 1);
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(item.getMaxStackSize(), amount)));
        return this;
    }

    public ItemBuilder name(Component name) {
        this.name = name;
        return this;
    }

    public ItemBuilder lore(List<Component> lines) {
        lore.addAll(lines);
        return this;
    }

    public ItemBuilder lore(Component line) {
        lore.add(line);
        return this;
    }

    public ItemBuilder glow(boolean glow) {
        this.glow = glow;
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) meta.displayName(name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
            if (!lore.isEmpty()) {
                List<Component> fixed = new ArrayList<>();
                for (Component c : lore) fixed.add(c.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                meta.lore(fixed);
            }
            if (glow) meta.setEnchantmentGlintOverride(true);
            meta.addItemFlags(ItemFlag.values());
            item.setItemMeta(meta);
        }
        return item;
    }
}
