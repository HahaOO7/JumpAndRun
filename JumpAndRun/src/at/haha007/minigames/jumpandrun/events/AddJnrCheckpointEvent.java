package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunCheckpoint;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class AddJnrCheckpointEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jnr;
	private final JumpAndRunCheckpoint checkpoint;
	private final int index;
	private boolean cancelled = false;

	public AddJnrCheckpointEvent(JumpAndRun jnr, JumpAndRunCheckpoint checkpoint, int index, Player player) {
		super(player);
		this.checkpoint = checkpoint;
		this.index = index;
		this.jnr = jnr;
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

	public int getIndex() {
		return index;
	}

	public JumpAndRun getJnr() {
		return jnr;
	}

	public JumpAndRunCheckpoint getCheckpoint() {
		return checkpoint;
	}


}
