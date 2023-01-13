package org.springy;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Game extends ApplicationAdapter {
	private OrthographicCamera camera;
	private Viewport viewport;

	private PolygonSpriteBatch batch;
	private Sprite background;
	private Stage stage;

	@Override
	public void create() {
		camera = new OrthographicCamera(1280, 720);
		camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
		camera.update();
		viewport = new ExtendViewport(1280, 720, camera);
		batch = new PolygonSpriteBatch();

		Texture bgTexture = new Texture(Gdx.files.internal("bluegrid.png"), true);
		bgTexture.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.MipMapNearestLinear);
		bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		background = new Sprite(bgTexture, 2*20*64, (int)(2*11.25*64)); // 20x 11,25
		background.setOrigin(0, 0);
		background.setScale(0.5f);

		createUI();
	}

	private void createUI() {
		stage = new Stage(new ScreenViewport());
		Skin skin = new Skin(Gdx.files.internal("skin/test/uiskin.json"));
		Gdx.input.setInputProcessor(stage);

		Table table = new Table();
		table.setBackground(skin.getDrawable("default-pane"));
		//table.setFillParent(true);
		table.setPosition(50, 50);
		table.setSize(150, 150);

		TextButton textButton = new TextButton("Edit", skin);
		table.add(textButton).space(8.0f);

		table.row();
		Label label = new Label("Amplitude", skin);
		table.add(label).space(8.0f);

		table.row();
		Slider slider = new Slider(0.0f, 100.0f, 10.0f, false, skin, "default-horizontal");
		table.add(slider).space(8.0f);
		stage.addActor(table);
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camera.translate(3, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) camera.translate(-3, 0);
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) camera.translate(0, 3);
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) camera.translate(0, -3);

		if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom -= 0.01;
		if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.zoom += 0.01;

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		background.draw(batch);
		batch.end();

		stage.getViewport().apply();
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, false);
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void dispose() {
		batch.dispose();
	}
}