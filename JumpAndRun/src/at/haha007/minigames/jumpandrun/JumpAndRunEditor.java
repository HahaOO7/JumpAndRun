package at.haha007.minigames.jumpandrun;

import at.haha007.edenlib.utils.Utils;
import at.haha007.minigames.jumpandrun.events.AddJnrCheckpointEvent;
import at.haha007.minigames.jumpandrun.gui.JumpAndRunCheckpointEditorGui;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.EnumChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static at.haha007.edenlib.utils.ItemUtils.getItem;


public class JumpAndRunEditor implements Listener {
	private final HashSet<Player> cooldownPlayers = new HashSet<>();
	private final HashMap<Player, List<Integer>> entityIDs = new HashMap<>();
	private final Random rand = new Random();

	@EventHandler
	void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		if (!e.getPlayer().hasPermission("jnr.command.use"))
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
			JumpAndRunCheckpointEditorGui.open(e.getPlayer(), jnr, checkpoint, 1);
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
			// Insert Checkpoint
			Player player = e.getPlayer();
			if (!cooldownPlayers.contains(player)) {
				Block targetBlock = player.getTargetBlock(100);
				if (targetBlock == null) return;
				insertCheckpoint(jnr, checkpoint, player, targetBlock);
				cooldownPlayers.add(player);
				Bukkit.getScheduler().runTaskLater(JumpAndRunPlugin.getInstance(), () -> cooldownPlayers.remove(player), 10);
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
		if (!e.getPlayer().hasPermission("jnr.command.use"))
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
			Utils.colorGlow(player, EnumChatFormat.RED, uuids[selected - 1]);
		}
		if (selected < uuids.length && selected >= 0) {
			Utils.colorGlow(player, EnumChatFormat.BLUE, uuids[selected]);
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
