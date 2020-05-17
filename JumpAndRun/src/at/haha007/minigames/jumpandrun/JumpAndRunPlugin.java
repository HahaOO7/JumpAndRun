package at.haha007.minigames.jumpandrun;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class JumpAndRunPlugin extends JavaPlugin {
	private static Economy econ = null;
	private static JumpAndRunPlugin instance = null;

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
}
