package at.haha007.minigames.jumpandrun;

import java.util.ArrayList;

import net.minecraft.server.v1_15_R1.BlockPosition;

public class JumpAndRunCheckpoint {
	private BlockPosition pos;
	private ArrayList<String> commands;
	private float money;

	public BlockPosition getPos() {
		return pos;
	}
}
