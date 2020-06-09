package at.haha007.minigames.jumpandrun;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class JumpAndRun {
	private final String name;
	Location location;
	private final List<JumpAndRunCheckpoint> checkpoints;
	private final HashMap<UUID, Long> highscores;

	public JumpAndRun(String name, Location location, List<JumpAndRunCheckpoint> checkpoints,
	                  HashMap<UUID, Long> highscores) {
		this.checkpoints = checkpoints == null ? new ArrayList<>() : new ArrayList<>(checkpoints);
		this.highscores = highscores == null ? new HashMap<>() : highscores;
		this.name = name;
		this.location = location;
	}

	public void teleportToCheckpoint(Player player) {
		player.teleportAsync(null);
	}

	public List<JumpAndRunCheckpoint> getCheckpoints() {
		return checkpoints;
	}

	public HashMap<UUID, Long> getHighscores() {
		return highscores;
	}

	public String getName() {
		return name;
	}

	public World getWorld() {
		return location.getWorld();
	}

	public JumpAndRunCheckpoint getCheckpoint(int i) {
		try {
			return checkpoints.get(i);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	public int size() {
		return checkpoints.size();
	}

	public void addCheckpoint(JumpAndRunCheckpoint checkpoint, int index) {
		checkpoints.add(index, checkpoint);
	}

	public int getCheckpointIndex(int x, int y, int z) {
		for (int i = 0; i < checkpoints.size(); i++) {
			JumpAndRunCheckpoint cp = checkpoints.get(i);
			if (cp.comparePosition(x, y, z))
				return i;
		}
		return -1;
	}

	public void setLeavePoint(Location location) {
		this.location = location;
	}

	public Location getLeavePoint() {
		return location;
	}
}
