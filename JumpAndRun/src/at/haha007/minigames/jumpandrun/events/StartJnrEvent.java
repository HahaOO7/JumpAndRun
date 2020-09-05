package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class StartJnrEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private final int cp;
	private final JumpAndRunPlayer jnrPlayer;
	private boolean cancelled = false;

	public StartJnrEvent(Player player, JumpAndRun jumpAndRun, JumpAndRunPlayer jnrPlayer, int cp) {
		super(player);
		this.jumpAndRun = jumpAndRun;
		this.jnrPlayer = jnrPlayer;
		this.cp = cp;
	}

	public int getCheckpoint() {
		return cp;
	}

	public JumpAndRunPlayer getJnrPlayer() {
		return jnrPlayer;
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
}
