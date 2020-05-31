package at.haha007.minigames.jumpandrun;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_15_R1.Block;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.DataWatcher;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.DataWatcherRegistry;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.EnumChatFormat;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntityLiving;

public class JumpAndRunEditor implements Listener {
	private HashMap<Player, List<Integer>> entityIDs = new HashMap<>();
	private Random rand = new Random();

	@EventHandler
	void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getHand() != EquipmentSlot.HAND)
			return;
		if (!e.getPlayer().hasPermission("jnr.editor.use"))
			return;
		ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
		if (item == null)
			return;
		if (!isEditorTool(item))
			return;
		e.setCancelled(true);
		JumpAndRun jnr = getJumpAndRun(item);
		if (jnr == null)
			return;
		int checkpoint = getCheckpoint(item);
		if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
			// Edit Checkpoint
		}
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			// Insert Checkpoint
			jnr.addCheckpoint(
					new JumpAndRunCheckpoint(
							e.getClickedBlock().getX(),
							e.getClickedBlock().getY() + 1,
							e.getClickedBlock().getZ(),
							0f,
							0f,
							null,
							0d),
					checkpoint);
			e.getClickedBlock().getRelative(BlockFace.UP).setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE, false);
			e.getPlayer().getInventory().setItemInMainHand(getEditorTool(jnr, checkpoint + 1));
			removePath(e.getPlayer());
			displayPath(e.getPlayer(), jnr, checkpoint + 1);
		}
	}

	@EventHandler
	void onSlotChange(PlayerItemHeldEvent e) {
		if (!e.getPlayer().hasPermission("jnr.editor.use"))
			return;
		if (!e.getPlayer().isSneaking()) {
			removePath(e.getPlayer());
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
		if (!isEditorTool(item))
			return;
		e.setCancelled(true);
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
			return;
		}
	}

	public ItemStack getEditorTool(JumpAndRun jnr, int checkpoint) {
		ItemStack item = Utils.getItem(
				Material.NETHER_STAR, ChatColor.GOLD + "JNR Tool",
				ChatColor.DARK_AQUA + "Left: " + ChatColor.AQUA + "edit Checkpoint",
				ChatColor.DARK_AQUA + "Right: " + ChatColor.AQUA + "add Checkpoint",
				ChatColor.DARK_AQUA + "Checkpoint: " + ChatColor.AQUA + checkpoint,
				ChatColor.DARK_AQUA + "JumpAndRun: " + ChatColor.AQUA + jnr.getName());
		return item;
	}

	public boolean isEditorTool(ItemStack item) {
		try {
			return item.getItemMeta().getDisplayName().equals(ChatColor.GOLD + "JNR Tool");
		} catch (NullPointerException e) {
			return false;
		}
	}

	public JumpAndRun getJumpAndRun(ItemStack editorTool) {
		return JumpAndRunPlugin.getJumpAndRun(editorTool.getItemMeta().getLore().get(3).replaceFirst(ChatColor.DARK_AQUA + "JumpAndRun: " + ChatColor.AQUA, ""));
	}

	public int getCheckpoint(ItemStack editorTool) {
		return Integer.parseInt(editorTool.getItemMeta().getLore().get(2).replaceFirst(ChatColor.DARK_AQUA + "Checkpoint: " + ChatColor.AQUA, ""));
	}

	// add checkpoints
	// remove checkpoints
	// edit checkpoints

	// display path
	public void displayPath(Player player, JumpAndRun jnr, int selected) {
		int[] guardianIDs = new int[jnr.size()];
		int[] checkpointIDs = new int[jnr.size()];
		UUID[] uuids = new UUID[jnr.size()];
		List<Integer> idList = entityIDs.get(player);
		if (idList == null)
			entityIDs.put(player, idList = new ArrayList<>());

		// spawn guardians
		for (int i = 0; i < guardianIDs.length; i++) {
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
			JumpAndRunCheckpoint cp = jnr.getCheckpoint(i);
			int entityID = rand.nextInt();
			setField(packet.getClass(), "a", packet, entityID);
			setField(packet.getClass(), "b", packet, UUID.randomUUID());
			// entity type
			setField(packet.getClass(), "c", packet, 31);
			// pos
			setField(packet.getClass(), "d", packet, cp.getPos().getX());
			setField(packet.getClass(), "e", packet, cp.getPos().getY());
			setField(packet.getClass(), "f", packet, cp.getPos().getZ());
			Utils.sendPacket(player, packet);
			guardianIDs[i] = entityID;
			idList.add(entityID);
		}

		// shoot guardians
		for (int i = 0; i < guardianIDs.length; i++) {
			DataWatcher dataWatcher = new DataWatcher(null);
			dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0b00100000);
			dataWatcher.register(new DataWatcherObject<>(4, DataWatcherRegistry.i), true);
			dataWatcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.i), true);
			if (i + 1 != guardianIDs.length)
				dataWatcher.register(new DataWatcherObject<>(16, DataWatcherRegistry.b), guardianIDs[i + 1]);
			PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(guardianIDs[i], dataWatcher, true);
			Utils.sendPacket(player, packet);
		}

		// spawn checkpoints
		for (int i = 0; i < checkpointIDs.length; i++) {
			PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity();
			JumpAndRunCheckpoint cp = jnr.getCheckpoint(i);
			int entityID = rand.nextInt();
			UUID uuid = UUID.randomUUID();
			setField(packet.getClass(), "a", packet, entityID);
			setField(packet.getClass(), "b", packet, uuid);
			// pos
			setField(packet.getClass(), "c", packet, cp.getPos().getX());
			setField(packet.getClass(), "d", packet, cp.getPos().getY());
			setField(packet.getClass(), "e", packet, cp.getPos().getZ());
			// entity type
			setField(packet.getClass(), "k", packet, EntityTypes.FALLING_BLOCK);
			setField(packet.getClass(), "l", packet, Block.getCombinedId(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.getBlockData()));
			Utils.sendPacket(player, packet);
			checkpointIDs[i] = entityID;
			uuids[i] = uuid;
			idList.add(entityID);
		}

		// light checkpoints up
		for (int i = 0; i < checkpointIDs.length; i++) {
			DataWatcher dataWatcher = new DataWatcher(null);
			dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0b01000000);
			dataWatcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.i), true);
			PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(checkpointIDs[i], dataWatcher, true);
			Utils.sendPacket(player, packet);
		}

		if (selected > 0) {
			PacketPlayOutScoreboardTeam packetRed = new PacketPlayOutScoreboardTeam();
			setField(PacketPlayOutScoreboardTeam.class, "a", packetRed, getRandomString(16)); // name
			setField(PacketPlayOutScoreboardTeam.class, "b", packetRed, new ChatComponentText("")); // display name
			setField(PacketPlayOutScoreboardTeam.class, "c", packetRed, new ChatComponentText("PRE ")); // prefix
			setField(PacketPlayOutScoreboardTeam.class, "d", packetRed, new ChatComponentText(" SUF")); // suffix
			setField(PacketPlayOutScoreboardTeam.class, "e", packetRed, "never"); // nametag visible
			setField(PacketPlayOutScoreboardTeam.class, "f", packetRed, "never"); // collision rule
			setField(PacketPlayOutScoreboardTeam.class, "g", packetRed, EnumChatFormat.RED); // team color
			setField(PacketPlayOutScoreboardTeam.class, "h", packetRed, Arrays.asList(uuids[selected - 1].toString())); // entities
			setField(PacketPlayOutScoreboardTeam.class, "i", packetRed, 0); // packet type crete team
			setField(PacketPlayOutScoreboardTeam.class, "j", packetRed, 1); // entity count?
			Utils.sendPacket(player, packetRed);
		}

		if (selected < uuids.length && selected >= 0) {
			PacketPlayOutScoreboardTeam packetBlue = new PacketPlayOutScoreboardTeam();
			setField(PacketPlayOutScoreboardTeam.class, "a", packetBlue, getRandomString(16)); // name
			setField(PacketPlayOutScoreboardTeam.class, "b", packetBlue, new ChatComponentText("")); // display name
			setField(PacketPlayOutScoreboardTeam.class, "c", packetBlue, new ChatComponentText("PRE ")); // prefix
			setField(PacketPlayOutScoreboardTeam.class, "d", packetBlue, new ChatComponentText(" SUF")); // suffix
			setField(PacketPlayOutScoreboardTeam.class, "e", packetBlue, "never"); // nametag visible
			setField(PacketPlayOutScoreboardTeam.class, "f", packetBlue, "never"); // collision rule
			setField(PacketPlayOutScoreboardTeam.class, "g", packetBlue, EnumChatFormat.BLUE); // team color
			setField(PacketPlayOutScoreboardTeam.class, "h", packetBlue, Arrays.asList(uuids[selected].toString())); // entities
			setField(PacketPlayOutScoreboardTeam.class, "i", packetBlue, 0); // packet type crete team
			setField(PacketPlayOutScoreboardTeam.class, "j", packetBlue, 1); // entity count?
			Utils.sendPacket(player, packetBlue);
		}
	}

	public void removePath(Player player) {
		if (!entityIDs.containsKey(player))
			return;
		for (int i : entityIDs.get(player)) {
			Utils.sendPacket(player, new PacketPlayOutEntityDestroy(i));
		}
	}

	private String getRandomString(int length) {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		return rand.ints(leftLimit, rightLimit + 1)
				.limit(length)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();

	}

	private void setField(Class<?> clazz, String fieldName, Object object, Object value) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			boolean a = field.isAccessible();
			field.setAccessible(true);
			field.set(object, value);
			field.setAccessible(a);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}
}
