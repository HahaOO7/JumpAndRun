package at.haha007.minigames.jumpandrun.gui;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunCheckpoint;
import at.haha007.minigames.jumpandrun.JumpAndRunEditor;
import at.haha007.minigames.jumpandrun.JumpAndRunPlugin;
import at.haha007.minigames.jumpandrun.events.RemoveJnrCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.RemoveJnrCmdEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static at.haha007.edenlib.utils.ItemUtils.getItem;
import static at.haha007.edenlib.utils.ItemUtils.getSkull;

public class JumpAndRunCheckpointEditorGui implements @NotNull Listener {
	private static final String titleCpEditor = ChatColor.GREEN + "Checkpoint Editor";
	private static ItemStack arrow;
	private final Plugin plugin;

	public JumpAndRunCheckpointEditorGui(JavaPlugin plugin) {
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

		arrow = getSkull(
			"ewogICJ0aW1lc3RhbXAiIDogMTU5MDg1NTAyMTg0OCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQy"
				+ "ZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0"
				+ "lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1"
				+ "MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==");
	}


	public static void open(Player player, JumpAndRun jnr, int cpIndex, int moneyMultiplyer) {
		if (!player.hasPermission("jnr.command.use"))
			return;
		if (cpIndex >= jnr.size() || cpIndex < 0)
			return;
		Inventory inv = Bukkit.createInventory(null, 54, titleCpEditor);
		JumpAndRunCheckpoint cp = jnr.getCheckpoint(cpIndex);
		// set yaw/pitch
		ItemStack arrowItem = getArrow();
		ItemMeta arrowMeta = arrowItem.getItemMeta();
		arrowMeta.setDisplayName(ChatColor.GOLD + "Set Direction");
		arrowMeta.setLore(new ArrayList<>(Arrays.asList(ChatColor.AQUA + "Overrides yaw/pitch",
			ChatColor.AQUA + "Yaw:   " + ChatColor.DARK_AQUA + cp.getYaw(),
			ChatColor.AQUA + "Pitch: " + ChatColor.DARK_AQUA + cp.getPitch()
		)));
		arrowItem.setItemMeta(arrowMeta);
		inv.setItem(0, JumpAndRunPlugin.getEditor().getEditorTool(jnr, cpIndex));
		inv.setItem(2, arrowItem);

		// set money
		inv.setItem(4,
			getItem(Material.GOLD_INGOT,
				ChatColor.GOLD + "Money Amount",
				ChatColor.AQUA.toString() + cp.getMoney(),
				ChatColor.AQUA + "Right Click: " + ChatColor.DARK_AQUA + "-",
				ChatColor.AQUA + "Left Click:  " + ChatColor.DARK_AQUA + "+"));
		inv.setItem(5,
			getItem(Material.GOLD_INGOT,
				ChatColor.GOLD + "Money Change",
				ChatColor.AQUA.toString() + ChatColor.DARK_AQUA + Math.pow(10, moneyMultiplyer),
				ChatColor.AQUA + "Multiplyer:  " + ChatColor.DARK_AQUA + moneyMultiplyer,
				ChatColor.AQUA + "Right Click: " + ChatColor.DARK_AQUA + "-",
				ChatColor.AQUA + "Left Click:  " + ChatColor.DARK_AQUA + "+"));
		inv.setItem(8, getItem(Material.BARRIER, ChatColor.RED + "Delete Checkpoint"));
		// commands
		List<String> cmds = cp.getCommands();
		for (int i = 0; i < cmds.size(); i++)
			inv.setItem(i + 9, getItem(Material.PAPER, ChatColor.GOLD.toString() + cmds.get(i), ChatColor.AQUA + "Click to Delete."));
		player.openInventory(inv);
	}

	@EventHandler
	void onInventoryClick(InventoryClickEvent event) {
		if (!titleCpEditor.equals(event.getView().getTitle())) return;
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory()) return;
		ItemStack toolItem = event.getView().getTopInventory().getItem(0);
		if (toolItem == null) return;
		JumpAndRunEditor editor = JumpAndRunPlugin.getEditor();
		JumpAndRun jnr = editor.getJumpAndRun(toolItem);
		int checkpointIndex;
		checkpointIndex = editor.getCheckpoint(toolItem);
		JumpAndRunCheckpoint cp = jnr.getCheckpoint(checkpointIndex);

		switch (event.getSlot()) {
			case 2:
				cp.setRotation(event.getWhoClicked().getLocation().getYaw(), event.getWhoClicked().getLocation().getPitch());
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				break;

			case 0:
				Location loc = new Location(jnr.getWorld(), cp.getPosX() + .5, cp.getPosY(), cp.getPosZ() + .5, cp.getYaw(), cp.getPitch());
				event.getWhoClicked().teleport(loc);
				break;

			case 4:
				ItemStack item = event.getInventory().getItem(5);
				if (item == null) break;
				List<String> lore = item.getItemMeta().getLore();
				if (lore == null) break;
				int moneyDif = Integer.parseInt(lore.get(1).replaceFirst(
					ChatColor.AQUA + "Multiplyer: {2}" + ChatColor.DARK_AQUA,
					""));
				cp.setMoney(cp.getMoney() + (event.getClick().isLeftClick() ? (Math.pow(10, moneyDif)) : -(Math.pow(10, moneyDif))));
				Bukkit.getScheduler().runTaskLater(plugin, () -> open((Player) event.getWhoClicked(), jnr, checkpointIndex, moneyDif), 0);
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				break;

			case 5:
				item = event.getInventory().getItem(5);
				if (item == null) break;
				lore = item.getItemMeta().getLore();
				if (lore == null) break;
				Bukkit.getScheduler().runTaskLater(plugin, () -> open((Player) event.getWhoClicked(), jnr, checkpointIndex,
					Integer.parseInt(lore.get(1).replaceFirst(
						ChatColor.AQUA + "Multiplyer: {2}" + ChatColor.DARK_AQUA,
						"")) + (event.getClick().isLeftClick() ? 1 : -1)), 0);
				break;
			case 8:

				RemoveJnrCheckpointEvent removeJnrCheckpointEvent = new RemoveJnrCheckpointEvent((Player) event.getWhoClicked(), jnr, cp, checkpointIndex);
				Bukkit.getPluginManager().callEvent(removeJnrCheckpointEvent);
				if (removeJnrCheckpointEvent.isCancelled()) break;

				jnr.getCheckpoints().remove(checkpointIndex);
				editor.removePath((Player) event.getWhoClicked());
				editor.displayPath((Player) event.getWhoClicked(), jnr, checkpointIndex);
				event.getWhoClicked().closeInventory();
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				break;
			default:
				if (event.getSlot() < 9) break;
				item = event.getCurrentItem();
				if (item == null) break;
				if (item.getType() != Material.PAPER) break;
				String command = item.getItemMeta().getDisplayName().replaceFirst(ChatColor.GOLD.toString(), "");
				RemoveJnrCmdEvent removeJnrCmdEvent = new RemoveJnrCmdEvent((Player) event.getWhoClicked(), jnr, cp, command);
				Bukkit.getPluginManager().callEvent(removeJnrCmdEvent);
				if (removeJnrCmdEvent.isCancelled()) break;
				cp.getCommands().removeIf(cmd -> cmd.equals(command));
				break;
		}
	}

	private static ItemStack getArrow() {
		return arrow.clone();
	}
}
