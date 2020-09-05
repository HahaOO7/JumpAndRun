package at.haha007.minigames.jumpandrun;

import at.haha007.edenlib.utils.Utils;
import at.haha007.minigames.jumpandrun.events.AddJnrCmdEvent;
import at.haha007.minigames.jumpandrun.events.CreateJnrEvent;
import at.haha007.minigames.jumpandrun.events.DeleteJnrEvent;
import at.haha007.minigames.jumpandrun.gui.JumpAndRunManagementGui;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;


public class JumpAndRunCommand implements CommandExecutor, TabCompleter, Listener {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (!(sender instanceof Player)) {
			if (!(sender instanceof BlockCommandSender))
				return false;
			BlockCommandSender block = (BlockCommandSender) sender;
			JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(args[0]);
			if (jnr == null) return false;
			double radius;
			try {
				radius = Double.parseDouble(args[1]);
			} catch (NumberFormatException e) {
				return false;
			}
			Collection<Player> players = block.getBlock().getLocation().getNearbyPlayers(radius);
			players.forEach(player -> JumpAndRunPlugin.startJumpAndRun(jnr, player));
			return true;
		}
		Player player = (Player) sender;

		if (args.length == 2 && args[0].equalsIgnoreCase("delete")) {

			JumpAndRun jnr = JumpAndRunPlugin.getJumpAndRun(args[1]);
			if (jnr == null) {
				sender.sendMessage(ChatColor.RED + "[JNR] JumpAndRun wurde nicht gefunden.");
				return true;
			}

			DeleteJnrEvent deleteJnrEvent = new DeleteJnrEvent(player, jnr);
			Bukkit.getPluginManager().callEvent(deleteJnrEvent);
			if (deleteJnrEvent.isCancelled()) return true;

			JumpAndRunPlugin.delete(jnr);
			sender.sendMessage(ChatColor.GOLD + "[JNR] JumpAndRun wurde entfernt.");
			return true;
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
			if (JumpAndRunPlugin.getJumpAndRun(args[1]) != null) {
				sender.sendMessage(ChatColor.RED + "Dieses JNR existiert bereits.");
				return true;
			}
			Location loc = player.getLocation();
			JumpAndRun jnr = new JumpAndRun(
				ChatColor.translateAlternateColorCodes('&', args[1]),
				loc,
				Collections.singletonList(
					new JumpAndRunCheckpoint(
						loc.getBlockX(),
						loc.getBlockY(),
						loc.getBlockZ(),
						0f,
						0f,
						null,
						0d)),
				new ArrayList<>(),
				false);

			CreateJnrEvent createJnrEvent = new CreateJnrEvent(player, jnr);
			Bukkit.getPluginManager().callEvent(createJnrEvent);
			if (createJnrEvent.isCancelled()) return true;

			JumpAndRunPlugin.getJumpAndRuns().add(jnr);
			JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
			sender.sendMessage(ChatColor.GOLD + "JNR erstellt.");

			return true;
		}
		if (args.length > 1 && args[0].equalsIgnoreCase("addcmd")) {
			ItemStack item = player.getInventory().getItemInMainHand();
			JumpAndRunEditor editor = JumpAndRunPlugin.getEditor();
			if (!editor.isEditorTool(item)) {
				sender.sendMessage(ChatColor.RED + "Du benötigst ein JNR tool in der Hand.");
				return true;
			}
			String cmd = Utils.combineStrings(1, args.length - 1, args);
			JumpAndRun jnr = editor.getJumpAndRun(item);
			if (jnr == null)
				return true;
			int cp = editor.getCheckpoint(item);
			JumpAndRunCheckpoint checkpoint = jnr.getCheckpoint(cp);

			AddJnrCmdEvent addJnrCmdEvent = new AddJnrCmdEvent(player, jnr, checkpoint, cmd);
			Bukkit.getPluginManager().callEvent(addJnrCmdEvent);
			if (addJnrCmdEvent.isCancelled()) return true;
			cmd = addJnrCmdEvent.getCommand();

			checkpoint.getCommands().add(cmd);
			JumpAndRunPlugin.getLoader().saveJumpAndRun(jnr);
			sender.sendMessage(ChatColor.GOLD + "Command hinzugefügt");
			return true;
		}
		JumpAndRunManagementGui.open((Player) sender);
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
		if (sender instanceof BlockCommandSender) {
			if (args.length == 1)
				return JumpAndRunPlugin.getJumpAndRuns().stream().map(JumpAndRun::getName).filter(str -> str.toLowerCase().startsWith(args[0])).collect(Collectors.toList());
		}
		if (args.length == 1) {
			List<String> cmds = new ArrayList<>(Arrays.asList("create", "addcmd", "delete"));
			return cmds.stream().filter(str -> str.toLowerCase().startsWith(args[0])).collect(Collectors.toList());
		}
		if (args.length == 2 && args[0].equalsIgnoreCase("delete"))
			return JumpAndRunPlugin.getJumpAndRuns().stream().map(JumpAndRun::getName).filter(str -> str.toLowerCase().startsWith(args[1])).collect(Collectors.toList());

		return Collections.emptyList();
	}


}
