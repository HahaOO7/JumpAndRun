package at.haha007.minigames.jumpandrun;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_16_R1.Packet;
import net.minecraft.server.v1_16_R1.PacketPlayOutSetSlot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.DecoderException;
import org.bukkit.craftbukkit.libs.org.apache.commons.codec.binary.Hex;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

public class Utils {
	private static final Random rand = new Random();

	public static ItemStack getSkull(String texture) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);

		SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", texture));
		try {
			Field profileField = itemMeta.getClass().getDeclaredField("profile");
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

	public static net.minecraft.server.v1_16_R1.ItemStack getNmsStack(ItemStack itemStack) {
		return CraftItemStack.asNMSCopy(itemStack);
	}

	public static void setField(Object object, String fieldName, Object value) {
		try {
			Class<?> clazz = object.getClass();
			Field field = clazz.getDeclaredField(fieldName);
			boolean a = field.isAccessible();
			field.setAccessible(true);
			field.set(object, value);
			field.setAccessible(a);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void giveItem(Player player, ItemStack item) {
		HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
		for (Entry<Integer, ItemStack> entry : remaining.entrySet()) {
			Item entityItem = player.getWorld().dropItem(player.getLocation(), entry.getValue());
			entityItem.setOwner(player.getUniqueId());
		}
	}

	public static String combineStrings(int startIndex, int endIndex, String... strings) {
		StringBuilder string = new StringBuilder();
		for (int i = startIndex; i <= endIndex; i++) {
			string.append(" ").append(strings[i]);
		}

		return string.toString().replaceFirst(" ", "");
	}

	public static void sendPacket(Player player, Packet<?> packet) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
	}

	public static ItemStack setNbtString(ItemStack item, String name, String value) {
		net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		nmsItem.getOrCreateTag().setString(name, value);
		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static String getNbtString(ItemStack item, String key) {
		return CraftItemStack.asNMSCopy(item).getOrCreateTag().getString(key);
	}

	public static ItemStack setNbtInt(ItemStack item, String name, int value) {
		net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
		nmsItem.getOrCreateTag().setInt(name, value);
		return CraftItemStack.asCraftMirror(nmsItem);
	}

	public static int getNbtInt(ItemStack item, String key) {
		return CraftItemStack.asNMSCopy(item).getOrCreateTag().getInt(key);
	}

	public static String getRandomString(int length) {
		int leftLimit = 97; // letter 'a'
		int rightLimit = 122; // letter 'z'
		return rand.ints(leftLimit, rightLimit + 1)
			.limit(length)
			.collect(StringBuilder::new,
				StringBuilder::appendCodePoint,
				StringBuilder::append)
			.toString();
	}

	public static UUID getUUID(String name) {
		try {
			JSONObject json = readJsonFromUrl("https://api.mojang.com/users/profiles/minecraft/" + name);
			String uuidString = json.get("id").toString();
			byte[] data = Hex.decodeHex(uuidString.toCharArray());
			return new UUID(ByteBuffer.wrap(data, 0, 8).getLong(), ByteBuffer.wrap(data, 8, 8).getLong());
		} catch (IOException | ParseException | DecoderException e) {
			return null;
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, ParseException {
		try (InputStream is = new URL(url).openStream()) {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String jsonText = readAll(rd);
			return (JSONObject) new JSONParser().parse(jsonText);
		}
	}

	public static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}

		return sb.toString();
	}


	public static void sendFakeItemChange(int slot, net.minecraft.server.v1_16_R1.ItemStack item, Player player) {
		sendPacket(player, new PacketPlayOutSetSlot(0, dataSlotToNetworkSlot(slot), item));
	}

	public static int dataSlotToNetworkSlot(int index) {
		if (index <= 8)
			index += 36;
		else if (index == 100)
			index = 8;
		else if (index == 101)
			index = 7;
		else if (index == 102)
			index = 6;
		else if (index == 103)
			index = 5;
		else if (index >= 80 && index <= 83)
			index -= 79;
		return index;
	}
}
