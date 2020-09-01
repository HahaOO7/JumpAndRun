package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunCheckpoint;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class RemoveJnrCheckpointEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private final int index;
	private final JumpAndRunCheckpoint checkpoint;
	private boolean cancelled = false;

	public RemoveJnrCheckpointEvent(Player player, JumpAndRun jumpAndRun, JumpAndRunCheckpoint checkpoint, int index) {
		super(player);
		this.checkpoint = checkpoint;
		this.jumpAndRun = jumpAndRun;
		this.index = index;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

	public JumpAndRun getJumpAndRun() {
		return jumpAndRun;
	}

	public JumpAndRunCheckpoint getCheckpoint() {
		return checkpoint;
	}

	public int getIndex() {
		return index;
	}
}
