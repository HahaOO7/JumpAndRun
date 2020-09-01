package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CreateJnrEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private boolean cancelled = false;

	public CreateJnrEvent(Player player, JumpAndRun jumpAndRun) {
		super(player);
		this.jumpAndRun = jumpAndRun;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		cancelled = cancel;
	}
}
