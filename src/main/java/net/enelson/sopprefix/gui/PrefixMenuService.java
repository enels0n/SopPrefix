package net.enelson.sopprefix.gui;

import net.enelson.sopprefix.prefix.FormatDefinition;
import net.enelson.sopprefix.prefix.PrefixCategory;
import net.enelson.sopprefix.prefix.PrefixDefinition;
import net.enelson.sopprefix.prefix.PrefixManager;
import net.enelson.sopprefix.prefix.SegmentSide;
import net.enelson.sopprefix.prefix.TagSegment;
import net.enelson.sopprefix.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PrefixMenuService {

    private static final int PAGE_SIZE = 21;

    private final PrefixManager prefixManager;
    private final List<Integer> entrySlots = Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

    public PrefixMenuService(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    public void openMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new PrefixMenuHolder(MenuType.MAIN, null, null, 0, false), 45, this.prefixManager.getMainMenuTitle(SegmentSide.PREFIX));
        renderMenu(player, inventory, (PrefixMenuHolder) inventory.getHolder(), true);
        player.openInventory(inventory);
    }

    public void openCategoryListMenu(Player player, String segmentId, int page) {
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (segment == null) {
            return;
        }
        Inventory inventory = Bukkit.createInventory(new PrefixMenuHolder(MenuType.CATEGORY_LIST, segmentId, null, page, false), 45, Text.color("&8Categories: " + segment.getName()));
        renderMenu(player, inventory, (PrefixMenuHolder) inventory.getHolder(), true);
        player.openInventory(inventory);
    }

    public void openTextMenu(Player player, String segmentId, String categoryId, int page, boolean availableOnly) {
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        PrefixCategory category = this.prefixManager.getCategory(categoryId);
        if (segment == null || category == null) {
            player.sendMessage(this.prefixManager.getMessage("category-empty"));
            return;
        }
        Inventory inventory = Bukkit.createInventory(new PrefixMenuHolder(MenuType.TEXT, segmentId, categoryId, page, availableOnly), 45, this.prefixManager.getCategoryMenuTitle(segment, category));
        renderMenu(player, inventory, (PrefixMenuHolder) inventory.getHolder(), true);
        player.openInventory(inventory);
    }

    public void openFormatMenu(Player player, String segmentId, int page, boolean availableOnly) {
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (segment == null) {
            return;
        }
        Inventory inventory = Bukkit.createInventory(new PrefixMenuHolder(MenuType.FORMAT, segmentId, null, page, availableOnly), 45, this.prefixManager.getFormatMenuTitle(segment));
        renderMenu(player, inventory, (PrefixMenuHolder) inventory.getHolder(), true);
        player.openInventory(inventory);
    }

    public void closeAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof PrefixMenuHolder) {
                player.closeInventory();
            }
        }
    }

    public void refreshOpenMenus() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inventory = player.getOpenInventory().getTopInventory();
            if (!(inventory.getHolder() instanceof PrefixMenuHolder)) {
                continue;
            }
            renderMenu(player, inventory, (PrefixMenuHolder) inventory.getHolder(), false);
        }
    }

    public void handleClick(Player player, PrefixMenuHolder holder, int rawSlot, boolean rightClick, boolean shiftClick) {
        if (holder.getType() == MenuType.MAIN) {
            handleMainClick(player, rawSlot, rightClick, shiftClick);
            return;
        }
        if (holder.getType() == MenuType.CATEGORY_LIST) {
            handleCategoryListClick(player, holder.getSegmentId(), holder.getPage(), rawSlot);
            return;
        }
        if (holder.getType() == MenuType.TEXT) {
            handleTextClick(player, holder.getSegmentId(), holder.getCategoryId(), holder.getPage(), holder.isAvailableOnly(), rawSlot);
            return;
        }
        handleFormatClick(player, holder.getSegmentId(), holder.getPage(), holder.isAvailableOnly(), rawSlot);
    }

    private void handleMainClick(Player player, int rawSlot, boolean rightClick, boolean shiftClick) {
        for (TagSegment segment : this.prefixManager.getMainMenuSegments(SegmentSide.PREFIX)) {
            int textSlot = segment.getMenuSlot();
            int formatSlot = segment.getFormatMenuSlot();
            boolean formatButtonAvailable = segment.isEditable();
            if (rawSlot != textSlot && (!formatButtonAvailable || rawSlot != formatSlot)) {
                continue;
            }
            if (segment.isToggleable() && !segment.isEditable()) {
                this.prefixManager.toggleSegmentVisibility(player, segment.getId());
                openMainMenu(player);
                return;
            }
            if (shiftClick && rightClick) {
                this.prefixManager.clearActiveFormat(player, segment.getId());
                player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-cleared"), Collections.singletonMap("%segment%", segment.getName())));
                openMainMenu(player);
                return;
            }
            if (shiftClick) {
                this.prefixManager.clearActiveText(player, segment.getId());
                player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-cleared"), Collections.singletonMap("%segment%", segment.getName())));
                openMainMenu(player);
                return;
            }
            if (rawSlot == formatSlot || rightClick) {
                if (!this.prefixManager.hasActiveText(player, segment.getId())) {
                    player.sendMessage(this.prefixManager.getMessage("select-text-first"));
                    return;
                }
                openFormatMenu(player, segment.getId(), 0, false);
                return;
            }
            openCategoryListMenu(player, segment.getId(), 0);
            return;
        }
    }

    private void renderMenu(Player player, Inventory inventory, PrefixMenuHolder holder, boolean initial) {
        if (initial) {
            clearMenu(inventory);
            fillBorders(inventory);
        }
        if (holder.getType() == MenuType.MAIN) {
            renderMainMenu(player, inventory);
            return;
        }
        if (holder.getType() == MenuType.CATEGORY_LIST) {
            renderCategoryListMenu(player, inventory, holder.getSegmentId(), holder.getPage());
            return;
        }
        if (holder.getType() == MenuType.TEXT) {
            renderTextMenu(player, inventory, holder.getSegmentId(), holder.getCategoryId(), holder.getPage(), holder.isAvailableOnly());
            return;
        }
        renderFormatMenu(player, inventory, holder.getSegmentId(), holder.getPage(), holder.isAvailableOnly());
    }

    private void renderMainMenu(Player player, Inventory inventory) {
        for (TagSegment segment : this.prefixManager.getMainMenuSegments(SegmentSide.PREFIX)) {
            placeMainSegmentButtons(player, inventory, segment);
        }
        setItemIfChanged(inventory, 4, this.prefixManager.createConfiguredMenuItem(
                player,
                "preview",
                Material.BOOK,
                "&fPreview",
                Collections.singletonList("%preview%"),
                previewReplacements(player)
        ));
    }

    private void renderCategoryListMenu(Player player, Inventory inventory, String segmentId, int page) {
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (segment == null) {
            return;
        }
        setItemIfChanged(inventory, 36, createBackItem(player, segment));
        List<PrefixCategory> categories = new ArrayList<PrefixCategory>(this.prefixManager.getCategoriesForSegment(segmentId));
        fillPagedEntries(inventory, page, categories, new EntryRenderer<PrefixCategory>() {
            @Override
            public ItemStack render(PrefixCategory value) {
                return prefixManager.createCategoryItem(player, value, segmentId);
            }
        });
        placePageControls(player, inventory, page, categories.size());
    }

    private void renderTextMenu(Player player, Inventory inventory, String segmentId, String categoryId, int page, boolean availableOnly) {
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (segment == null) {
            return;
        }
        setItemIfChanged(inventory, 36, createBackItem(player, segment));
        setItemIfChanged(inventory, 38, this.prefixManager.createConfiguredMenuItem(
                player,
                "clear-text",
                Material.BARRIER,
                "&cClear text",
                Arrays.asList("&7Clears the selected text.", "", "&eLeft click: clear"),
                segmentReplacements(player, segment)
        ));
        setItemIfChanged(inventory, 40, this.prefixManager.createConfiguredMenuItem(
                player,
                "random-text",
                Material.SUNFLOWER,
                "&6Random text",
                Arrays.asList("&7Selects a random available text.", "", "&eLeft click: choose"),
                segmentReplacements(player, segment)
        ));
        setItemIfChanged(inventory, 42, this.prefixManager.createConfiguredMenuItem(
                player,
                availableOnly ? "available-toggle-on" : "available-toggle-off",
                availableOnly ? Material.LIME_DYE : Material.HOPPER,
                availableOnly ? "&aAvailable only" : "&fAll variants",
                Arrays.asList(availableOnly ? "&7Unavailable variants are hidden." : "&7All variants are visible.", "", "&eLeft click: toggle"),
                segmentReplacements(player, segment)
        ));

        List<PrefixDefinition> values = availableOnly
                ? this.prefixManager.getAvailableDefinitions(player, segmentId)
                : this.prefixManager.getDefinitionsByCategory(segmentId, categoryId);
        if (availableOnly) {
            java.util.Iterator<PrefixDefinition> iterator = values.iterator();
            while (iterator.hasNext()) {
                PrefixDefinition definition = iterator.next();
                if (!definition.getCategoryId().equalsIgnoreCase(categoryId)) {
                    iterator.remove();
                }
            }
        }
        fillPagedEntries(inventory, page, values, new EntryRenderer<PrefixDefinition>() {
            @Override
            public ItemStack render(PrefixDefinition value) {
                return prefixManager.createDefinitionItem(player, value);
            }
        });
        placePageControls(player, inventory, page, values.size());
    }

    private void renderFormatMenu(Player player, Inventory inventory, String segmentId, int page, boolean availableOnly) {
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (segment == null) {
            return;
        }
        setItemIfChanged(inventory, 36, createBackItem(player, segment));
        setItemIfChanged(inventory, 38, this.prefixManager.createConfiguredMenuItem(
                player,
                "clear-format",
                Material.BARRIER,
                "&cClear format",
                Arrays.asList("&7Returns the default format.", "", "&eLeft click: clear"),
                segmentReplacements(player, segment)
        ));
        setItemIfChanged(inventory, 40, this.prefixManager.createConfiguredMenuItem(
                player,
                "random-format",
                Material.SUNFLOWER,
                "&6Random format",
                Arrays.asList("&7Selects a random available format.", "", "&eLeft click: choose"),
                segmentReplacements(player, segment)
        ));
        setItemIfChanged(inventory, 42, this.prefixManager.createConfiguredMenuItem(
                player,
                availableOnly ? "available-toggle-on" : "available-toggle-off",
                availableOnly ? Material.LIME_DYE : Material.HOPPER,
                availableOnly ? "&aAvailable only" : "&fAll variants",
                Arrays.asList(availableOnly ? "&7Unavailable formats are hidden." : "&7All formats are visible.", "", "&eLeft click: toggle"),
                segmentReplacements(player, segment)
        ));

        List<FormatDefinition> values = availableOnly
                ? this.prefixManager.getAvailableFormats(player, segmentId)
                : getAllFormats(segmentId);
        fillPagedEntries(inventory, page, values, new EntryRenderer<FormatDefinition>() {
            @Override
            public ItemStack render(FormatDefinition value) {
                return prefixManager.createFormatItem(player, segmentId, value);
            }
        });
        placePageControls(player, inventory, page, values.size());
    }

    private void placeMainSegmentButtons(Player player, Inventory inventory, TagSegment segment) {
        PrefixDefinition activeText = this.prefixManager.getActiveText(player, segment.getId());
        FormatDefinition activeFormat = this.prefixManager.getActiveFormat(player, segment.getId());
        Map<String, String> replacements = segmentReplacements(player, segment);
        replacements.put("%current_text%", activeText == null ? this.prefixManager.getMenuString("value-none", "&7Not selected") : activeText.getValue());
        replacements.put("%current_format%", activeFormat == null ? this.prefixManager.getMenuString("value-default-format", "&7Default") : Text.color(activeFormat.getDisplayName()));
        replacements.put("%format_state%", activeText == null ? this.prefixManager.getMenuString("format-locked", "&cSelect text first") : this.prefixManager.getMenuString("format-open", "&eLeft click: choose format"));
        if (segment.isToggleable() && !segment.isEditable()) {
            boolean visible = this.prefixManager.isSegmentVisible(player, segment.getId());
            String currentValue = this.prefixManager.getRawSegmentValue(player, segment.getId());
            replacements.put("%current_text%", currentValue == null || currentValue.trim().isEmpty() ? this.prefixManager.getMenuString("value-none", "&7Not selected") : currentValue);
            replacements.put("%toggle_state%", this.prefixManager.getMenuString(visible ? "toggle-state-visible" : "toggle-state-hidden", visible ? "&aVisible" : "&cHidden"));
            replacements.put("%toggle_action%", this.prefixManager.getMenuString(visible ? "toggle-action-hide" : "toggle-action-show", visible ? "&eLeft click: hide" : "&eLeft click: show"));
            if (segment.hasCustomButtonAppearance()) {
                setItemIfChanged(inventory, segment.getMenuSlot(), this.prefixManager.createConfiguredMenuItem(
                        player,
                        "__unused_segment_toggle_button__",
                        segment.getButtonMaterialSpec(),
                        visible ? Material.LIME_DYE : Material.GRAY_DYE,
                        segment.getButtonName() == null ? segment.getName() : segment.getButtonName(),
                        segment.getButtonLore().isEmpty()
                                ? Arrays.asList("&7Value: &f%current_text%", "&7State: %toggle_state%", "", "&7Preview:", "%preview%", "", "%toggle_action%")
                                : segment.getButtonLore(),
                        replacements,
                        segment.getLore()
                ));
            } else {
                setItemIfChanged(inventory, segment.getMenuSlot(), this.prefixManager.createConfiguredMenuItem(
                        player,
                        "segment-toggle-button",
                        segment.getButtonMaterialSpec(),
                        visible ? Material.LIME_DYE : Material.GRAY_DYE,
                        segment.getName(),
                        Arrays.asList("&7Value: &f%current_text%", "&7State: %toggle_state%", "", "&7Preview:", "%preview%", "", "%toggle_action%"),
                        replacements,
                        segment.getLore()
                ));
            }
            return;
        }
        if (segment.hasCustomButtonAppearance()) {
            setItemIfChanged(inventory, segment.getMenuSlot(), this.prefixManager.createConfiguredMenuItem(
                    player,
                    "__unused_segment_text_button__",
                    segment.getButtonMaterialSpec(),
                    Material.NAME_TAG,
                    segment.getButtonName() == null ? segment.getName() : segment.getButtonName(),
                    segment.getButtonLore().isEmpty()
                            ? Arrays.asList("&7Text: &f%current_text%", "&7Preview:", "%preview%", "", "&eLeft click: choose text", "&cShift+Left click: clear text")
                            : segment.getButtonLore(),
                    replacements,
                    segment.getLore()
            ));
        } else {
            setItemIfChanged(inventory, segment.getMenuSlot(), this.prefixManager.createConfiguredMenuItem(
                    player,
                    "segment-text-button",
                    segment.getButtonMaterialSpec(),
                    Material.NAME_TAG,
                    segment.getName(),
                    Arrays.asList("&7Text: &f%current_text%", "&7Preview:", "%preview%", "", "&eLeft click: choose text", "&cShift+Left click: clear text"),
                    replacements,
                    segment.getLore()
            ));
        }
        if (segment.hasCustomFormatButtonAppearance()) {
            setItemIfChanged(inventory, segment.getFormatMenuSlot(), this.prefixManager.createConfiguredMenuItem(
                    player,
                    "__unused_segment_format_button__",
                    segment.getFormatButtonMaterialSpec(),
                    activeText == null ? Material.GRAY_DYE : Material.INK_SAC,
                    segment.getFormatButtonName() == null ? "&6Format: " + segment.getName() : segment.getFormatButtonName(),
                    segment.getFormatButtonLore().isEmpty()
                            ? Arrays.asList("&7Format: &f%current_format%", "%format_state%", activeText == null ? "&8Unavailable" : "%preview%", "", activeText == null ? "&cLeft click: unavailable" : "&eLeft click: choose format", "&cShift+Left click: clear format")
                            : segment.getFormatButtonLore(),
                    replacements,
                    segment.getLore()
            ));
        } else {
            setItemIfChanged(inventory, segment.getFormatMenuSlot(), this.prefixManager.createConfiguredMenuItem(
                    player,
                    "segment-format-button",
                    segment.getFormatButtonMaterialSpec(),
                    activeText == null ? Material.GRAY_DYE : Material.INK_SAC,
                    "&6Format: " + segment.getName(),
                    Arrays.asList("&7Format: &f%current_format%", "%format_state%", activeText == null ? "&8Unavailable" : "%preview%", "", activeText == null ? "&cLeft click: unavailable" : "&eLeft click: choose format", "&cShift+Left click: clear format"),
                    replacements,
                    segment.getLore()
            ));
        }
    }

    private void handleCategoryListClick(Player player, String segmentId, int page, int rawSlot) {
        List<PrefixCategory> categories = new ArrayList<PrefixCategory>(this.prefixManager.getCategoriesForSegment(segmentId));
        if (rawSlot == 36) {
            openMainMenu(player);
            return;
        }
        if (rawSlot == 37 && page > 0) {
            openCategoryListMenu(player, segmentId, page - 1);
            return;
        }
        if (rawSlot == 43 && hasNextPage(categories.size(), page)) {
            openCategoryListMenu(player, segmentId, page + 1);
            return;
        }

        int index = getEntryIndex(page, rawSlot);
        if (index < 0 || index >= categories.size()) {
            return;
        }
        openTextMenu(player, segmentId, categories.get(index).getId(), 0, false);
    }

    private void handleTextClick(Player player, String segmentId, String categoryId, int page, boolean availableOnly, int rawSlot) {
        List<PrefixDefinition> values = availableOnly ? this.prefixManager.getAvailableDefinitions(player, segmentId) : this.prefixManager.getDefinitionsByCategory(segmentId, categoryId);
        if (availableOnly) {
            java.util.Iterator<PrefixDefinition> iterator = values.iterator();
            while (iterator.hasNext()) {
                PrefixDefinition definition = iterator.next();
                if (!definition.getCategoryId().equalsIgnoreCase(categoryId)) {
                    iterator.remove();
                }
            }
        }
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (rawSlot == 36) {
            openCategoryListMenu(player, segmentId, 0);
            return;
        }
        if (rawSlot == 38) {
            this.prefixManager.clearActiveText(player, segmentId);
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-cleared"), Collections.singletonMap("%segment%", segment.getName())));
            openTextMenu(player, segmentId, categoryId, page, availableOnly);
            return;
        }
        if (rawSlot == 40) {
            PrefixDefinition random = this.prefixManager.setRandomText(player, segmentId);
            if (random == null) {
                player.sendMessage(this.prefixManager.getMessage("no-texts"));
            } else {
                player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-set"), replacements(segment.getName(), random.getValue())));
            }
            openTextMenu(player, segmentId, categoryId, page, availableOnly);
            return;
        }
        if (rawSlot == 42) {
            openTextMenu(player, segmentId, categoryId, 0, !availableOnly);
            return;
        }
        if (rawSlot == 37 && page > 0) {
            openTextMenu(player, segmentId, categoryId, page - 1, availableOnly);
            return;
        }
        if (rawSlot == 43 && hasNextPage(values.size(), page)) {
            openTextMenu(player, segmentId, categoryId, page + 1, availableOnly);
            return;
        }

        int index = getEntryIndex(page, rawSlot);
        if (index < 0 || index >= values.size()) {
            return;
        }
        PrefixDefinition definition = values.get(index);
        if (!this.prefixManager.setActiveText(player, segmentId, definition.getId())) {
            player.sendMessage(this.prefixManager.getMessage("text-not-available"));
            return;
        }
        player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-set"), replacements(segment.getName(), definition.getValue())));
        openTextMenu(player, segmentId, categoryId, page, availableOnly);
    }

    private void handleFormatClick(Player player, String segmentId, int page, boolean availableOnly, int rawSlot) {
        List<FormatDefinition> values = availableOnly ? this.prefixManager.getAvailableFormats(player, segmentId) : getAllFormats(segmentId);
        TagSegment segment = this.prefixManager.getSegment(segmentId);
        if (rawSlot == 36) {
            openMainMenu(player);
            return;
        }
        if (rawSlot == 38) {
            this.prefixManager.clearActiveFormat(player, segmentId);
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-cleared"), Collections.singletonMap("%segment%", segment.getName())));
            openFormatMenu(player, segmentId, page, availableOnly);
            return;
        }
        if (rawSlot == 40) {
            FormatDefinition random = this.prefixManager.setRandomFormat(player, segmentId);
            if (random == null) {
                player.sendMessage(this.prefixManager.getMessage("no-formats"));
            } else {
                player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-set"), replacements(segment.getName(), random.getDisplayName())));
            }
            openFormatMenu(player, segmentId, page, availableOnly);
            return;
        }
        if (rawSlot == 42) {
            openFormatMenu(player, segmentId, 0, !availableOnly);
            return;
        }
        if (rawSlot == 37 && page > 0) {
            openFormatMenu(player, segmentId, page - 1, availableOnly);
            return;
        }
        if (rawSlot == 43 && hasNextPage(values.size(), page)) {
            openFormatMenu(player, segmentId, page + 1, availableOnly);
            return;
        }

        int index = getEntryIndex(page, rawSlot);
        if (index < 0 || index >= values.size()) {
            return;
        }
        FormatDefinition definition = values.get(index);
        if (!this.prefixManager.setActiveFormat(player, segmentId, definition.getId())) {
            player.sendMessage(this.prefixManager.getMessage("format-not-available"));
            return;
        }
        player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-set"), replacements(segment.getName(), definition.getDisplayName())));
        openFormatMenu(player, segmentId, page, availableOnly);
    }

    private List<FormatDefinition> getAllFormats(String segmentId) {
        List<FormatDefinition> result = new ArrayList<FormatDefinition>();
        for (String id : this.prefixManager.getFormatIds(segmentId)) {
            FormatDefinition definition = this.prefixManager.getFormatDefinition(segmentId, id);
            if (definition != null) {
                result.add(definition);
            }
        }
        return result;
    }

    private Map<String, String> replacements(String segment, String value) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("%segment%", segment);
        map.put("%value%", value == null ? "" : value);
        return map;
    }

    private Map<String, String> previewReplacements(Player player) {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("%preview%", this.prefixManager.getPreview(player));
        return replacements;
    }

    private Map<String, String> segmentReplacements(Player player, TagSegment segment) {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("%segment%", segment.getName());
        replacements.put("%preview%", this.prefixManager.getPreview(player));
        return replacements;
    }

    private ItemStack createBackItem(Player player, TagSegment segment) {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("%segment%", segment == null ? "" : segment.getName());
        return this.prefixManager.createConfiguredMenuItem(
                player,
                "back",
                Material.ARROW,
                "&eBack",
                Arrays.asList("&7Return to previous menu.", "", "&eLeft click: back"),
                replacements
        );
    }

    private void fillBorders(Inventory inventory) {
        ItemStack filler = this.prefixManager.createConfiguredMenuItem(
                "filler",
                Material.BLACK_STAINED_GLASS_PANE,
                " ",
                Collections.<String>emptyList(),
                Collections.<String, String>emptyMap()
        );
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                setItemIfChanged(inventory, i, filler);
            }
        }
    }

    private void clearMenu(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, null);
        }
    }

    private void fillPagedEntries(Inventory inventory, int page, List<?> values, EntryRenderer<?> renderer) {
        int start = page * PAGE_SIZE;
        for (int i = 0; i < PAGE_SIZE; i++) {
            int index = start + i;
            if (index >= values.size()) {
                break;
            }
            setItemIfChanged(inventory, this.entrySlots.get(i), render(renderer, values.get(index)));
        }
        for (int i = Math.max(0, values.size() - start); i < PAGE_SIZE; i++) {
            setItemIfChanged(inventory, this.entrySlots.get(i), null);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ItemStack render(EntryRenderer<?> renderer, Object value) {
        return ((EntryRenderer<T>) renderer).render((T) value);
    }

    private void placePageControls(Player player, Inventory inventory, int page, int totalEntries) {
        int maxPage = totalEntries == 0 ? 0 : (totalEntries - 1) / PAGE_SIZE;
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("%page%", String.valueOf(page + 1));
        replacements.put("%max_page%", String.valueOf(maxPage + 1));
        replacements.put("%previous_page%", String.valueOf(page));
        replacements.put("%next_page%", String.valueOf(page + 2));
        setItemIfChanged(inventory, 41, this.prefixManager.createConfiguredMenuItem(
                player,
                "page-info",
                Material.BOOK,
                "&fPage",
                Collections.singletonList("&7%page%&f/&7%max_page%"),
                replacements
        ));
        if (page > 0) {
            setItemIfChanged(inventory, 37, this.prefixManager.createConfiguredMenuItem(
                    player,
                    "previous-page",
                    Material.ARROW,
                    "&ePrevious",
                    Arrays.asList("&7Return to page %previous_page%.", "", "&eLeft click: back"),
                    replacements
            ));
        } else {
            setItemIfChanged(inventory, 37, null);
        }
        if (hasNextPage(totalEntries, page)) {
            setItemIfChanged(inventory, 43, this.prefixManager.createConfiguredMenuItem(
                    player,
                    "next-page",
                    Material.ARROW,
                    "&eNext",
                    Arrays.asList("&7Go to page %next_page%.", "", "&eLeft click: next"),
                    replacements
            ));
        } else {
            setItemIfChanged(inventory, 43, null);
        }
    }

    private boolean hasNextPage(int totalEntries, int page) {
        return (page + 1) * PAGE_SIZE < totalEntries;
    }

    private int getEntryIndex(int page, int rawSlot) {
        int slotIndex = this.entrySlots.indexOf(rawSlot);
        if (slotIndex < 0) {
            return -1;
        }
        return page * PAGE_SIZE + slotIndex;
    }

    private void setItemIfChanged(Inventory inventory, int slot, ItemStack item) {
        ItemStack current = inventory.getItem(slot);
        if (current == null && item == null) {
            return;
        }
        if (current != null && item != null && current.getType() == item.getType() && current.getAmount() == item.getAmount() && current.getType() == Material.PLAYER_HEAD) {
            if (!current.isSimilar(item)) {
                updateHeadPresentation(current, item);
            }
            return;
        }
        if (current != null && item != null && current.getAmount() == item.getAmount() && current.isSimilar(item)) {
            return;
        }
        inventory.setItem(slot, item);
    }

    private void updateHeadPresentation(ItemStack current, ItemStack updated) {
        ItemMeta currentMeta = current.getItemMeta();
        ItemMeta updatedMeta = updated.getItemMeta();
        if (currentMeta == null || updatedMeta == null) {
            return;
        }
        currentMeta.setDisplayName(updatedMeta.getDisplayName());
        currentMeta.setLore(updatedMeta.getLore());
        if (updatedMeta.hasCustomModelData()) {
            currentMeta.setCustomModelData(updatedMeta.getCustomModelData());
        } else if (currentMeta.hasCustomModelData()) {
            currentMeta.setCustomModelData(null);
        }
        current.setItemMeta(currentMeta);
    }

    private interface EntryRenderer<T> {
        ItemStack render(T value);
    }
}
