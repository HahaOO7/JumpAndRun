package at.haha007.minigames.jumpandrun;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class JumpAndRunCheckpoint {
	private final List<String> commands;
	private int posX, posY, posZ;
	private float pitch, yaw;
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

	public void setPos(int x, int y, int z) {
		posX = x;
		posY = y;
		posZ = z;
	}

	public void setRotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}

	public List<String> getCommands() {
		return commands;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
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

	public int getPosX() {
		return posX;
	}

	public int getPosY() {
		return posY;
	}

	public int getPosZ() {
		return posZ;
	}
}
