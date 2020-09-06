package at.haha007.minigames.jumpandrun;

import at.haha007.edenlib.utils.Utils;
import at.haha007.minigames.jumpandrun.events.ChangeJnrCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.ReachCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.StopJnrEvent;
import net.minecraft.server.v1_16_R2.Blocks;
import net.minecraft.server.v1_16_R2.Items;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.UUID;

import static at.haha007.edenlib.utils.ItemUtils.*;

public class JumpAndRunPlayer {
	private final HashMap<String, Integer> checkPoints;
	private final HashMap<String, Integer> reachedCheckpoints;
	private final HashMap<String, Long> runTimestamps;
	private final UUID playerUUID;
	private int highlightedCheckpointId;
	private JumpAndRun activeJumpAndRun;
	private final Random random = new Random();

	public JumpAndRunPlayer(HashMap<String, Integer> checkPoints, HashMap<String, Integer> reachedCheckpoints, HashMap<String, Long> runTimestamps, JumpAndRun activeJumpAndRun, UUID uuid) {
		this.checkPoints = checkPoints;
		this.reachedCheckpoints = reachedCheckpoints;
		this.activeJumpAndRun = activeJumpAndRun;
		this.playerUUID = uuid;
		this.runTimestamps = runTimestamps;
	}

	public JumpAndRun getActiveJumpAndRun() {
		return activeJumpAndRun;
	}

	public int getActiveCheckPointIndex(JumpAndRun jnr) {
		return checkPoints.getOrDefault(jnr.getName(), 0);
	}

	// checks if the active checkpoint is reached
	public void checkActiveCheckpoint(Block block) {
		if (activeJumpAndRun == null)
			return;
		if (activeJumpAndRun.getWorld() != block.getWorld())
			return;
		JumpAndRunCheckpoint checkpoint = activeJumpAndRun.getCheckpoint(checkPoints.getOrDefault(activeJumpAndRun.getName(), 0) + 1);
		if (checkpoint == null)
			return;
		if (!(checkpoint.comparePosition(block.getX(), block.getY(), block.getZ())))
			return;
		reachCheckpoint();
	}

	public void respawn() {
		JumpAndRunCheckpoint cp = getActiveCheckpoint();
		if (cp == null)
			return;
		Player player = Bukkit.getPlayer(playerUUID);
		if (player == null)
			return;
		Vector pos = cp.getPos();
		if (activeJumpAndRun.shouldHighlightNextCheckpoint())
			highlightCheckpoint(player, activeJumpAndRun.getCheckpoint(getActiveCheckPointIndex(activeJumpAndRun) + 1));
		player.teleport(new Location(
			getActiveJumpAndRun().getWorld(),
			pos.getX(),
			pos.getY(),
			pos.getZ(),
			cp.getYaw(),
			cp.getPitch()));
		player.setHealth(20);
		player.setFoodLevel(20);
		player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
		player.setFireTicks(0);
		player.setFallDistance(0);
		int cpIndex = getActiveCheckPointIndex(activeJumpAndRun);
		Bukkit.getServer().getPluginManager().callEvent(new ChangeJnrCheckpointEvent(activeJumpAndRun, this, player, cpIndex, cpIndex));
		Bukkit.getScheduler().runTaskLater(JumpAndRunPlugin.getInstance(), this::fillJnrInventory, 1);
	}

	private void highlightCheckpoint(Player player, JumpAndRunCheckpoint cp) {
		if (highlightedCheckpointId != 0)
			Utils.destroyFakeEntity(player, highlightedCheckpointId);
		if (cp == null) return;
		highlightedCheckpointId = random.nextInt();
		Utils.displayFakeBlock(player, cp.getPos(), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE, highlightedCheckpointId, UUID.randomUUID());
		Utils.addGlow(player, highlightedCheckpointId);
	}

	public void fillJnrInventory() {
		Player player = Bukkit.getPlayer(playerUUID);
		if (player == null)
			return;

		for (int i = 0; i < 9; i++) {
			sendFakeItemChange(i, Items.AIR.r(), player);
		}
		sendFakeItemChange(0, getNmsStack(getItem(Material.YELLOW_DYE, ChatColor.YELLOW + "Respawn")), player);
		sendFakeItemChange(1, getNmsStack(getItem(Material.ORANGE_DYE, ChatColor.YELLOW + "Checkpoints")), player);
		sendFakeItemChange(8, getNmsStack(getItem(Material.BARRIER, ChatColor.YELLOW + "Leave")), player);
	}

	private void reachCheckpoint() {
		if (activeJumpAndRun == null)
			return;
		int activeCheckpointIndex = checkPoints.getOrDefault(activeJumpAndRun.getName(), 0);
		int maxCheckpointIndex = reachedCheckpoints.getOrDefault(activeJumpAndRun.getName(), 0);
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
		Player player = offlinePlayer.getPlayer();
		JumpAndRunCheckpoint checkpoint = getActiveCheckpoint();
		JumpAndRun active = activeJumpAndRun;

		ReachCheckpointEvent event = new ReachCheckpointEvent(player, active, this, activeCheckpointIndex + 1);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		checkPoints.put(active.getName(), activeCheckpointIndex + 1);
		if (checkpoint == null) return;
		if (player != null) {
			if (active.shouldHighlightNextCheckpoint()) {
				highlightCheckpoint(player, active.getCheckpoint(activeCheckpointIndex + 2));
				player.sendBlockChange(checkpoint.getPos().toLocation(player.getWorld()), player.getWorld().getBlockAt(checkpoint.getPosX(), checkpoint.getPosY(), checkpoint.getPosZ()).getBlockData());
			}
			player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, .5f, 1f);
			player.spawnParticle(Particle.PORTAL, player.getLocation(), 500);
			player.sendActionBar(ChatColor.GOLD + "Checkpoint Erreicht!");
		}
		if (activeCheckpointIndex == maxCheckpointIndex) {
			reachedCheckpoints.put(active.getName(), activeCheckpointIndex + 1);
			double money = checkpoint.getMoney();
			if (money > 0) {
				JumpAndRunPlugin.getEconomy().depositPlayer(offlinePlayer, checkpoint.getMoney());
				if (player != null)
					player.sendMessage(ChatColor.GOLD + "Du hast " + ChatColor.YELLOW + checkpoint.getMoney() + "$" + ChatColor.GOLD + " erhalten.");
			}
			for (String command : checkpoint.getCommands()) {
				Bukkit.getScheduler().runTask(
					JumpAndRunPlugin.getInstance(),
					() -> Bukkit.dispatchCommand(
						Bukkit.getConsoleSender(),
						command.replaceAll("%player%", offlinePlayer.getName() == null ? "" : offlinePlayer.getName())));
			}
		}
		Bukkit.getScheduler().runTaskAsynchronously(JumpAndRunPlugin.getInstance(), () -> JumpAndRunPlugin.getLoader().saveJumpAndRunPlayer(this));
	}

	public void setActiveJnr(JumpAndRun jnr) {
		activeJumpAndRun = jnr;
	}

	public void setCheckpoint(JumpAndRun jnr, int checkpoint) {
		checkPoints.put(jnr.getName(), checkpoint);
		Bukkit.getScheduler().runTaskAsynchronously(JumpAndRunPlugin.getInstance(), () -> JumpAndRunPlugin.getLoader().saveJumpAndRunPlayer(this));
	}

	public int getMaxCheckpoint(JumpAndRun jnr) {
		return reachedCheckpoints.getOrDefault(jnr.getName(), 0);
	}

	private JumpAndRunCheckpoint getActiveCheckpoint() {
		if (activeJumpAndRun == null)
			return null;
		return activeJumpAndRun.getCheckpoint(checkPoints.getOrDefault(activeJumpAndRun.getName(), 0));
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public HashMap<String, Integer> getCheckPoints() {
		return checkPoints;
	}

	public HashMap<String, Integer> getReachedCheckpoints() {
		return reachedCheckpoints;
	}

	public HashMap<String, Long> getRunTimestamps() {
		return runTimestamps;
	}

	public JumpAndRunCheckpoint getActiveCheckPoint() {
		if (activeJumpAndRun == null) return null;
		return activeJumpAndRun.getCheckpoint(checkPoints.getOrDefault(activeJumpAndRun.getName(), 0));
	}

	public void stopActiveJumpAndRun(Player player, boolean teleport) {
		if (!player.getUniqueId().equals(playerUUID)) throw new InputMismatchException();
		StopJnrEvent e = new StopJnrEvent(player, activeJumpAndRun, true, this);
		Bukkit.getPluginManager().callEvent(e);
		if (teleport) player.teleport(activeJumpAndRun.getLeavePoint());
		setActiveJnr(null);
	}

	public int getFakeBlockId() {
		return highlightedCheckpointId;
	}
}
