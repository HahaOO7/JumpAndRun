package at.haha007.minigames.jumpandrun;

import at.haha007.edenlib.database.SqliteDatabase;
import at.haha007.edenlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class JumpAndRunLoader {
	private final File jnrFolder = new File(JumpAndRunPlugin.getInstance().getDataFolder(), "JumpAndRuns");
	private SqliteDatabase db;

	public JumpAndRunLoader() {
		try {
			db = new SqliteDatabase(JumpAndRunPlugin.getInstance(), "jnrPlayers.db");
			db.connect();
			db.prepareStatement("create table if not exists players (uuid VARCHAR(36), data blob(256), PRIMARY KEY (uuid))").executeUpdate();
		} catch (SQLException exception) {
			exception.printStackTrace();
		}
	}

	public HashSet<JumpAndRun> loadAllJumpAndRuns() {
		if (!jnrFolder.exists())
			if (!jnrFolder.mkdirs()) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[JNR] Error while loading jump and runs");
				return new HashSet<>();
			}
		File[] jnrFiles = jnrFolder.listFiles();
		HashSet<JumpAndRun> jnrs = new HashSet<>();
		for (File jnrFile : jnrFiles) {
			JumpAndRun jnr = loadJumpAndRun(jnrFile);
			if (jnr != null)
				jnrs.add(jnr);
		}

		return jnrs;
	}

	public JumpAndRun loadJumpAndRun(File file) {
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

		HashMap<UUID, Long> highScores = new HashMap<>();
		List<JumpAndRunCheckpoint> checkpoints = new ArrayList<>();

		String name = cfg.getString("name");
		String world = cfg.getString("world");
		boolean highlighted = cfg.getBoolean("highlighted");

		List<?> checkpointSections = cfg.getList("checkpoints");
		if (checkpointSections == null || world == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[JNR] Error while loading jump and run: " + ChatColor.AQUA + name);
			return null;
		}

		for (Object obj : checkpointSections) {
			//List<ConfigurationSection> does not exist...
			LinkedHashMap<String, Object> hashMap = (LinkedHashMap<String, Object>) obj;
			checkpoints.add(new JumpAndRunCheckpoint(
				(int) hashMap.get("x"),
				(int) hashMap.get("y"),
				(int) hashMap.get("z"),
				(float) (double) hashMap.get("yaw"),
				(float) (double) hashMap.get("pitch"),
				(List<String>) hashMap.get("commands"),
				(double) hashMap.get("money")));
		}

		ConfigurationSection highscoreSection = cfg.getConfigurationSection("highscores");
		if (highscoreSection != null)
			highscoreSection.getKeys(false).forEach(key -> highScores.put(UUID.fromString(key), highscoreSection.getLong(key)));

		World w = Bukkit.getWorld(world);
		Location loc = new Location(w,
			cfg.getDouble("x"),
			cfg.getDouble("y"),
			cfg.getDouble("z"),
			(float) cfg.getDouble("yaw"),
			(float) cfg.getDouble("pitch"));


		return new JumpAndRun(name, loc, checkpoints, highScores, highlighted);
	}

	public void saveJumpAndRun(JumpAndRun jnr) {
		File file = new File(jnrFolder, jnr.getName() + ".yml");
		YamlConfiguration cfg = new YamlConfiguration();

		Location loc = jnr.getLeavePoint();
		cfg.set("name", jnr.getName());
		cfg.set("highlighted", jnr.shouldHighlightNextCheckpoint());
		cfg.set("world", jnr.getWorld().getName());
		cfg.set("x", loc.getX());
		cfg.set("y", loc.getY());
		cfg.set("z", loc.getZ());
		cfg.set("yaw", loc.getYaw());
		cfg.set("pitch", loc.getPitch());

		jnr.getHighscores().forEach((uuid, time) -> cfg.set("highscores." + uuid.toString(), time));
		List<ConfigurationSection> checkpoints = new ArrayList<>(jnr.size());
		jnr.getCheckpoints().forEach((checkpoint) -> {
			YamlConfiguration checkpointConfig = new YamlConfiguration();
			checkpointConfig.set("x", checkpoint.getPosX());
			checkpointConfig.set("y", checkpoint.getPosY());
			checkpointConfig.set("z", checkpoint.getPosZ());
			checkpointConfig.set("yaw", checkpoint.getYaw());
			checkpointConfig.set("pitch", checkpoint.getPitch());
			checkpointConfig.set("money", checkpoint.getMoney());
			checkpointConfig.set("commands", checkpoint.getCommands());
			checkpoints.add(checkpointConfig);
		});
		cfg.set("checkpoints", checkpoints);
		try {
			cfg.save(file);
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[JNR] Error while trying to save " + ChatColor.AQUA + jnr.getName());
		}
	}

	@NotNull
	public JumpAndRunPlayer loadJumpAndRunPlayer(@NotNull UUID uuid) {
		YamlConfiguration cfg;
		HashMap<String, Integer> checkpoints = new HashMap<>();
		HashMap<String, Integer> reachedCheckpoints = new HashMap<>();
		HashMap<String, Long> runTimestamps = new HashMap<>();

		try {
			PreparedStatement ps = db.prepareStatement("SELECT data FROM players WHERE uuid = ?");
			ps.setString(1, uuid.toString());
			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				cfg = new YamlConfiguration();
			} else {
				BufferedReader reader =
					new BufferedReader(new InputStreamReader(new GZIPInputStream(new ByteArrayInputStream(rs.getBytes(1))), StandardCharsets.UTF_8));
				StringBuilder sb = new StringBuilder();
				reader.lines().forEach(str -> sb.append(str).append(System.lineSeparator()));
				reader.close();
				cfg = YamlConfiguration.loadConfiguration(new StringReader(sb.toString()));
			}
		} catch (SQLException | IOException exception) {
			exception.printStackTrace();
			cfg = new YamlConfiguration();
		}

		ConfigurationSection checkpointsSection = cfg.getConfigurationSection("checkpoints");
		ConfigurationSection reachedCheckpointsSection = cfg.getConfigurationSection("reachedCheckpoints");
		ConfigurationSection runTimestampsSection = cfg.getConfigurationSection("runTimestamps");

		if (checkpointsSection != null)
			checkpointsSection.getKeys(false).forEach(key -> checkpoints.put(key, checkpointsSection.getInt(key)));
		if (reachedCheckpointsSection != null)
			reachedCheckpointsSection.getKeys(false).forEach(key -> reachedCheckpoints.put(key, reachedCheckpointsSection.getInt(key)));
		if (runTimestampsSection != null)
			runTimestampsSection.getKeys(false).forEach(key -> runTimestamps.put(key, runTimestampsSection.getLong(key)));

		return new JumpAndRunPlayer(checkpoints, reachedCheckpoints, runTimestamps, null, uuid);
	}

	public void saveJumpAndRunPlayer(@NotNull JumpAndRunPlayer player) {
		Bukkit.getScheduler().runTaskAsynchronously(JumpAndRunPlugin.getInstance(), () -> {
			YamlConfiguration cfg = new YamlConfiguration();
			ConfigurationSection checkpointsSection = cfg.createSection("checkpoints");
			ConfigurationSection reachedCheckpointsSection = cfg.createSection("reachedCheckpoints");
			ConfigurationSection runTimestampsSection = cfg.createSection("runTimestamps");

			player.getCheckPoints().forEach(checkpointsSection::set);
			player.getReachedCheckpoints().forEach(reachedCheckpointsSection::set);
			player.getRunTimestamps().forEach(runTimestampsSection::set);

			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream os = new GZIPOutputStream(bos);
				os.write(cfg.saveToString().getBytes(StandardCharsets.UTF_8));
				os.close();
				byte[] data = bos.toByteArray();
				PreparedStatement ps = db.prepareStatement("REPLACE INTO players VALUES(?, ?)");
				ps.setString(1, player.getPlayerUUID().toString());
				ps.setBytes(2, data);
				ps.executeUpdate();
			} catch (IOException | SQLException e) {
				e.printStackTrace();
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[JNR] Error while saving player: " + ChatColor.AQUA + player.getPlayerUUID());
			}
		});
	}

	@Nullable
	public JumpAndRunPlayer loadJumpAndRunPlayer(String name) {
		// should only be used when player is not online
		UUID uuid = Utils.getUUID(name);
		if (uuid == null) return null;
		loadJumpAndRunPlayer(uuid);
		return null;
	}

	public void delete(JumpAndRun jnr) {
		new File(jnrFolder, jnr.getName() + ".yml").delete();
	}
}
