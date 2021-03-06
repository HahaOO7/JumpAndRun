package at.haha007.minigames.jumpandrun.gui;

import at.haha007.edenlib.utils.ItemUtils;
import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunCheckpoint;
import at.haha007.minigames.jumpandrun.JumpAndRunPlayer;
import at.haha007.minigames.jumpandrun.JumpAndRunPlugin;
import at.haha007.minigames.jumpandrun.events.ChangeJnrCheckpointEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JumpAndRunCheckpointGui implements @NotNull Listener {
	private static final String titleCheckpointMenu = ChatColor.GREEN + "JNR Checkpoint Menu";
	private static ItemStack arrowRight;
	private static ItemStack arrowLeft;
	private final Plugin plugin;

	public JumpAndRunCheckpointGui(JavaPlugin plugin) {
		this.plugin = plugin;
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

	public static void open(Player player, JumpAndRun jnr) {
		open(player, jnr, 0);
	}

	private static void open(Player player, JumpAndRun jnr, int page) {
		Inventory inv = Bukkit.createInventory(null, 54, titleCheckpointMenu);
		if (page < 0) page = jnr.size() / 45;
		if (page > jnr.size() / 45) page = 0;
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayer(player.getUniqueId());
		int reached = jnrPlayer.getMaxCheckpoint(jnr);
		for (int i = 0; i < 45; i++) {
			int cpIndex = i + 45 * page;
			JumpAndRunCheckpoint cp = jnr.getCheckpoint(cpIndex);
			if (cp == null) break;
			ItemStack cpItem = ItemUtils.setNbtInt(ItemUtils.getItem(cpIndex > reached ? Material.DIRT : Material.GRASS_BLOCK, ChatColor.GOLD + "CP " + cpIndex), "index", cpIndex);
			inv.setItem(i, cpItem);
		}

		ItemStack editorTool = JumpAndRunPlugin.getEditor().getEditorTool(jnr, 0);
		ItemMeta itemMeta = editorTool.getItemMeta();
		List<String> lore = itemMeta.getLore();
		lore.add(ChatColor.AQUA + "Set leave spawnpoint.");
		itemMeta.setLore(lore);
		editorTool.setItemMeta(itemMeta);
		if (player.hasPermission("jnr.command.use"))
			inv.setItem(49, ItemUtils.setNbtInt(editorTool, "page", page));
		ItemStack left = ItemUtils.setNbtString(ItemUtils.setNbtInt(getArrowLeft(), "page", page), "jnr", jnr.getName());
		ItemStack highlight = ItemUtils.getItem(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, ChatColor.GOLD + "Show next checkpoint", ChatColor.AQUA.toString() + jnr.shouldHighlightNextCheckpoint());
		if (jnr.shouldHighlightNextCheckpoint())
			highlight = ItemUtils.setGlow(highlight, true);

		inv.setItem(45, left);
		inv.setItem(53, getArrowRight());
		inv.setItem(51, highlight);

		player.openInventory(inv);
		if (JumpAndRunPlugin.getPlayerIfActive(player) != null)
			Bukkit.getScheduler().runTaskLater(JumpAndRunPlugin.getInstance(), jnrPlayer::fillJnrInventory, 1);
	}

	private static ItemStack getArrowRight() {
		return arrowRight.clone();
	}

	private static ItemStack getArrowLeft() {
		return arrowLeft.clone();
	}


	@EventHandler(priority = EventPriority.LOW)
	void onInventoryClick(InventoryClickEvent event) {
		if (!event.getView().getTitle().equals(titleCheckpointMenu)) return;

		Player player = (Player) event.getWhoClicked();
		event.setCancelled(true);
		if (event.getClickedInventory() != event.getView().getTopInventory()) return;
		int page = ItemUtils.getNbtInt(event.getInventory().getItem(45), "page");
		JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(ItemUtils.getNbtString(event.getInventory().getItem(45), "jnr"));
		if (jnr == null) {
			player.closeInventory();
			return;
		}

		switch (event.getSlot()) {
			case 53:
				Bukkit.getScheduler().runTaskLater(plugin, () -> open(player, jnr, page + 1), 0);
				break;
			case 49:
				if (!player.hasPermission("jnr.command.use")) break;
				jnr.setLeavePoint(player.getLocation());
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				break;
			case 45:
				Bukkit.getScheduler().runTaskLater(plugin, () -> open(player, jnr, page - 1), 0);
				break;
			case 51:
				jnr.setHighlightNextCheckpoint(!jnr.shouldHighlightNextCheckpoint());
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				Bukkit.getScheduler().runTask(plugin, () -> open(player, jnr, page));
				break;
			default:
				if (event.getSlot() >= 45)
					break;
				ItemStack cpItem = event.getCurrentItem();
				if (cpItem == null) break;
				JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive(player);
				if (cpItem.getType() == Material.DIRT && jnrPlayer != null) break;
				int cp = ItemUtils.getNbtInt(cpItem, "index");
				if (jnrPlayer != null) {
					ChangeJnrCheckpointEvent ev = new ChangeJnrCheckpointEvent(jnr, jnrPlayer, player, jnrPlayer.getActiveCheckPointIndex(jnr), cp);
					Bukkit.getServer().getPluginManager().callEvent(ev);
					if (ev.isCancelled()) break;
					jnrPlayer.setCheckpoint(jnr, ev.getTo());
					jnrPlayer.respawn();
					break;
				}
				JumpAndRunCheckpoint checkpoint = jnr.getCheckpoint(cp);
				player.teleport(new Location(jnr.getWorld(), checkpoint.getPosX() + .5, checkpoint.getPosY(), checkpoint.getPosZ(), checkpoint.getYaw(), checkpoint.getPitch()));
				break;
		}
	}
}
