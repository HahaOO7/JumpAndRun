package at.haha007.minigames.jumpandrun;

import at.haha007.edenlib.utils.Utils;
import at.haha007.minigames.jumpandrun.events.AddJnrCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.RemoveJnrCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.RemoveJnrCmdEvent;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.EnumChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static at.haha007.edenlib.utils.ItemUtils.getItem;
import static at.haha007.edenlib.utils.ItemUtils.getSkull;


public class JumpAndRunEditor implements Listener {
	private final HashSet<Player> cooldownPlayers = new HashSet<>();
	private final HashMap<Player, List<Integer>> entityIDs = new HashMap<>();
	private final Random rand = new Random();
	private final String titleCpEditor = ChatColor.GREEN + "Checkpoint Editor";

	@EventHandler
	void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		if (!e.getPlayer().hasPermission("jnr.editor.use"))
			return;
		ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
		if (!isEditorTool(item))
			return;
		e.setCancelled(true);
		JumpAndRun jnr = getJumpAndRun(item);
		if (jnr == null)
			return;
		int checkpoint = getCheckpoint(item);
		if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
			// Edit Checkpoint
			openCheckpointEditor(e.getPlayer(), jnr, checkpoint, 1);
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Insert Checkpoint
			Player player = e.getPlayer();
			if (!cooldownPlayers.contains(player)) {
				insertCheckpoint(jnr, checkpoint, player, e.getClickedBlock());
				cooldownPlayers.add(player);
				Bukkit.getScheduler().runTaskLater(JumpAndRunPlugin.getInstance(), () -> {
					cooldownPlayers.remove(player);
				}, 10);
			}
		}
	}

	private void insertCheckpoint(JumpAndRun jnr, int index, Player player, Block block) {
		JumpAndRunCheckpoint cp =
			new JumpAndRunCheckpoint(
				block.getX(),
				block.getY() + 1,
				block.getZ(),
				0f,
				0f,
				null,
				0d);
		AddJnrCheckpointEvent addJnrCheckpointEvent = new AddJnrCheckpointEvent(jnr, cp, index, player);
		Bukkit.getPluginManager().callEvent(addJnrCheckpointEvent);
		if (addJnrCheckpointEvent.isCancelled()) return;
		jnr.addCheckpoint(cp, index);
		block.getRelative(BlockFace.UP).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
		player.getInventory().setItemInMainHand(getEditorTool(jnr, index + 1));
		JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
		removePath(player);
		displayPath(player, jnr, index + 1);
	}

	@EventHandler
	void onSlotChange(PlayerItemHeldEvent e) {
		if (!e.getPlayer().hasPermission("jnr.editor.use"))
			return;
		if (!e.getPlayer().isSneaking()) {
			removePath(e.getPlayer());
			ItemStack prevItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
			if (isEditorTool(prevItem)) {
				JumpAndRun jnr = getJumpAndRun(prevItem);
				if (jnr != null)
					jnr.getCheckpoints().forEach(
						jumpAndRunCheckpoint ->
							e.getPlayer().sendBlockChange(
								jumpAndRunCheckpoint.getPos().toLocation(e.getPlayer().getWorld()),
								e.getPlayer().getWorld().getBlockAt(jumpAndRunCheckpoint.getPosX(), jumpAndRunCheckpoint.getPosY(), jumpAndRunCheckpoint.getPosZ()).getBlockData()));
			}
			ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
			if (!isEditorTool(item))
				return;
			JumpAndRun jnr = getJumpAndRun(item);
			if (jnr == null)
				return;
			displayPath(e.getPlayer(), jnr, getCheckpoint(item));
			return;
		}
		int prev = e.getPreviousSlot();
		ItemStack item = e.getPlayer().getInventory().getItem(prev);
		if (!isEditorTool(item)) {
			item = e.getPlayer().getInventory().getItem(e.getNewSlot());
			if (isEditorTool(item)) {
				assert item != null;
				displayPath(e.getPlayer(), getJumpAndRun(item), getCheckpoint(item));
			}
			return;
		}
		e.setCancelled(true);
		assert item != null;
		JumpAndRun jnr = getJumpAndRun(item);
		if (jnr == null)
			return;
		int cp = getCheckpoint(item);
		int next = e.getNewSlot();
		int change = (prev - next + 9) % 9;
		int size = jnr.size() + 1;
		if (change == 8) {
			e.getPlayer().getInventory().setItemInMainHand(getEditorTool(jnr, (cp + 1) % size));
			removePath(e.getPlayer());
			displayPath(e.getPlayer(), jnr, getCheckpoint(e.getPlayer().getInventory().getItemInMainHand()));
			return;
		}
		if (change == 1) {
			e.getPlayer().getInventory().setItemInMainHand(getEditorTool(jnr, (cp - 1 + size) % size));
			removePath(e.getPlayer());
			displayPath(e.getPlayer(), jnr, getCheckpoint(e.getPlayer().getInventory().getItemInMainHand()));
		}
	}

	public ItemStack getEditorTool(JumpAndRun jnr, int checkpoint) {
		return getItem(
			Material.NETHER_STAR, ChatColor.GOLD + "JNR Tool",
			ChatColor.DARK_AQUA + "Left: " + ChatColor.AQUA + "edit Checkpoint",
			ChatColor.DARK_AQUA + "Right: " + ChatColor.AQUA + "add Checkpoint",
			ChatColor.DARK_AQUA + "Checkpoint: " + ChatColor.AQUA + checkpoint,
			ChatColor.DARK_AQUA + "JumpAndRun: " + ChatColor.AQUA + jnr.getName());
	}

	public boolean isEditorTool(ItemStack item) {
		try {
			return item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "JNR Tool");
		} catch (NullPointerException e) {
			return false;
		}
	}

	public void openCheckpointEditor(Player player, JumpAndRun jnr, int cpIndex, int moneyMultiplyer) {
		if (!player.hasPermission("jnr.command.use"))
			return;
		if (cpIndex >= jnr.size() || cpIndex < 0)
			return;
		Inventory inv = Bukkit.createInventory(null, 54, titleCpEditor);
		JumpAndRunCheckpoint cp = jnr.getCheckpoint(cpIndex);
		// set yaw/pitch
		ItemStack arrowItem = getSkull(
			"ewogICJ0aW1lc3RhbXAiIDogMTU5MDg1NTAyMTg0OCwKICAicHJvZmlsZUlkIiA6ICI1MGM4NTEwYjVlYTA0ZDYwYmU5YTdkNTQy"
				+ "ZDZjZDE1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfQXJyb3dSaWdodCIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0"
				+ "lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kMzRlZjA2Mzg1"
				+ "MzcyMjJiMjBmNDgwNjk0ZGFkYzBmODVmYmUwNzU5ZDU4MWFhN2ZjZGYyZTQzMTM5Mzc3MTU4IgogICAgfQogIH0KfQ==");
		ItemMeta arrowMeta = arrowItem.getItemMeta();
		arrowMeta.setDisplayName(ChatColor.GOLD + "Set Direction");
		arrowMeta.setLore(new ArrayList<>(Arrays.asList(ChatColor.AQUA + "Overrides yaw/pitch",
			ChatColor.AQUA + "Yaw:   " + ChatColor.DARK_AQUA + cp.getYaw(),
			ChatColor.AQUA + "Pitch: " + ChatColor.DARK_AQUA + cp.getPitch()
		)));
		arrowItem.setItemMeta(arrowMeta);
		inv.setItem(0, getEditorTool(jnr, cpIndex));
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
		JumpAndRun jnr = getJumpAndRun(toolItem);
		int checkpointIndex;
		checkpointIndex = getCheckpoint(toolItem);
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
				openCheckpointEditor((Player) event.getWhoClicked(), jnr, checkpointIndex, moneyDif);
				JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
				break;

			case 5:
				item = event.getInventory().getItem(5);
				if (item == null) break;
				lore = item.getItemMeta().getLore();
				if (lore == null) break;
				openCheckpointEditor((Player) event.getWhoClicked(), jnr, checkpointIndex,
					Integer.parseInt(lore.get(1).replaceFirst(
						ChatColor.AQUA + "Multiplyer: {2}" + ChatColor.DARK_AQUA,
						"")) + (event.getClick().isLeftClick() ? 1 : -1));
				break;
			case 8:

				RemoveJnrCheckpointEvent removeJnrCheckpointEvent = new RemoveJnrCheckpointEvent((Player) event.getWhoClicked(), jnr, cp, checkpointIndex);
				Bukkit.getPluginManager().callEvent(removeJnrCheckpointEvent);
				if (removeJnrCheckpointEvent.isCancelled()) break;

				jnr.getCheckpoints().remove(checkpointIndex);
				removePath((Player) event.getWhoClicked());
				displayPath((Player) event.getWhoClicked(), jnr, checkpointIndex);
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

	public void displayPath(Player player, JumpAndRun jnr, int selected) {
		if (jnr == null) return;
		Integer[] guardianIDs = new Integer[jnr.size()];
		Integer[] checkpointIDs = new Integer[jnr.size()];
		UUID[] uuids = new UUID[jnr.size()];
		List<Integer> idList = entityIDs.computeIfAbsent(player, entityIds -> new ArrayList<>());

		for (int i = 0; i < guardianIDs.length; i++) {
			guardianIDs[i] = rand.nextInt();
		}
		Utils.guardianBeam(player, jnr.getCheckpoint(1).getPos(), jnr.getCheckpoint(0).getPos(), guardianIDs[1], guardianIDs[0]);
		for (int i = guardianIDs.length - 1; i > 1; i--) {
			Utils.guardianBeamExisting(player, jnr.getCheckpoint(i).getPos(), guardianIDs[i], guardianIDs[i - 1]);
		}
		Collections.addAll(idList, guardianIDs);

		for (int i = 0; i < jnr.getCheckpoints().size(); i++) {
			checkpointIDs[i] = rand.nextInt();
			uuids[i] = UUID.randomUUID();
			Utils.displayFakeBlock(player, jnr.getCheckpoint(i).getPos(), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, checkpointIDs[i], uuids[i]);
			Utils.addGlow(player, checkpointIDs[i]);
		}
		if (selected > 0) {
			Utils.colorClow(player, EnumChatFormat.RED, uuids[selected - 1]);
		}
		if (selected < uuids.length && selected >= 0) {
			Utils.colorClow(player, EnumChatFormat.BLUE, uuids[selected]);
		}
		Collections.addAll(idList, checkpointIDs);

	}

	public JumpAndRun getJumpAndRun(ItemStack editorTool) {
		List<String> lore = editorTool.getItemMeta().getLore();
		if (lore == null) return null;
		return JumpAndRunPlugin.getJumpAndRun(lore.get(3).replaceFirst(
			ChatColor.DARK_AQUA + "JumpAndRun: " + ChatColor.AQUA, ""));
	}

	public int getCheckpoint(ItemStack editorTool) {
		List<String> lore = editorTool.getItemMeta().getLore();
		if (lore == null) return -1;
		return Integer.parseInt(lore.get(2).replaceFirst(
			ChatColor.DARK_AQUA + "Checkpoint: " + ChatColor.AQUA, ""));
	}

	public void removePath(Player player) {
		if (!entityIDs.containsKey(player))
			return;
		List<Integer> ids = entityIDs.get(player);
		entityIDs.remove(player);
		int[] ints = new int[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			ints[i] = ids.get(i);
		}

		Utils.destroyFakeEntity(player, ints);

	}
}
