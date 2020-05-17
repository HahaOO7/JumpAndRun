package at.haha007.minigames.jumpandrun;

import java.util.HashMap;

import org.bukkit.block.Block;

public class JumpAndRunPlayer {
	private HashMap<JumpAndRun, Integer> checkPoints;
	private HashMap<JumpAndRun, Integer> reachedCheckpoints;
	private JumpAndRun activeJumpAndRun;

	public JumpAndRun getActiveJumpAndRun() {
		return activeJumpAndRun;
	}

	public int getActiveCheckPointIndex(JumpAndRun jnr) {
		return checkPoints.getOrDefault(jnr, 0);
	}

	// checks if the active checkpoint is reached
	boolean checkActiveCheckpoint(Block block) {
		if (activeJumpAndRun == null)
			return false;
		if (activeJumpAndRun.getWorld() != block.getWorld())
			return false;
		JumpAndRunCheckpoint checkpoint = activeJumpAndRun.getCheckpoint(checkPoints.get(activeJumpAndRun) + 1);
		if (checkpoint == null)
			return false;
		if (!(checkpoint.comparePosition(block.getX(), block.getY(), block.getZ())))
			return false;
		reachCheckpoint();
		return true;
	}

	private void reachCheckpoint() {
	}
}
