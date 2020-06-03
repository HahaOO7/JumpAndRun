package at.haha007.minigames.jumpandrun;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class JumpAndRunListener implements Listener {

	@EventHandler
	void onInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.PHYSICAL) return;
		JumpAndRunPlayer player = JumpAndRunPlugin.getPlayer(event.getPlayer());
		System.out.println(player.checkActiveCheckpoint(event.getClickedBlock()));
	}
}
