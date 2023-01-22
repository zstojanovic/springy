package org.springy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.springy.data.DeviceData;
import org.springy.physics.Bounds;
import org.springy.physics.Device;
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
  private Bounds bounds = new Bounds(world);
  Device device;
  private boolean isRunning = false;
  private boolean stateChangeRequested = false;

  Preferences preferences;

  @Override
  public void show() {
    camera = new OrthographicCamera(Game.WIDTH, Game.HEIGHT);
    camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
    camera.update();
    viewport = new ExtendViewport(Game.WIDTH, Game.HEIGHT, camera);
    batch = new PolygonSpriteBatch();

    var pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.drawPixel(0, 0);
    var texture = new Texture(pixmap); //remember to dispose of later
    pixmap.dispose();
    var region = new TextureRegion(texture, 0, 0, 1, 1);
    shapeDrawer = new ShapeDrawer(batch, region);

    var bgTexture = new Texture(Gdx.files.internal("bluegrid.png"), true);
    bgTexture.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.MipMapNearestLinear);
    bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

    background = new Sprite(bgTexture, 2*20*64, (int)(2*11.25*64));
    background.setOrigin(0, 0);
    background.setScale(0.00625f);

    createUI();

    device = new Device(world);
    preferences = Gdx.app.getPreferences("springy");
  }

  private void createUI() {
    stage = new Stage(new ScreenViewport());
    var skin = new Skin(Gdx.files.internal("skin/test/uiskin.json"));
    Gdx.input.setInputProcessor(stage);

    window = new Window("", skin);
    window.setPosition(10, stage.getHeight() - 170);
    window.setSize(220, 160);

    window.add();
    var button = new TextButton("Start/Stop", skin);
    button.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        stateChangeRequested = true;
      }
    });
    window.add(button).padBottom(10);

    window.row();
    var amplLabel = new Label("Ampl", skin);
    window.add(amplLabel).padRight(10);

    amplSlider = new Slider(0.0f, 0.5f, 0.1f, false, skin);
    amplSlider.setDisabled(true);
    amplSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (inputHandler.selectedSpring != null) {
          inputHandler.selectedSpring.setAmplitude(amplSlider.getValue());
        }
      }
    });
    window.add(amplSlider);

    window.row();
    var phaseLabel = new Label("Phase", skin);
    window.add(phaseLabel).padRight(10).padBottom(10);

    phaseSlider = new Slider(0, 360, 30, false, skin);
    phaseSlider.setDisabled(true);
    phaseSlider.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (inputHandler.selectedSpring != null) {
          inputHandler.selectedSpring.setPhase(phaseSlider.getValue());
        }
      }
    });
    window.add(phaseSlider);

    window.row();
    var saveButton = new TextButton("Save", skin);
    saveButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        var save1 = new Json().toJson(device.getData());
        preferences.putString("save1", save1);
        preferences.flush();
      }
    });
    window.add(saveButton);

    var loadButton = new TextButton("Load", skin);
    loadButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        var save1 = preferences.getString("save1");
        var data = new Json().fromJson(DeviceData.class, save1);
        if (data != null) device.load(data);
      }
    });
    window.add(loadButton);
    stage.addActor(window);

    var mux = new InputMultiplexer();
    mux.addProcessor(stage);
    mux.addProcessor(inputHandler);
    Gdx.input.setInputProcessor(mux);
  }

  void onSpringSelected() {
    amplSlider.setDisabled(false);
    amplSlider.setValue(inputHandler.selectedSpring.amplitude());
    phaseSlider.setDisabled(false);
    phaseSlider.setValue(inputHandler.selectedSpring.phase());
  }

  void act(float delta) {
    stage.act(delta);

    if (stateChangeRequested) {
      if (!isRunning) {
        isRunning = true;
        inputHandler.unselectSpring();
      } else {
        isRunning = false;
        device.reset();
      }
      stateChangeRequested = false;
    }
    if (isRunning) {
      device.act(delta);
      world.step(delta, 6, 2);
    }
  }

  @Override
  public void render(float delta) {
    act(delta);

    Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    camera.update();
    batch.setProjectionMatrix(camera.combined);

    batch.begin();
    background.draw(batch);
    bounds.draw(shapeDrawer);
    device.draw(shapeDrawer);
    batch.end();

    stage.getViewport().apply();
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    stage.getViewport().update(width, height, true);
    window.setPosition(10, stage.getHeight() - 170);
  }

  @Override
  public void dispose() {
    batch.dispose();
  }
}
