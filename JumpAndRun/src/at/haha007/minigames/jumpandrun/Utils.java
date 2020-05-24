package at.haha007.minigames.jumpandrun;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Utils {

	public static ItemStack getItem(Material material, String name, List<String> lore) {
		ItemStack item = new ItemStack(material);
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack getItem(Material material, String name, String... lore) {
		return getItem(material, name, Arrays.asList(lore));
	}

	public static ItemStack addGlow(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
		meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
		item.setItemMeta(meta);
		return item;
	}

	public static void giveItem(Player player, ItemStack item) {
		HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
		for (Entry<Integer, ItemStack> entry : remaining.entrySet()) {
			player.getWorld().dropItem(player.getLocation(), entry.getValue());
		}
	}

	public static String combineStrings(int startIndex, int endIndex, String... strings) {
		String string = "";
		try {
			for (int i = startIndex; i <= endIndex; i++) {
				string += " " + strings[i];
			}
		} catch (IndexOutOfBoundsException e) {
		}

		return string.replaceFirst(" ", "");
	}
}
