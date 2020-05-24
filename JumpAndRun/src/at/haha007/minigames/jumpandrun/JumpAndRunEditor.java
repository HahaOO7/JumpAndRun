package at.haha007.minigames.jumpandrun;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_15_R1.DataWatcher;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.DataWatcherRegistry;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntityLiving;

public class JumpAndRunEditor implements Listener {
	private HashMap<Player, List<Integer>> entityIDs = new HashMap<>();
	private Random rand = new Random();

	public ItemStack getEditorTool(JumpAndRun jnr, int checkpoint) {
		ItemStack item = Utils.getItem(
				Material.NETHER_STAR, ChatColor.GOLD + "JNR Tool",
				ChatColor.DARK_AQUA + "Left: " + ChatColor.AQUA + "edit Checkpoint",
				ChatColor.DARK_AQUA + "Right: " + ChatColor.AQUA + "add Checkpoint",
				ChatColor.DARK_AQUA + "Checkpoint: " + ChatColor.AQUA + checkpoint,
				ChatColor.DARK_AQUA + "JumpAndRun: " + ChatColor.AQUA + jnr.getName());
		return item;
	}
	// add checkpoints
	// remove checkpoints
	// edit checkpoints

	// display path
	public void displayPath(Player player, JumpAndRun jnr) {
		int[] guardianIDs = new int[jnr.size()];
		int[] checkpointIDs = new int[jnr.size()];
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
			setField(packet.getClass(), "c", packet, 28);
			// pos
			setField(packet.getClass(), "d", packet, cp.getPos().getX());
			setField(packet.getClass(), "e", packet, cp.getPos().getY());
			setField(packet.getClass(), "f", packet, cp.getPos().getZ());
			Utils.sendPacket(player, packet);
			guardianIDs[i] = entityID;
			idList.add(entityID);
		}

		// shoot guardians
		for (int i = 0; i < guardianIDs.length - 1; i++) {
			DataWatcher dataWatcher = new DataWatcher(null);
			dataWatcher.register(new DataWatcherObject<>(16, DataWatcherRegistry.b), guardianIDs[i + 1]);
			dataWatcher.register(new DataWatcherObject<>(4, DataWatcherRegistry.i), true);
			dataWatcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.i), true);
			dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0b00100000);
			PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(guardianIDs[i], dataWatcher, false);
			Utils.sendPacket(player, packet);
		}

		// spawn checkpoints
		for (int i = 0; i < checkpointIDs.length; i++) {
			PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
			JumpAndRunCheckpoint cp = jnr.getCheckpoint(i);
			int entityID = rand.nextInt();
			setField(packet.getClass(), "a", packet, entityID);
			setField(packet.getClass(), "b", packet, UUID.randomUUID());
			// entity type
			setField(packet.getClass(), "c", packet, 24);
			// pos
			setField(packet.getClass(), "d", packet, cp.getPos().getX());
			setField(packet.getClass(), "e", packet, cp.getPos().getY());
			setField(packet.getClass(), "f", packet, cp.getPos().getZ());
			Utils.sendPacket(player, packet);
			checkpointIDs[i] = entityID;
			idList.add(entityID);
		}

		// light checkpoints up
		for (int i = 0; i < guardianIDs.length - 1; i++) {
			DataWatcher dataWatcher = new DataWatcher(null);
			dataWatcher.register(new DataWatcherObject<>(5, DataWatcherRegistry.i), true);
			dataWatcher.register(new DataWatcherObject<>(0, DataWatcherRegistry.a), (byte) 0b01000000);
			PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(guardianIDs[i], dataWatcher, false);
			Utils.sendPacket(player, packet);
		}
	}

	public void removePath(Player player) {
		if (!entityIDs.containsKey(player))
			return;
		for (int i : entityIDs.get(player)) {
			Utils.sendPacket(player, new PacketPlayOutEntityDestroy(i));
		}
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
