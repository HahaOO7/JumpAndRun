package at.haha007.minigames.jumpandrun;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class JumpAndRunPlayer {
	private final HashMap<String, Integer> checkPoints;
	private final HashMap<String, Integer> reachedCheckpoints;
	private final JumpAndRun activeJumpAndRun;
	private final UUID uuid;

	public JumpAndRunPlayer(HashMap<String, Integer> checkPoints, HashMap<String, Integer> reachedCheckpoints, JumpAndRun activeJumpAndRun, UUID uuid) {
		this.checkPoints = checkPoints;
		this.reachedCheckpoints = reachedCheckpoints;
		this.activeJumpAndRun = activeJumpAndRun;
		this.uuid = uuid;
	}

	public JumpAndRun getActiveJumpAndRun() {
		return activeJumpAndRun;
	}

	public int getActiveCheckPointIndex(JumpAndRun jnr) {
		return checkPoints.getOrDefault(jnr.getName(), 0);
	}

	// checks if the active checkpoint is reached
	boolean checkActiveCheckpoint(Block block) {
		if (activeJumpAndRun == null)
			return false;
		if (activeJumpAndRun.getWorld() != block.getWorld())
			return false;
		JumpAndRunCheckpoint checkpoint = activeJumpAndRun.getCheckpoint(checkPoints.get(activeJumpAndRun.getName()) + 1);
		if (checkpoint == null)
			return false;
		if (!(checkpoint.comparePosition(block.getX(), block.getY(), block.getZ())))
			return false;
		reachCheckpoint();
		return true;
	}

	public void respawn() {
		JumpAndRunCheckpoint cp = getActiveCheckpoint();
		if (cp == null)
			return;
		Player player = Bukkit.getPlayer(uuid);
		if (player == null)
			return;
		Vector pos = cp.getPos();
		player.teleport(new Location(
			getActiveJumpAndRun().getWorld(),
			pos.getX(),
			pos.getY(),
			pos.getZ(),
			cp.getYaw(),
			cp.getPitch()));
	}

	public void fillJnrInventory() {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null)
			return;
		PlayerInventory inv = player.getInventory();
		inv.setItem(0, Utils.getItem(Material.YELLOW_DYE, ChatColor.YELLOW + "Respawn"));
		inv.setItem(1, Utils.getItem(Material.ORANGE_DYE, ChatColor.YELLOW + "Checkpoints"));
		inv.setItem(8, Utils.getItem(Material.BARRIER, ChatColor.YELLOW + "Leave"));
	}

	private void reachCheckpoint() {
		if (activeJumpAndRun == null)
			return;
		int activeCheckpintIndex = checkPoints.getOrDefault(activeJumpAndRun.getName(), 0);
		checkPoints.put(activeJumpAndRun.getName(), activeCheckpintIndex + 1);
		int maxCheckpointIndex = reachedCheckpoints.getOrDefault(activeJumpAndRun.getName(), 0);
		if (activeCheckpintIndex == maxCheckpointIndex) {
			reachedCheckpoints.put(activeJumpAndRun.getName(), activeCheckpintIndex + 1);
			JumpAndRunCheckpoint checkpoint = getActiveCheckpoint();
			double money = checkpoint.getMoney();
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
			if (money > 0) {
				JumpAndRunPlugin.getEconomy().depositPlayer(offlinePlayer, checkpoint.getMoney());
			}
			for (String command : checkpoint.getCommands()) {
				Bukkit.getScheduler().runTask(
					JumpAndRunPlugin.getInstance(),
					() -> Bukkit.dispatchCommand(
						Bukkit.getConsoleSender(),
						command.replaceAll("%player%", offlinePlayer.getName() == null ? "" : offlinePlayer.getName())));
			}
		}
	}

	private JumpAndRunCheckpoint getActiveCheckpoint() {
		return activeJumpAndRun.getCheckpoint(checkPoints.getOrDefault(activeJumpAndRun.getName(), 0));
	}
}
