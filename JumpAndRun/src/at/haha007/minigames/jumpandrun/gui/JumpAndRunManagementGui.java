package at.haha007.minigames.jumpandrun.gui;

import at.haha007.edenlib.utils.ItemUtils;
import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class JumpAndRunManagementGui implements Listener {
	private static ItemStack arrowRight;
	private static ItemStack arrowLeft;
	private static final String titleMainMenu = ChatColor.GREEN + "JNR menu";
	private static final String[] explanation = {
		ChatColor.DARK_AQUA + "Leftclick a JNR to open its Inventory.",
		ChatColor.DARK_AQUA + "Rightclick a JNR to get the JNR tool.",
		ChatColor.DARK_AQUA + "Shift+Scroll to select the wantet Checkpoint.",
		ChatColor.DARK_AQUA + "Rightclick with the tool to ad a checkpoint",
		ChatColor.DARK_AQUA + "  in between the red and the blue checkpoint.",
		ChatColor.DARK_AQUA + "Leftclick with the tool to edit the blue Checkpoint",
		ChatColor.DARK_AQUA + "Player Commands:",
		ChatColor.DARK_AQUA + "Add a command at blue Checkpoint: ",
		ChatColor.AQUA + "  /jnr addcmd <cmd>",
		ChatColor.AQUA + "  Use %player% for the JNR player.",
		ChatColor.DARK_AQUA + "Create a new JNR: ",
		ChatColor.AQUA + "  /jnr create <name>",
		ChatColor.DARK_AQUA + "Delete an existing JNR: ",
		ChatColor.AQUA + "  /jnr delete <name>",
		ChatColor.DARK_AQUA + "CommandBlock:",
		ChatColor.DARK_AQUA + "Start JNR: ",
		ChatColor.AQUA + "  /jnr <name> <radius>"
	};

	public JumpAndRunManagementGui(JavaPlugin plugin) {
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);


		arrowRight = ItemUtils.getSkull(
			"ewogICJ0aW1lc3RhbXAiIDogMTU5MDg1NTAyMTg0OCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQy"
				+ "ZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0"
				+ "lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1"
				+ "MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==");
		ItemMeta rightMeta = arrowRight.getItemMeta();
		rightMeta.setDisplayName(ChatColor.GOLD + "Next Page");
		arrowRight.setItemMeta(rightMeta);

		arrowLeft = ItemUtils.getSkull(
			"ewogICJ0aW1lc3RhbXAiIDogMTU5MDg1NTI4OTM2MiwKICAicHJvZmlsZUlkIiA6ICJhNjhmMGI2NDhkMTQ0MDAwYTk1ZjRiOWJh"
				+ "MTRmOGRmOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dMZWZ0IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU"
				+ "4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y3YWFjYWQxOTNl"
				+ "MjIyNjk3MWVkOTUzMDJkYmE0MzM0MzhiZTQ2NDRmYmFiNWViZjgxODA1NDA2MTY2N2ZiZTIiCiAgICB9CiAgfQp9");
		ItemMeta leftMeta = arrowLeft.getItemMeta();
		leftMeta.setDisplayName(ChatColor.GOLD + "Previous Page");
		arrowLeft.setItemMeta(leftMeta);

	}

	public static void open(Player player) {
		open(player, 0);
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		if (!event.getView().getTitle().equals(titleMainMenu)) return;
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory())
			return;
		int page = ItemUtils.getNbtInt(event.getInventory().getItem(49), "page");
		switch (event.getSlot()) {
			case 45:
				open((Player) event.getWhoClicked(), page - 1);
				break;
			case 46:
				for (String str : explanation) {
					event.getWhoClicked().sendMessage(ChatColor.GOLD + "[JNR] " + str);
				}
				break;
			case 53:
				open((Player) event.getWhoClicked(), page + 1);
				break;

			default:
				if (event.getClick() == ClickType.RIGHT) {
					ItemStack item = event.getCurrentItem();
					if (item == null)
						break;
					String itemName = item.getItemMeta().getDisplayName();
					String jnrName = itemName.replaceFirst(ChatColor.GREEN.toString(), "");
					JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(jnrName);
					if (jnr == null)
						break;
					ItemUtils.giveItem((Player) event.getWhoClicked(), JumpAndRunPlugin.getEditor().getEditorTool(jnr, jnr.size()));
					JumpAndRunPlugin.getEditor().displayPath((Player) event.getWhoClicked(), jnr, 0);
				} else if (event.getClick() == ClickType.LEFT) {
					ItemStack item = event.getCurrentItem();
					if (item == null)
						break;
					String itemName = item.getItemMeta().getDisplayName();
					String jnrName = itemName.replaceFirst(ChatColor.GREEN.toString(), "");
					JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(jnrName);
					if (jnr == null)
						break;
					JumpAndRunCheckpointGui.open((Player) event.getWhoClicked(), jnr);
				}
				break;
		}
	}

	private static void open(Player player, int page) {
		Inventory inv = Bukkit.createInventory(null, 54, titleMainMenu);
		List<JumpAndRun> jnrList = Arrays.asList(JumpAndRunPlugin.getJumpAndRuns().toArray(new JumpAndRun[0]));
		if (page > jnrList.size() / 45)
			page = 0;
		if (page < 0)
			page = jnrList.size() / 45;

		jnrList.sort(Comparator.comparing(JumpAndRun::getName));
		for (int i = 0; i < 45; i++) {
			int index = page * 45 + i;
			if (index > jnrList.size() - 1)
				break;
			JumpAndRun jnr = jnrList.get(index);
			inv.setItem(index, ItemUtils.getItem(
				Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
				ChatColor.GREEN + jnr.getName(),
				ChatColor.GOLD + "Checkpoints: " + ChatColor.AQUA + jnr.size(),
				ChatColor.GOLD + "World: " + jnr.getWorld()));

		}

		// next page, create jnr etc last line
		inv.setItem(45, getArrowLeft());
		inv.setItem(46, ItemUtils.getItem(Material.BOOK, "Explanation", explanation));
		inv.setItem(53, getArrowRight());
		player.openInventory(inv);
	}

	private static ItemStack getArrowRight() {
		return arrowRight.clone();
	}

	private static ItemStack getArrowLeft() {
		return arrowLeft.clone();
	}


}
