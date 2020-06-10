package at.haha007.minigames.jumpandrun;

import at.haha007.minigames.jumpandrun.events.AddJnrCmdEvent;
import at.haha007.minigames.jumpandrun.events.CreateJnrEvent;
import at.haha007.minigames.jumpandrun.events.DeleteJnrEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.*;
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
	private final String[] explanation = {
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

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			if (!(sender instanceof BlockCommandSender))
				return false;
			BlockCommandSender block = (BlockCommandSender) sender;
			JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(args[0]);
			if (jnr == null) return false;
			double radius;
			try {
				radius = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				return false;
			}
			Collection<Player> players = block.getBlock().getLocation().getNearbyPlayers(radius);
			players.forEach(player -> JumpAndRunPlugin.startJumpAndRun(jnr, player));
			return true;
		}
		Player player = (Player) sender;

		if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {

			JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(args[1]);
			if (jnr == null) {
				sender.sendMessage(ChatColor.RED + "[JNR] JumpAndRun wurde nicht gefunden.");
				return true;
			}

			DeleteJnrEvent deleteJnrEvent = new DeleteJnrEvent(player, jnr);
			Bukkit.getPluginManager().callEvent(deleteJnrEvent);
			if (deleteJnrEvent.isCancelled()) return true;

			JumpAndRunPlugin.delete(jnr);
			sender.sendMessage(ChatColor.GOLD + "[JNR] JumpAndRun wurde entfernt.");
			return true;
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
			if (JumpAndRunPlugin.getJumpAndRun(args[1]) != null) {
				sender.sendMessage(ChatColor.RED + "Dieses JNR existiert bereits.");
				return true;
			}
			Location loc = player.getLocation();
			JumpAndRun jnr = new JumpAndRun(
				ChatColor.translateAlternateColorCodes('&', args[1]),
				loc,
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

			CreateJnrEvent createJnrEvent = new CreateJnrEvent(player, jnr);
			Bukkit.getPluginManager().callEvent(createJnrEvent);
			if (createJnrEvent.isCancelled()) return true;

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
			JumpAndRunCheckpoint checkpoint = jnr.getCheckpoint(cp);

			AddJnrCmdEvent addJnrCmdEvent = new AddJnrCmdEvent(player, jnr, checkpoint, cmd);
			Bukkit.getPluginManager().callEvent(addJnrCmdEvent);
			if (addJnrCmdEvent.isCancelled()) return true;
			cmd = addJnrCmdEvent.getCommand();

			checkpoint.getCommands().add(cmd);
			JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
			sender.sendMessage(ChatColor.GOLD + "Command hinzugefügt");
			return true;
		}
		openMainJnrMenu((Player) sender, 0);
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (sender instanceof BlockCommandSender) {
			if (args.length == 1)
				return JumpAndRunPlugin.getJumpAndRuns().stream().map(JumpAndRun::getName).filter(str -> str.toLowerCase().startsWith(args[0])).collect(Collectors.toList());
		}
		if (args.length == 1) {
			List<String> cmds = new ArrayList<>(Arrays.asList("create", "addcmd", "delete"));
			return cmds.stream().filter(str -> str.toLowerCase().startsWith(args[0])).collect(Collectors.toList());
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("delete"))
			return JumpAndRunPlugin.getJumpAndRuns().stream().map(JumpAndRun::getName).filter(str -> str.toLowerCase().startsWith(args[1])).collect(Collectors.toList());

		return Collections.emptyList();
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
		inv.setItem(46, Utils.getItem(Material.BOOK, "Explanation", explanation));
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
			case 46:
				for (String str : explanation) {
					event.getWhoClicked().sendMessage(ChatColor.GOLD + "[JNR] " + str);
				}
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
					Utils.giveItem((Player) event.getWhoClicked(), JumpAndRunPlugin.getEditor().getEditorTool(jnr, jnr.size()));
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
					openJnrMenu((Player) event.getWhoClicked(), jnr, 0);
				}
				break;
		}
	}

	public void openJnrMenu(Player player, JumpAndRun jnr, int page) {
		Inventory inv = Bukkit.createInventory(null, 54, titleCheckpointMenu);
		if (page < 0) page = jnr.size() / 45;
		if (page > jnr.size() / 45) page = 0;
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayer(player.getUniqueId());
		int reached = jnrPlayer.getMaxCheckpoint(jnr);
		for (int i = 0; i < 45; i++) {
			int cpIndex = i + 54 * page;
			JumpAndRunCheckpoint cp = jnr.getCheckpoint(cpIndex);
			if (cp == null) break;
			ItemStack cpItem = Utils.setNbtInt(Utils.getItem(cpIndex > reached ? Material.DIRT : Material.GRASS_BLOCK, ChatColor.GOLD + "CP " + cpIndex), "index", cpIndex);
			inv.setItem(i, cpItem);
		}

		ItemStack editorTool = JumpAndRunPlugin.getEditor().getEditorTool(jnr, 0);
		ItemMeta itemMeta = editorTool.getItemMeta();
		List<String> lore = itemMeta.getLore();
		lore.add(ChatColor.AQUA + "Set leave spawnpoint.");
		itemMeta.setLore(lore);
		editorTool.setItemMeta(itemMeta);
		if (player.hasPermission("jnr.command.use"))
			inv.setItem(49, Utils.setNbtInt(editorTool, "page", page));
		ItemStack left = Utils.setNbtString(Utils.setNbtInt(getArrowLeft(), "page", page), "jnr", jnr.getName());

		inv.setItem(45, left);
		inv.setItem(53, getArrowRight());

		player.openInventory(inv);
	}

	private void handleJnrMenuClick(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory()) return;
		int page = Utils.getNbtInt(event.getInventory().getItem(45), "page");
		JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(Utils.getNbtString(event.getInventory().getItem(45), "jnr"));
		if (jnr == null) {
			event.getWhoClicked().closeInventory();
			return;
		}

		switch (event.getSlot()) {
			case 53:
				openJnrMenu((Player) event.getWhoClicked(), jnr, page + 1);
				break;
			case 49:
				if (!event.getWhoClicked().hasPermission("jnr.command.use")) break;
				jnr.setLeavePoint(event.getWhoClicked().getLocation());
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
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
				JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive((Player) event.getWhoClicked());
				if (jnrPlayer != null) {
					jnrPlayer.setCheckpoint(jnr, cp);
					jnrPlayer.respawn();
					break;
				}
				JumpAndRunCheckpoint checkpoint = jnr.getCheckpoint(cp);
				event.getWhoClicked().teleport(new Location(jnr.getWorld(), checkpoint.getPosX() + .5, checkpoint.getPosY(), checkpoint.getPosZ(), checkpoint.getYaw(), checkpoint.getPitch()));
				break;
		}
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		String title = event.getView().getTitle();
		if (title.equals(titleCheckpointMenu))
			handleJnrMenuClick(event);
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
