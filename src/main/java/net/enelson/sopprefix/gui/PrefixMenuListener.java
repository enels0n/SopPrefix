package net.enelson.sopprefix.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public final class PrefixMenuListener implements Listener {

    private final PrefixMenuService menuService;

    public PrefixMenuListener(PrefixMenuService menuService) {
        this.menuService = menuService;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof PrefixMenuHolder)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (event.getClickedInventory() == null || !event.getClickedInventory().equals(topInventory)) {
            return;
        }

        this.menuService.handleClick((Player) event.getWhoClicked(), (PrefixMenuHolder) topInventory.getHolder(), event.getRawSlot(), event.isRightClick(), event.isShiftClick());
    }
}
