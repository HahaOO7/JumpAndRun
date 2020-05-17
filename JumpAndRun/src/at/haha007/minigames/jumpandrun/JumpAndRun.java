package at.haha007.minigames.jumpandrun;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class JumpAndRun {
	private String name;
	private World world;
	private ArrayList<JumpAndRunCheckpoint> checkpoints;
	private HashMap<String, Integer> highscores;

	public JumpAndRun(String name, World world, ArrayList<JumpAndRunCheckpoint> checkpoints,
			HashMap<String, Integer> highscores) {
		this.checkpoints = checkpoints;
		this.highscores = highscores;
		this.name = name;
		this.world = world;
	}
	
	public void teleportToCheckpoint(Player player) {
		player.teleportAsync(null);
	}

	public ArrayList<JumpAndRunCheckpoint> getCheckpoints() {
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
}
