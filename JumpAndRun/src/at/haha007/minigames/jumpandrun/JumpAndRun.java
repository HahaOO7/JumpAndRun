package at.haha007.minigames.jumpandrun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class JumpAndRun {
	private String name;
	private World world;
	private List<JumpAndRunCheckpoint> checkpoints;
	private HashMap<String, Integer> highscores;

	public JumpAndRun(String name, World world, List<JumpAndRunCheckpoint> checkpoints,
			HashMap<String, Integer> highscores) {
		this.checkpoints = checkpoints == null ? new ArrayList<>() : new ArrayList<>(checkpoints);
		this.highscores = highscores == null ? new HashMap<>() : highscores;
		this.name = name;
		this.world = world;
	}

	public void teleportToCheckpoint(Player player) {
		player.teleportAsync(null);
	}

	public List<JumpAndRunCheckpoint> getCheckpoints() {
		return checkpoints;
	}

	public HashMap<String, Integer> getHighscores() {
		return highscores;
	}

	public String getName() {
		return name;
	}

	public World getWorld() {
		return world;
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
}
