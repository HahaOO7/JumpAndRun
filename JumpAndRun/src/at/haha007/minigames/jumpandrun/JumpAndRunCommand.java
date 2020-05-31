package at.haha007.minigames.jumpandrun;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class JumpAndRunCommand implements CommandExecutor, TabCompleter, Listener {
//	private final String titleCreateJnr = ChatColor.GREEN + "Create JNR";
	private final String titleMainMenu = ChatColor.GREEN + "JNR menu";
	private final String titleCheckpointMenu = ChatColor.GREEN + "JNR Checkpoint Menu";

	// create jnr
	// get jnr tool
	// teleport to jnr

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		if (!sender.hasPermission("jnr.command.execute"))
			return false;
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("create")) {
				if (JumpAndRunPlugin.getJumpAndRun(args[1]) != null) {
					sender.sendMessage(ChatColor.RED + "Dieses JNR existiert bereits.");
					return true;
				}
				JumpAndRun jnr = new JumpAndRun(
						ChatColor.translateAlternateColorCodes('&', args[1]),
						((Player) sender).getWorld(),
						Arrays.asList(new JumpAndRunCheckpoint[] {}),
						new HashMap<>());
				JumpAndRunPlugin.getJumpAndRuns().add(jnr);
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				sender.sendMessage(ChatColor.GOLD + "JNR erstellt.");
				return true;
			}
			return true;
		}
		openMainJnrMenu((Player) sender, 0);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	Inventory openMainJnrMenu(Player player, int page) {
		Inventory inv = Bukkit.createInventory(null, 54, titleMainMenu);
		// list jnr's fist 5 lines
		List<JumpAndRun> jnrs = Arrays.asList(JumpAndRunPlugin.getJumpAndRuns().toArray(new JumpAndRun[0]));

		if (page < jnrs.size() / 45)
			page = 0;
		if (page < 0)
			page = jnrs.size() / 45;

		jnrs.sort((a, b) -> a.getName().compareTo(b.getName()));
		for (int i = 0; i < 45; i++) {
			int index = page * 45 + i;
			if (index > jnrs.size() - 1)
				break;
			JumpAndRun jnr = jnrs.get(index);
			inv.setItem(index, Utils.getItem(
					Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
					ChatColor.GREEN + jnr.getName(),
					ChatColor.GOLD + "Checkpoints: " + ChatColor.AQUA + jnr.size(),
					ChatColor.GOLD + "World: " + jnr.getWorld()));

		}

		// next page, create jnr etc last line
		inv.setItem(45, getArrowLeft());
		inv.setItem(53, getArrowRight());
		player.openInventory(inv);
		return inv;
	}

	private void handleMainMenuClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory())
			return;
		int page = Utils.getNbtInt(event.getInventory().getItem(49), "page");
		switch (event.getSlot()) {
		case 45:
			openMainJnrMenu((Player) event.getWhoClicked(), page--);
			break;
		case 53:
			openMainJnrMenu((Player) event.getWhoClicked(), page++);
			break;

		default:
			if (event.getClick() == ClickType.RIGHT) {
				ItemStack item = event.getCurrentItem();
				if (item == null)
					break;
				String itemName = item.getItemMeta().getDisplayName();
				if (itemName == null)
					break;
				String jnrName = itemName.replaceFirst(ChatColor.GREEN.toString(), "");
				JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(jnrName);
				if (jnr == null)
					break;
				Utils.giveItem((Player) event.getWhoClicked(),
						JumpAndRunPlugin.getEditor().getEditorTool(jnr, jnr.size()));
			} else if (event.getClick() == ClickType.LEFT) {
				ItemStack item = event.getCurrentItem();
				if (item == null)
					break;
				String itemName = item.getItemMeta().getDisplayName();
				if (itemName == null)
					break;
				String jnrName = itemName.replaceFirst(ChatColor.GREEN.toString(), "");
				JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(jnrName);
				if (jnr == null)
					break;
				openCheckpointMenu((Player) event.getWhoClicked(), jnr);
			}
			break;
		}
	}

	Inventory openCheckpointMenu(Player player, JumpAndRun jnr) {
		Inventory inv = Bukkit.createInventory(null, 54, titleCheckpointMenu);
		player.openInventory(inv);
		return inv;
	}

	private void handleCheckpointMenuClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory())
			return;
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		String title = event.getView().getTitle();
		if (title == null)
			return;
		if (title.equals(titleCheckpointMenu))
			handleCheckpointMenuClick(event);
		else if (title.equals(titleMainMenu))
			handleMainMenuClick(event);
	}

	private ItemStack getArrowRight() {
		ItemStack item = Utils.getSkull(
				"ewogICJ0aW1lc3RhbXAiIDogMTU5MDg1NTAyMTg0OCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQy"
						+ "ZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0"
						+ "lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1"
						+ "MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Next Page");
		item.setItemMeta(meta);
		return item;
	}

	private ItemStack getArrowLeft() {
		ItemStack item = Utils.getSkull(
				"ewogICJ0aW1lc3RhbXAiIDogMTU5MDg1NTI4OTM2MiwKICAicHJvZmlsZUlkIiA6ICJhNjhmMGI2NDhkMTQ0MDAwYTk1ZjRiOWJh"
						+ "MTRmOGRmOSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dMZWZ0IiwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU"
						+ "4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y3YWFjYWQxOTNl"
						+ "MjIyNjk3MWVkOTUzMDJkYmE0MzM0MzhiZTQ2NDRmYmFiNWViZjgxODA1NDA2MTY2N2ZiZTIiCiAgICB9CiAgfQp9");
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Previous Page");
		item.setItemMeta(meta);
		return item;
	}
}
