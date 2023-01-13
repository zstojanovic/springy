package org.springy;

public class Game extends com.badlogic.gdx.Game {
	public static final int WIDTH = 16;
	public static final int HEIGHT = 9;

	@Override
	public void create() {
		setScreen(new MainScreen());
	}
}