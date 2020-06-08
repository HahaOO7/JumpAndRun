package at.haha007.minigames.jumpandrun;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JumpAndRunPlugin extends JavaPlugin {
	private static Economy econ = null;
	private static JumpAndRunPlugin instance = null;
	private static HashMap<UUID, JumpAndRunPlayer> players;
	private static JumpAndRunLoader loader;
	private static JumpAndRunEditor editor;
	private static HashSet<JumpAndRun> jumpAndRuns;

	@Override
	public void onEnable() {
		if (!getDataFolder().exists()) getDataFolder().mkdirs();
		instance = this;
		if (!setupEconomy())
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[JumpAndRun] Failed to connect to economy!");
		players = new HashMap();
		loader = new JumpAndRunLoader();
		editor = new JumpAndRunEditor();
		jumpAndRuns = loader.loadAllJumpAndRuns();

		JumpAndRunCommand cmd = new JumpAndRunCommand();
		getCommand("jumpandrun").setExecutor(cmd);
		getCommand("jumpandrun").setTabCompleter(cmd);
		getServer().getPluginManager().registerEvents(cmd, this);
		getServer().getPluginManager().registerEvents(editor, this);
		getServer().getPluginManager().registerEvents(new JumpAndRunListener(), this);
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) return false;
		econ = rsp.getProvider();
		return econ.isEnabled();
	}

	public static Economy getEconomy() {
		return econ;
	}

	public static JumpAndRunPlugin getInstance() {
		return instance;
	}

	public static JumpAndRunPlayer getPlayer(Player player) {
		return getPlayer(player.getUniqueId());
	}

	public static JumpAndRunPlayer getPlayer(UUID uuid) {
		JumpAndRunPlayer p = players.get(uuid);
		if (p == null) {
			p = loader.loadJumpAndRunPlayer(uuid);
			players.put(uuid, p);
		}
		return p;
	}

	public static Set<JumpAndRunPlayer> getActivePlayers() {
		if (players.size() > Bukkit.getOnlinePlayers().size()) {
			Set<UUID> remove = new HashSet<>();
			players.forEach((uuid, player) -> {
				if (player.getActiveJumpAndRun() == null) remove.add(uuid);
			});
			remove.forEach(uuid -> players.remove(uuid));
		}
		return players.values().parallelStream().filter(entry -> entry.getActiveJumpAndRun() == null).collect(Collectors.toSet());
	}

	public static JumpAndRunEditor getEditor() {
		return editor;
	}

	public static JumpAndRun getJumpAndRun(String name) {
		for (JumpAndRun jumpAndRun : jumpAndRuns) {
			if (jumpAndRun.getName().equals(name))
				return jumpAndRun;
		}
		return null;
	}

	public static HashSet<JumpAndRun> getJumpAndRuns() {
		return jumpAndRuns;
	}

	public static JumpAndRunLoader getLoader() {
		return loader;
	}
}
