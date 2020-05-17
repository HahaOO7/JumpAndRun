package at.haha007.minigames.jumpandrun;

import java.util.ArrayList;

import org.bukkit.util.Vector;

public class JumpAndRunCheckpoint {
	private int posX, posY, posZ;
	private float pitch, yaw;
	private ArrayList<String> commands;
	private float money;

	public Vector getPos() {
		return new Vector(posX, posY, posZ);
	}

	public ArrayList<String> getCommands() {
		return commands;
	}

	public float getMoney() {
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
