package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class DeleteJnrEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private boolean cancelled = false;

	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
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

	private final JumpAndRun jumpAndRun;

	public DeleteJnrEvent(Player player, JumpAndRun jumpAndRun) {
		super(player);
		this.jumpAndRun = jumpAndRun;
	}

	public JumpAndRun getJumpAndRun() {
		return jumpAndRun;
	}
}
