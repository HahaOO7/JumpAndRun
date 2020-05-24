package at.haha007.minigames.jumpandrun;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class JumpAndRunCommand implements CommandExecutor, TabCompleter {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}

	Inventory openInventory(Player player) {
		Inventory inv = Bukkit.createInventory(null, 9);
		return inv;
	}
}
