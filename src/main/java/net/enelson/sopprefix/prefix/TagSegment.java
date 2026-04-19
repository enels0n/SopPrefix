package net.enelson.sopprefix.prefix;
import java.util.List;
public final class TagSegment {
    private final String id;
    private final String name;
    private final SegmentSide side;
    private final boolean editable;
    private final boolean toggleable;
    private final boolean defaultVisible;
    private final SegmentSource source;
    private final int menuSlot;
    private final int formatMenuSlot;
    private final String materialSpec;
    private final String buttonMaterialSpec;
    private final String buttonName;
    private final List<String> buttonLore;
    private final String formatButtonMaterialSpec;
    private final String formatButtonName;
    private final List<String> formatButtonLore;
    private final String rawValue;
    private final String textSection;
    private final String formatSection;
    private final String defaultFormat;
    private final boolean separatorContent;
    private final List<String> lore;
    public TagSegment(String id, String name, SegmentSide side, boolean editable, boolean toggleable, boolean defaultVisible, SegmentSource source, int menuSlot, int formatMenuSlot, String materialSpec, String buttonMaterialSpec, String buttonName, List<String> buttonLore, String formatButtonMaterialSpec, String formatButtonName, List<String> formatButtonLore, String rawValue, String textSection, String formatSection, String defaultFormat, boolean separatorContent, List<String> lore) {
        this.id = id;
        this.name = name;
        this.side = side;
        this.editable = editable;
        this.toggleable = toggleable;
        this.defaultVisible = defaultVisible;
        this.source = source;
        this.menuSlot = menuSlot;
        this.formatMenuSlot = formatMenuSlot;
        this.materialSpec = materialSpec;
        this.buttonMaterialSpec = buttonMaterialSpec;
        this.buttonName = buttonName;
        this.buttonLore = buttonLore;
        this.formatButtonMaterialSpec = formatButtonMaterialSpec;
        this.formatButtonName = formatButtonName;
        this.formatButtonLore = formatButtonLore;
        this.rawValue = rawValue;
        this.textSection = textSection;
        this.formatSection = formatSection;
        this.defaultFormat = defaultFormat;
        this.separatorContent = separatorContent;
        this.lore = lore;
    }
    public String getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public SegmentSide getSide() {
        return this.side;
    }
    public boolean isEditable() {
        return this.editable;
    }
    public boolean isToggleable() {
        return this.toggleable;
    }
    public boolean isDefaultVisible() {
        return this.defaultVisible;
    }
    public SegmentSource getSource() {
        return this.source;
    }
    public int getMenuSlot() {
        return this.menuSlot;
    }
    public int getFormatMenuSlot() {
        return this.formatMenuSlot;
    }
    public String getMaterialSpec() {
        return this.materialSpec;
    }
    public String getButtonMaterialSpec() {
        return this.buttonMaterialSpec;
    }
    public String getButtonName() {
        return this.buttonName;
    }
    public List<String> getButtonLore() {
        return this.buttonLore;
    }
    public String getFormatButtonMaterialSpec() {
        return this.formatButtonMaterialSpec;
    }
    public String getFormatButtonName() {
        return this.formatButtonName;
    }
    public List<String> getFormatButtonLore() {
        return this.formatButtonLore;
    }
    public boolean hasCustomFormatButtonAppearance() {
        return this.formatButtonName != null || (this.formatButtonLore != null && !this.formatButtonLore.isEmpty());
    }
    public boolean hasCustomButtonAppearance() {
        return this.buttonName != null || (this.buttonLore != null && !this.buttonLore.isEmpty());
    }
    public String getRawValue() {
        return this.rawValue;
    }
    public String getTextSection() {
        return this.textSection;
    }
    public String getFormatSection() {
        return this.formatSection;
    }
    public String getDefaultFormat() {
        return this.defaultFormat;
    }
    public boolean isSeparatorContent() {
        return this.separatorContent;
    }
    public List<String> getLore() {
        return this.lore;
    }
}