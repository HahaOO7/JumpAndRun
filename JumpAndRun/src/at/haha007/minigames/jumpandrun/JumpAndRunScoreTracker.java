package at.haha007.minigames.jumpandrun;

import at.haha007.minigames.jumpandrun.events.ChangeJnrCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.ReachCheckpointEvent;
import at.haha007.minigames.jumpandrun.events.StartJnrEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JumpAndRunScoreTracker implements Listener {

	@EventHandler
	void onCheckpointReached(ReachCheckpointEvent event) {
		if (event.getCheckpoint() == event.getJumpAndRun().size() - 1) {
			finishJnr(event.getJumpAndRun(), event.getJnrPlayer(), event.getPlayer());
		}
	}


	@EventHandler
	void onStartJnr(StartJnrEvent event) {
		if (event.getCheckpoint() == 0) {
			setStartTime(event.getJumpAndRun(), event.getJnrPlayer());
		}
	}

	@EventHandler
	void onCheckpointChanged(ChangeJnrCheckpointEvent event) {
		if (event.getTo() == 0) {
			setStartTime(event.getJumpAndRun(), event.getJnrPlayer());
		}
		if (event.getTo() > event.getFrom()) {
			resetStartTime(event.getJumpAndRun(), event.getJnrPlayer());
		}
	}

	private void resetStartTime(JumpAndRun jumpAndRun, JumpAndRunPlayer jnrPlayer) {
		jnrPlayer.getRunTimestamps().remove(jumpAndRun.getName());
	}

	private void setStartTime(JumpAndRun jumpAndRun, JumpAndRunPlayer jnrPlayer) {
		jnrPlayer.getRunTimestamps().put(jumpAndRun.getName(), System.currentTimeMillis());
	}

	private void finishJnr(JumpAndRun jumpAndRun, JumpAndRunPlayer jnrPlayer, Player player) {
		long startTime = jnrPlayer.getRunTimestamps().getOrDefault(jumpAndRun.getName(), -1L);
		if (startTime <= 0) return;
		long runTime = System.currentTimeMillis() - startTime;
		jumpAndRun.submitScore(jnrPlayer, runTime);
	}
}
