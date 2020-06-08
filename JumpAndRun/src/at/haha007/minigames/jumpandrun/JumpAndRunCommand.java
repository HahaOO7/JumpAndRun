package at.haha007.minigames.jumpandrun;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public class JumpAndRunCommand implements CommandExecutor, TabCompleter, Listener {
	//	private final String titleCreateJnr = ChatColor.GREEN + "Create JNR";
	private final String titleMainMenu = ChatColor.GREEN + "JNR menu";
	private final String titleCheckpointMenu = ChatColor.GREEN + "JNR Checkpoint Menu";

	// create jnr
	// get jnr tool
	// teleport to jnr

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player))
			return false;
		if (!sender.hasPermission("jnr.command.execute"))
			return false;
		Player player = (Player) sender;
		if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
			if (JumpAndRunPlugin.getJumpAndRun(args[1]) != null) {
				sender.sendMessage(ChatColor.RED + "Dieses JNR existiert bereits.");
				return true;
			}
			Location loc = player.getLocation();
			JumpAndRun jnr = new JumpAndRun(
				ChatColor.translateAlternateColorCodes('&', args[1]),
				player.getWorld(),
				Collections.singletonList(
					new JumpAndRunCheckpoint(
						loc.getBlockX(),
						loc.getBlockY(),
						loc.getBlockZ(),
						0f,
						0f,
						null,
						0d)),
				new HashMap<>());
			JumpAndRunPlugin.getJumpAndRuns().add(jnr);
			JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
			sender.sendMessage(ChatColor.GOLD + "JNR erstellt.");
			return true;
		}
		if (args.length > 1 && args[0].equalsIgnoreCase("addcmd")) {
			ItemStack item = player.getInventory().getItemInMainHand();
			JumpAndRunEditor editor = JumpAndRunPlugin.getEditor();
			if (!editor.isEditorTool(item)) {
				sender.sendMessage(ChatColor.RED + "Du benötigst ein JNR tool in der Hand.");
				return true;
			}
			String cmd = Utils.combineStrings(1, args.length - 1, args);
			JumpAndRun jnr = editor.getJumpAndRun(item);
			if (jnr == null)
				return true;
			int cp = editor.getCheckpoint(item);
			jnr.getCheckpoint(cp).getCommands().add(cmd);
			JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
			sender.sendMessage(ChatColor.GOLD + "Command hinzugefügt");
			return true;
		}
		openMainJnrMenu((Player) sender, 0);
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (args.length == 1) {
			List<String> cmds = new ArrayList<>(Arrays.asList("create", "addcmd"));
			return cmds.stream().filter(str -> str.toLowerCase().startsWith(args[0])).collect(Collectors.toList());
		}
		return null;
	}

	private void openMainJnrMenu(Player player, int page) {
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
	}

	private void handleMainMenuClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory())
			return;
		int page = Utils.getNbtInt(event.getInventory().getItem(49), "page");
		switch (event.getSlot()) {
			case 45:
				openMainJnrMenu((Player) event.getWhoClicked(), page - 1);
				break;
			case 53:
				openMainJnrMenu((Player) event.getWhoClicked(), page + 1);
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
					Utils.giveItem((Player) event.getWhoClicked(),
						JumpAndRunPlugin.getEditor().getEditorTool(jnr, jnr.size()));
				} else if (event.getClick() == ClickType.LEFT) {
					ItemStack item = event.getCurrentItem();
					if (item == null)
						break;
					String itemName = item.getItemMeta().getDisplayName();
					String jnrName = itemName.replaceFirst(ChatColor.GREEN.toString(), "");
					JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(jnrName);
					if (jnr == null)
						break;
					openJnrMenu((Player) event.getWhoClicked(), jnr, 0);
				}
				break;
		}
	}

	private void openJnrMenu(Player player, JumpAndRun jnr, int page) {
		Inventory inv = Bukkit.createInventory(null, 54, titleCheckpointMenu);
		if (page < 0) page = jnr.size() / 45;
		if (page > jnr.size() / 45) page = 0;
		int reached = JumpAndRunPlugin.getPlayer(player.getUniqueId()).getMaxCheckpoint(jnr);
		for (int i = 0; i < 45; i++) {
			int cpIndex = i + 54 * page;
			JumpAndRunCheckpoint cp = jnr.getCheckpoint(cpIndex);
			if (cp == null) break;
			ItemStack cpItem = Utils.setNbtInt(Utils.getItem(cpIndex > reached ? Material.DIRT : Material.GRASS_BLOCK, ChatColor.GOLD + "CP " + cpIndex), "index", cpIndex);
			inv.setItem(i, cpItem);
		}

		inv.setItem(49, Utils.setNbtInt(JumpAndRunPlugin.getEditor().getEditorTool(jnr, 0), "page", page));

		inv.setItem(45, getArrowLeft());
		inv.setItem(53, getArrowRight());

		player.openInventory(inv);
	}

	private void handleCheckpointMenuClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory()) return;
		int page = Utils.getNbtInt(event.getInventory().getItem(49), "page");
		ItemStack tool = event.getInventory().getItem(49);
		if (tool == null) return;
		JumpAndRun jnr = JumpAndRunPlugin.getEditor().getJumpAndRun(tool);

		switch (event.getSlot()) {
			case 53:
				openJnrMenu((Player) event.getWhoClicked(), jnr, page + 1);
				break;
			case 45:
				openJnrMenu((Player) event.getWhoClicked(), jnr, page - 1);
				break;
			default:
				if (event.getSlot() >= 45)
					break;
				ItemStack cpItem = event.getCurrentItem();
				if (cpItem == null) break;
				if (cpItem.getType() == Material.DIRT) break;
				int cp = Utils.getNbtInt(cpItem, "index");
				JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayer(event.getWhoClicked().getUniqueId());
				jnrPlayer.setActiveJnr(jnr);
				jnrPlayer.setCheckpoint(jnr, cp);
				break;
		}
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		String title = event.getView().getTitle();
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
