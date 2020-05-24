package at.haha007.minigames.jumpandrun;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class JumpAndRunEditor implements Listener {

	public ItemStack getEditorTool() {
		return Utils.getItem(
				Material.NETHER_STAR, ChatColor.GOLD + "JNR Tool",
				ChatColor.DARK_AQUA + "Left: " + ChatColor.AQUA + "edit Checkpoint",
				ChatColor.DARK_AQUA + "Right: " + ChatColor.AQUA + "add Checkpoint");
	}
	// add checkpoints
	// remove checkpoints
	// edit checkpoints
	
	// display path
	public void displayPath(Player player, JumpAndRun jnr) {
		
	}
}
