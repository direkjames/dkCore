package com.direk.dkcore.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A chest menu. Items can't be taken out; clicks run the handler attached to the slot.
 * <pre>
 * Menu menu = new Menu(3, title);
 * menu.set(13, item, click -> ...);
 * menu.open(player);
 * </pre>
 * Author: direk james
 */
public class Menu implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, Consumer<InventoryClickEvent>> handlers = new HashMap<>();

    public Menu(int rows, Component title) {
        int r = Math.max(1, Math.min(6, rows));
        this.inventory = Bukkit.createInventory(this, r * 9, title);
    }

    public Menu set(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        if (slot < 0 || slot >= inventory.getSize()) return this;
        inventory.setItem(slot, item);
        if (onClick != null) handlers.put(slot, onClick);
        else handlers.remove(slot);
        return this;
    }

    public Menu set(int slot, ItemStack item) {
        return set(slot, item, null);
    }

    /** Puts the item in every empty slot. */
    public Menu fill(ItemStack item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) inventory.setItem(i, item);
        }
        return this;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    void handle(InventoryClickEvent e) {
        Consumer<InventoryClickEvent> h = handlers.get(e.getRawSlot());
        if (h != null) h.accept(e);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
