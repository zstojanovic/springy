package org.springy;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class MainScreen extends ScreenAdapter {
  OrthographicCamera camera;
  private Viewport viewport;

  private PolygonSpriteBatch batch;
  private ShapeDrawer shapeDrawer;
  private Sprite background;
  private Stage stage;
  private InputHandler inputHandler = new InputHandler(this);
  private Window window;
  private Slider amplSlider, phaseSlider;

  World world = new World(new Vector2(0, -10), true);
  private Bounds bounds = new Bounds(world, new Vector2[] {
    new Vector2(0, 9), new Vector2(0, 4), new Vector2(8, 0), new Vector2(16, 0), new Vector2(16, 9), });
  private boolean isRunning = false;
  private boolean stateChangeRequested = false;

  @Override
  public void show() {
    camera = new OrthographicCamera(Game.WIDTH, Game.HEIGHT);
    camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
    camera.update();
    viewport = new ExtendViewport(Game.WIDTH, Game.HEIGHT, camera);
    batch = new PolygonSpriteBatch();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.drawPixel(0, 0);
    Texture texture = new Texture(pixmap); //remember to dispose of later
    pixmap.dispose();
    TextureRegion region = new TextureRegion(texture, 0, 0, 1, 1);
    shapeDrawer = new ShapeDrawer(batch, region);

    Texture bgTexture = new Texture(Gdx.files.internal("bluegrid.png"), true);
    bgTexture.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.MipMapNearestLinear);
    bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

    background = new Sprite(bgTexture, 2*20*64, (int)(2*11.25*64));
    background.setOrigin(0, 0);
    background.setScale(0.00625f);

    createUI();
  }

  private void createUI() {
    stage = new Stage(new ScreenViewport());
    Skin skin = new Skin(Gdx.files.internal("skin/test/uiskin.json"));
    Gdx.input.setInputProcessor(stage);

    window = new Window("", skin);
    window.setPosition(10, stage.getHeight() - 160);
    window.setSize(220, 150);

    window.add();
    TextButton textButton = new TextButton("Start/Stop", skin);
    textButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        stateChangeRequested = true;
      }
    });
    window.add(textButton).padBottom(10);

    window.row();
    Label amplLabel = new Label("Ampl", skin);
    window.add(amplLabel).padRight(10);

    amplSlider = new Slider(0.0f, 0.5f, 0.1f, false, skin);
    amplSlider.setDisabled(true);
    amplSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (inputHandler.selectedSpring != null) {
          inputHandler.selectedSpring.amplitude = amplSlider.getValue();
        }
      }
    });
    window.add(amplSlider);

    window.row();
    Label label = new Label("Phase", skin);
    window.add(label).padRight(10.0f);

    phaseSlider = new Slider(0, 360, 30, false, skin);
    phaseSlider.setDisabled(true);
    phaseSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (inputHandler.selectedSpring != null) {
          inputHandler.selectedSpring.phase = phaseSlider.getValue();
        }
      }
    });
    window.add(phaseSlider);
    stage.addActor(window);

    var mux = new InputMultiplexer();
    mux.addProcessor(stage);
    mux.addProcessor(inputHandler);
    Gdx.input.setInputProcessor(mux);
  }

  void onSpringSelected() {
    System.out.println("onSpringSelected...");
    amplSlider.setDisabled(false);
    amplSlider.setValue(inputHandler.selectedSpring.amplitude);
    phaseSlider.setDisabled(false);
    phaseSlider.setValue(inputHandler.selectedSpring.phase);
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (stateChangeRequested) {
      if (!isRunning) {
        System.out.println("Starting...");
        isRunning = true;
        inputHandler.unselectSpring();
      } else {
        System.out.println("Resetting...");
        isRunning = false;
        Node.resetAll();
        Spring.resetAll();
      }
      stateChangeRequested = false;
    }
    if (isRunning) {
      Spring.act(1f / 60f);
      world.step(1f / 60f, 6, 2);
    }

    camera.update();
    batch.setProjectionMatrix(camera.combined);

    batch.begin();
    background.draw(batch);
    bounds.draw(shapeDrawer);
    Spring.drawAll(shapeDrawer);
    Node.drawAll(shapeDrawer);
    batch.end();

    stage.getViewport().apply();
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    stage.getViewport().update(width, height, true);
    window.setPosition(10, stage.getHeight() - 160);
  }

  @Override
  public void dispose() {
    batch.dispose();
  }
}
