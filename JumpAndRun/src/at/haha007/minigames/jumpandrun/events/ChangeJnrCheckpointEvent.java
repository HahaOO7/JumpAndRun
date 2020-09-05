package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import at.haha007.minigames.jumpandrun.JumpAndRunPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class ChangeJnrCheckpointEvent extends PlayerEvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private final JumpAndRunPlayer jnrPlayer;
	private final int from;
	private int to;
	private boolean cancelled = false;

	public ChangeJnrCheckpointEvent(JumpAndRun jumpAndRun, JumpAndRunPlayer jnrPlayer, Player player, int from, int to) {
		super(player);
		this.jumpAndRun = jumpAndRun;
		this.jnrPlayer = jnrPlayer;
		this.from = from;
		this.to = to;
	}

	public JumpAndRunPlayer getJnrPlayer() {
		return jnrPlayer;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public void  setTo(int to){
		this.to = to;
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
