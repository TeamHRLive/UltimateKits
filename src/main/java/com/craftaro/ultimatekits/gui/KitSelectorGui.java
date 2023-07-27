package com.craftaro.ultimatekits.gui;

import com.craftaro.core.compatibility.CompatibleMaterial;
import com.craftaro.core.gui.Gui;
import com.craftaro.core.gui.GuiUtils;
import com.craftaro.core.third_party.com.cryptomorin.xseries.XMaterial;
import com.craftaro.core.utils.ItemUtils;
import com.craftaro.core.utils.TextUtils;
import com.craftaro.ultimatekits.UltimateKits;
import com.craftaro.ultimatekits.kit.Kit;
import com.craftaro.ultimatekits.category.Category;
import com.craftaro.ultimatekits.settings.Settings;
import com.craftaro.ultimatekits.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class KitSelectorGui extends Gui {

    private final Player player;
    private final UltimateKits plugin;

    private int timer;
    private final Category category;
    private List<String> kitList;
    private boolean kitsmode = false;

    private final boolean glassless;
    private final int showPerPage;

    public KitSelectorGui(UltimateKits plugin, Player player, Category category) {
        this.player = player;
        this.plugin = plugin;
        this.category = category;
        glassless = Settings.DO_NOT_USE_GLASS_BORDERS.getBoolean();

        setTitle(plugin.getLocale().getMessage("interface.selector.title").getMessage());
        loadKits();
        int showPerRow = glassless ? 9 : 7;
        int nrows = (int) Math.ceil(kitList.size() / (double) showPerRow);
        setRows(glassless ? nrows : nrows + 2);
        showPerPage = showPerRow * (glassless ? (nrows == 6 ? 6 : 5) : 4);
        setPages(kitList.size() / showPerPage);

        setItem(0, 4, GuiUtils.createButtonItem(XMaterial.BOOK,
                plugin.getLocale().getMessage("interface.selector.details")
                        .processPlaceholder("player", player.getName()).getMessage().split("\\|")));

        if (pages > 1) {
            this.setNextPage(rows - 1, 5, GuiUtils.createButtonItem(ItemUtils.getCustomHead("1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b"),
                    plugin.getLocale().getMessage("interface.button.next").getMessage()));

            this.setPrevPage(rows - 1, 3, GuiUtils.createButtonItem(ItemUtils.getCustomHead("3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                    plugin.getLocale().getMessage("interface.button.last").getMessage()));

            this.setOnPage(pager -> showPage());
        }

        if (!glassless) {
            setButton(rows - 1, 4, GuiUtils.createButtonItem(Settings.EXIT_ICON.getMaterial(XMaterial.OAK_DOOR),
                            UltimateKits.getInstance().getLocale().getMessage("interface.button.exit").getMessage()),
                    event -> exit());
        }

        ItemStack glass2 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_2.getMaterial());
        setDefaultItem(AIR);
        mirrorFill(0, 0, true, true, glass2);

        if (!glassless) {
            if (Settings.RAINBOW.getBoolean()) {
                animateGlass();
                timer = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                    if (inventory.getViewers().isEmpty()) return;
                    animateGlass();
                }, 20L, 20L);
                setOnClose(event -> Bukkit.getScheduler().cancelTask(timer));
            } else {
                ItemStack glass1 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_1.getMaterial());
                ItemStack glass3 = GuiUtils.getBorderItem(Settings.GLASS_TYPE_3.getMaterial());
                mirrorFill(0, 0, true, true, glass2);
                mirrorFill(1, 0, true, true, glass2);
                mirrorFill(0, 1, true, true, glass2);
                mirrorFill(0, 2, true, true, glass3);
                mirrorFill(0, 3, false, true, glass1);
            }
        }

        if (category != null)
            setButton(0, 0, GuiUtils.createButtonItem(ItemUtils.getCustomHead("3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23"),
                            plugin.getLocale().getMessage("interface.button.back").getMessage()),
                    event -> guiManager.showGUI(player, new CategorySelectorGui(plugin, player)));


        showPage();
    }

    private void loadKits() {
        kitList = plugin.getKitManager().getKits().stream()
                .filter(kit -> !kit.isHidden() && kit.hasPermissionToPreview(player)
                        && (category == null || kit.getCategory() == category))
                .map(Kit::getKey)
                .collect(Collectors.toList());
    }

    private static final Random rand = new Random();

    private void animateGlass() {
        for (int col = 1; col < 8; ++col) {
            ItemStack it;
            if ((it = getItem(0, col)) == null || it.getType() == Material.AIR || it.getType().name().contains("PANE"))
                setItem(0, col, GuiUtils.getBorderItem(CompatibleMaterial.getGlassPaneForColor(rand.nextInt(16))));
            if ((it = getItem(rows - 1, col)) == null || it.getType() == Material.AIR || it.getType().name().contains("PANE"))
                setItem(rows - 1, col, GuiUtils.getBorderItem(CompatibleMaterial.getGlassPaneForColor(rand.nextInt(16))));
        }
        for (int row = 1; row + 1 < rows; ++row) {
            setItem(row, 0, GuiUtils.getBorderItem(CompatibleMaterial.getGlassPaneForColor(rand.nextInt(16))));
            setItem(row, 8, GuiUtils.getBorderItem(CompatibleMaterial.getGlassPaneForColor(rand.nextInt(16))));
        }
    }

    private void showPage() {
        int index = (page - 1) * showPerPage;
        for (int row = glassless ? 0 : 1; row < (!glassless || pages != 1 ? rows - 1 : rows); ++row) {
            for (int col = glassless ? 0 : 1; col < (glassless ? 9 : 8); ++col) {
                if (index >= kitList.size()) {
                    setItem(row, col, null);
                    clearActions(row, col);
                    continue;
                }
                final String kitItem = kitList.get(index++);
                final Kit kit = plugin.getKitManager().getKit(kitItem);

                String kitTitle = kit.getTitle() != null
                        ? ChatColor.translateAlternateColorCodes('&', kit.getTitle())
                        : plugin.getLocale().getMessage("interface.selector.kit")
                        .processPlaceholder("kit", TextUtils.formatText(kitItem, true)).getMessage();

                setButton(row, col, GuiUtils.createButtonItem(
                                kit.getDisplayItem() != null ? kit.getDisplayItem() : XMaterial.ENCHANTED_BOOK.parseItem(), kitTitle,
                                getKitLore(kit)),
                        event -> {
                            if (event.clickType == ClickType.MIDDLE && player.hasPermission("ultimatekits.admin")) {
                                kitsmode = !kitsmode;
                                showPage();
                            } else if (kitsmode) {
                                if (event.clickType == ClickType.RIGHT) {
                                    plugin.getKitManager().moveKit(kit, true);
                                } else if (event.clickType == ClickType.LEFT) {
                                    plugin.getKitManager().moveKit(kit, false);
                                }
                                loadKits();
                                plugin.saveKits(false);
                                showPage();
                            } else if (event.clickType == ClickType.LEFT) {
                                kit.display(player, guiManager, this);
                            } else if (event.clickType == ClickType.RIGHT) {
                                kit.buy(event.player, event.manager);
                            }
                        });
            }
        }
    }

    private List<String> getKitLore(Kit kit) {
        ArrayList<String> lore = new ArrayList<>();
        if (kit.getPrice() != 0)
            lore.add(plugin.getLocale().getMessage("interface.selector.aboutkitprice")
                    .processPlaceholder("price", String.valueOf(kit.getPrice()))
                    .getMessage());
        else if (kit.getLink() != null)
            lore.add(plugin.getLocale().getMessage("general.type.link").getMessage());

        if (!kitsmode) {
            if (!plugin.getLocale().getMessage("interface.selector.aboutkit").getMessage().trim().equals("")) {
                String[] parts = plugin.getLocale().getMessage("interface.selector.aboutkit").getMessage().split("\\|");
                lore.add("");
                for (String line : parts)
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            if (kit.hasPermissionToClaim(player)) {
                if (kit.getNextUse(player) == -1) {
                    lore.add(plugin.getLocale().getMessage("event.claim.once").getMessage());
                } else if (kit.getNextUse(player) > 0) {
                    if (!plugin.getLocale().getMessage("event.claim.wait").getMessage().trim().equals("")) {
                        lore.add(plugin.getLocale().getMessage("event.claim.wait")
                                .processPlaceholder("time", Methods.makeReadable(kit.getNextUse(player)))
                                .getMessage());
                    }
                } else if (!plugin.getLocale().getMessage("event.claim.ready").getMessage().trim().equals("")) {
                    lore.add(plugin.getLocale().getMessage("event.claim.ready").getMessage());
                }
            } else
                lore.add(plugin.getLocale().getMessage("event.claim.noaccess").getMessage());
            lore.add("");
            lore.add(plugin.getLocale().getMessage("interface.selector.leftpreview").getMessage());
            if (kit.hasPermissionToClaim(player)) {
                lore.add(plugin.getLocale().getMessage("interface.selector.rightclaim").getMessage());
            } else if (kit.getPrice() != 0 || kit.getLink() != null) {
                lore.add(plugin.getLocale().getMessage("interface.selector.rightbuy").getMessage());
            }

            if (player.hasPermission("ultimatekits.admin")) {
                lore.add("");
                lore.add(plugin.getLocale().getMessage("interface.selector.adminlore").getMessage());
            }
        } else {
            lore.addAll(Arrays.asList(plugin.getLocale().getMessage("interface.selector.editlore").getMessage().split("\\|")));
        }
        return lore;
    }

}
