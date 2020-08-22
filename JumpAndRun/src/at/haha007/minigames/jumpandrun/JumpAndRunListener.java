package at.haha007.minigames.jumpandrun;

import at.haha007.minigames.jumpandrun.events.StopJnrEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class JumpAndRunListener implements Listener {

	@EventHandler
	void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive(player);
		if (jnrPlayer == null) return;
		event.setCancelled(true);
		if (event.getAction() == Action.PHYSICAL) {
			jnrPlayer.checkActiveCheckpoint(event.getClickedBlock());
			return;
		}
		int slot = event.getPlayer().getInventory().getHeldItemSlot();
		switch (slot) {
			case 0:
				jnrPlayer.respawn();
				break;
			case 1:
				JumpAndRun jnr = jnrPlayer.getActiveJumpAndRun();
				JumpAndRunPlugin.getCmd().openJnrMenu(player, jnr, jnrPlayer.getActiveCheckPointIndex(jnr));
				break;
			case 8:
				jnr = jnrPlayer.getActiveJumpAndRun();

				StopJnrEvent e = new StopJnrEvent(event.getPlayer(), jnr, false);
				Bukkit.getPluginManager().callEvent(e);

				jnrPlayer.setActiveJnr(null);
				player.teleport(jnr.getLeavePoint());
				break;
			default:
				break;
		}
	}

	@EventHandler
	void onHunger(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive(player);
		if (jnrPlayer == null) return;
		event.setCancelled(true);
	}

	@EventHandler
	void onPlayerDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive(player);
		if (jnrPlayer == null) return;
		if (player.getHealth() >= event.getDamage()) return;
		event.setCancelled(true);
		jnrPlayer.respawn();
	}

	@EventHandler
	void onInventoryInteract(InventoryClickEvent event) {
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive((Player) event.getWhoClicked());
		if (jnrPlayer == null) return;
		event.setCancelled(true);
		Bukkit.getScheduler().runTaskLater(JumpAndRunPlugin.getInstance(), jnrPlayer::fillJnrInventory, 1);
	}

	@EventHandler
	void onPlayerDisconnect(PlayerQuitEvent event) {
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive(event.getPlayer());
		if (jnrPlayer == null) return;

		JumpAndRun jnr = jnrPlayer.getActiveJumpAndRun();

		StopJnrEvent e = new StopJnrEvent(event.getPlayer(), jnr, true);
		Bukkit.getPluginManager().callEvent(e);

		jnrPlayer.setActiveJnr(null);
		event.getPlayer().teleport(jnr.getLeavePoint());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerTeleport(PlayerTeleportEvent event) {
		JumpAndRunPlayer jnrPlayer = JumpAndRunPlugin.getPlayerIfActive(event.getPlayer());
		if (jnrPlayer == null) return;
		JumpAndRun jnr = jnrPlayer.getActiveJumpAndRun();
		JumpAndRunCheckpoint cp = jnrPlayer.getActiveCheckPoint();
		if (cp == null) return;
		Location to = event.getTo();
		if (to.getWorld() == jnr.getWorld() && to.toVector().distance(cp.getPos()) < 1) return;

		StopJnrEvent e = new StopJnrEvent(event.getPlayer(), jnr, true);
		Bukkit.getPluginManager().callEvent(e);

		jnrPlayer.setActiveJnr(null);
	}
}
