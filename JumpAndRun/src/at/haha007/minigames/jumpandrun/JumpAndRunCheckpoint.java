package at.haha007.minigames.jumpandrun;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.util.Vector;

public class JumpAndRunCheckpoint {
	private int posX, posY, posZ;
	private float pitch, yaw;
	private List<String> commands;
	private double money;

	public JumpAndRunCheckpoint(int x, int y, int z, float pitch, float yaw, List<String> commands, double money) {
		posX = x;
		posY = y;
		posZ = z;
		this.yaw = yaw;
		this.pitch = pitch;
		this.commands = commands == null ? new ArrayList<>() : commands;
		this.money = money;
	}

	public Vector getPos() {
		return new Vector(posX + .5, posY, posZ + .5);
	}

	public List<String> getCommands() {
		return commands;
	}

	public double getMoney() {
		return money;
	}

	public boolean comparePosition(int x, int y, int z) {
		return x == posX && y == posY && z == posZ;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}
}
