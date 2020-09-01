package at.haha007.minigames.jumpandrun.events;

import at.haha007.minigames.jumpandrun.JumpAndRun;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class StopJnrEvent extends PlayerEvent {

	private static final HandlerList HANDLERS = new HandlerList();
	private final JumpAndRun jumpAndRun;
	private final boolean forced;


	public StopJnrEvent(Player player, JumpAndRun jumpAndRun, boolean forced) {
		super(player);
		this.jumpAndRun = jumpAndRun;
		this.forced = forced;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public boolean isForced() {
		return forced;
	}

	public JumpAndRun getJumpAndRun() {
		return jumpAndRun;
	}
}

