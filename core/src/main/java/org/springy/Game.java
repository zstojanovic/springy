package org.springy;

public class Game extends com.badlogic.gdx.Game {

	@Override
	public void create() {
		setScreen(new MainScreen());
	}
}