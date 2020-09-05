package at.haha007.minigames.jumpandrun;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class JumpAndRun {
	private Location location;
	private final String name;
	private final List<JumpAndRunCheckpoint> checkpoints;
	private boolean highlightNextCheckpoint;
	private final ArrayList<JnrScore> highscores = new ArrayList<>();

	static class JnrScore {
		final UUID uuid;
		final long time;

		JnrScore(UUID uuid, long time) {
			this.uuid = uuid;
			this.time = time;
		}
	}

	public JumpAndRun(String name,
	                  Location location,
	                  List<JumpAndRunCheckpoint> checkpoints,
	                  List<JnrScore> highscores,
	                  boolean highlightNextCheckpoint) {
		this.checkpoints = checkpoints == null ? new ArrayList<>() : new ArrayList<>(checkpoints);
		if (highscores != null) this.highscores.addAll(highscores);
		this.name = name;
		this.location = location;
		this.highlightNextCheckpoint = highlightNextCheckpoint;
	}

	public boolean shouldHighlightNextCheckpoint() {
		return highlightNextCheckpoint;
	}

	public void setHighlightNextCheckpoint(boolean should) {
		highlightNextCheckpoint = should;
	}

	public void teleportToCheckpoint(Player player) {
		player.teleportAsync(null);
	}

	public List<JumpAndRunCheckpoint> getCheckpoints() {
		return checkpoints;
	}

	public List<JnrScore> getHighscores() {
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

	public Location getLeavePoint() {
		return location;
	}

	public void setLeavePoint(Location location) {
		this.location = location;
	}

	public void submitScore(JumpAndRunPlayer jnrPlayer, long runTime) {
		UUID uuid = jnrPlayer.getPlayerUUID();
		JnrScore newScore = new JnrScore(uuid, runTime);
		List<JnrScore> matching = highscores.stream().filter(s -> s.uuid.equals(uuid)).collect(Collectors.toList());
		if (!matching.isEmpty()) {
			JnrScore oldScore = matching.get(0);
			if (oldScore.time <= newScore.time) return;
		}
		highscores.removeAll(matching);
		int index = Collections.binarySearch(highscores, newScore, Comparator.comparingLong(s -> s.time));
		if (index >= 0) {
			highscores.remove(index);
		} else {
			//flip the bits (not gate) to get the insertion point
			index = ~index;
		}
		highscores.add(index, newScore);
		Bukkit.getScheduler().runTaskAsynchronously(JumpAndRunPlugin.getInstance(), () -> JumpAndRunPlugin.getLoader().saveJumpAndRun(this));
	}

}
