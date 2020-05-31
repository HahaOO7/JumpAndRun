package at.haha007.minigames.jumpandrun;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_15_R1.Packet;

public class Utils {
	public static ItemStack getSkull(String texture) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);

		SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		Field profileField = null;
		try {
			profileField = itemMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(itemMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		item.setItemMeta(itemMeta);
		return item;
	}

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

	public static void sendPacket(Player player, Packet<?> packet) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public static ItemStack setNbtString(ItemStack item, String name, String value) {
		net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		nmsItem.getOrCreateTag().setString(name, value);
		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static String getNbtString(ItemStack item, String key) {
		return CraftItemStack.asNMSCopy(item).getOrCreateTag().getString(key);
	}

	public static ItemStack setNbtInt(ItemStack item, String name, int value) {
		net.minecraft.server.v1_15_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		nmsItem.getOrCreateTag().setInt(name, value);
		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static int getNbtInt(ItemStack item, String key) {
		return CraftItemStack.asNMSCopy(item).getOrCreateTag().getInt(key);
	}
}
