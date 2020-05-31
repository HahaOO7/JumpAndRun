package at.haha007.minigames.jumpandrun;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;

public class JumpAndRunLoader {

	public JumpAndRun loadJumpAndRun(File file) {
		return null;
	}

	public JumpAndRunPlayer loadJumpAndRunPlayer(UUID uuid) {
		return new JumpAndRunPlayer();
	}

	public JumpAndRunPlayer loadJumpAndRunPlayer(String name) {
		// should only be used when player is not online
		return new JumpAndRunPlayer();
	}

	public HashSet<JumpAndRun> loadAllJumpAndRuns() {
		return new HashSet<>();
	}

	public void saveJumpAndRun(JumpAndRun jnr) {

	}
}
