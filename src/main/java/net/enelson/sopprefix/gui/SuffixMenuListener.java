package net.enelson.sopprefix.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public final class SuffixMenuListener implements Listener {

    private final SuffixMenuService menuService;

    public SuffixMenuListener(SuffixMenuService menuService) {
        this.menuService = menuService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof SuffixMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) {
            return;
        }

        this.menuService.handleClick((Player) event.getWhoClicked(), (SuffixMenuHolder) topInventory.getHolder(), event.getRawSlot(), event.isRightClick(), event.isShiftClick());
    }
}
