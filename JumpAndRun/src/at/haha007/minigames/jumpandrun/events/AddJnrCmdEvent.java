package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunCheckpoint;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class AddJnrCmdEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private final JumpAndRunCheckpoint checkpoint;
	private boolean cancelled = false;
	private String command;
	public AddJnrCmdEvent(Player player, JumpAndRun jnr, JumpAndRunCheckpoint checkpoint, String command) {
		super(player);
		this.checkpoint = checkpoint;
		this.command = command;
		jumpAndRun = jnr;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public JumpAndRun getJumpAndRun() {
		return jumpAndRun;
	}

	public JumpAndRunCheckpoint getCheckpoint() {
		return checkpoint;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}

}
