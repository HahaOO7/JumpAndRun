package at.haha007.minigames.jumpandrun;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public class JumpAndRunPlugin extends JavaPlugin {
	private static Economy econ = null;
	private static JumpAndRunPlugin instance = null;
	private static HashMap<UUID, JumpAndRunPlayer> players = new HashMap<>();
	private static JumpAndRunLoader loader;

	@Override
	public void onEnable() {
		instance = this;
		setupEconomy();
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
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
		if (p == null)
			p = loader.loadJumpAndRunPlayer(uuid);
		return p;
	}
}
