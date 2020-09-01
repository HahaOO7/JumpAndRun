package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunCheckpoint;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class RemoveJnrCmdEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private final JumpAndRunCheckpoint checkpoint;
	private final String command;
	private boolean cancelled = false;

	public RemoveJnrCmdEvent(Player player, JumpAndRun jnr, JumpAndRunCheckpoint cp, String command) {
		super(player);
		this.jumpAndRun = jnr;
		this.checkpoint = cp;
		this.command = command;
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

	public JumpAndRunCheckpoint getCheckpoint() {
		return checkpoint;
	}

	public JumpAndRun getJumpAndRun() {
		return jumpAndRun;
	}

	public String getCommand() {
		return command;
	}
}
