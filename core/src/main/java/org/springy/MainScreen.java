package org.springy;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.ArrayList;
import java.util.List;

public class MainScreen extends ScreenAdapter {
  private OrthographicCamera camera;
  private Viewport viewport;

  private PolygonSpriteBatch batch;
  private ShapeDrawer shapeDrawer;
  private Sprite background;
  private Stage stage;
  private InputHandler inputHandler = new InputHandler();
  private Table table;

  private World world = new World(new Vector2(0, -10), true);
  private Bounds bounds = new Bounds(world, new Vector2[] {
    new Vector2(0, 9), new Vector2(0, 0), new Vector2(16, 0), new Vector2(16, 9), });
  private List<Node> nodes = new ArrayList<>();
  private List<Spring> springs = new ArrayList<>();
  private boolean isRunning = false;
  private float time;

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

    background = new Sprite(bgTexture, 2*20*64, (int)(2*11.25*64)); // 20x11,25
    background.setOrigin(0, 0);
    background.setScale(0.00625f);

    createUI();
  }

  private void createUI() {
    stage = new Stage(new ScreenViewport());
    Skin skin = new Skin(Gdx.files.internal("skin/test/uiskin.json"));
    Gdx.input.setInputProcessor(stage);

    table = new Window("", skin);
    //table.setBackground(skin.getDrawable("default-pane"));
    table.setPosition(10, stage.getHeight() - 160);
    table.setSize(150, 150);

    TextButton textButton = new TextButton("Start", skin);
    textButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        isRunning = true;
        super.clicked(event, x, y);
      }
    });
    table.add(textButton).space(8.0f);

    table.row();
    Label label = new Label("Amplitude", skin);
    table.add(label).space(8.0f);

    table.row();
    Slider slider = new Slider(0.0f, 100.0f, 10.0f, false, skin, "default-horizontal");
    table.add(slider).space(8.0f);
    stage.addActor(table);

    var mux = new InputMultiplexer();
    mux.addProcessor(stage);
    mux.addProcessor(inputHandler);
    Gdx.input.setInputProcessor(mux);
  }

  private Vector2 getMousePosition() {
    var p = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    return new Vector2(p.x, p.y);
  }

  class InputHandler extends InputAdapter {
    Node lastNode, movingNode, selectedNode;
    Spring selectedSpring;
    Vector3 lastPanPoint;

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
      var position = getMousePosition();
      switch (button) {
        case Input.Buttons.LEFT:
          if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            nodes.add(new Node(world, position));
          } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
            var node = findNode(position);
            if (node != null) {
              if (lastNode == null) {
                lastNode = node;
              } else {
                springs.add(new Spring(world, lastNode, node, 0));
                lastNode = node;
              }
            }
          }
          break;
        case Input.Buttons.RIGHT:
          var node = findNode(position);
          if (node != null) {
            var i = springs.iterator();
            while (i.hasNext()) {
              var s = i.next();
              if (s.a == node || s.b == node) {
                i.remove();
                s.dispose();
              }
            }
            nodes.remove(node);
            node.dispose();
          } else {
            var spring = findSpring(position);
            if (spring != null) {
              springs.remove(spring);
              spring.dispose();
            }
          }
      }
      return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        var panPoint = new Vector3(screenX, screenY, 0);
        var pan = camera.unproject(new Vector3(lastPanPoint)).sub(camera.unproject(new Vector3(panPoint)));
        if (!pan.isZero()) {
          camera.translate(pan);
        }
        lastPanPoint.set(panPoint);
      } else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
        if (movingNode != null) {
          movingNode.setPosition(getMousePosition());
        }
      }
      return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
      switch (button) {
        case Input.Buttons.RIGHT:
          lastPanPoint = new Vector3(screenX, screenY, 0);
          break;
        case Input.Buttons.LEFT:
          if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            var position = getMousePosition();
            movingNode = findNode(position);
            if (movingNode != null) {
              if (selectedNode != null) selectedNode.selected = false;
              selectedNode = movingNode;
              selectedNode.selected = true;
            } else {
              var spring = findSpring(position);
              if (spring != null) {
                if (selectedSpring != null) selectedSpring.selected = false;
                selectedSpring = spring;
                selectedSpring.selected = true;
              }
            }
          }
      }
      return true;
    }

    @Override
    public boolean keyUp(int keycode) {
      if (keycode == Input.Keys.CONTROL_LEFT) {
        lastNode = null;
      }
      return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
      var zoom = camera.zoom * (1 + amountY/20f);
      if (zoom < 0.2) zoom = 0.2f;
      if (zoom > 5) zoom = 5;
      camera.zoom = zoom;
      return true;
    }
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    if (isRunning) {
			for (Spring spring: springs) {
				if (spring.amplitude != 0) {
					float length = (float)(spring.restLength + (spring.amplitude * spring.restLength) * Math.sin(time * spring.frequency * 6.28 + spring.phase));
					spring.joint.setLength(length);
				}
			}
      world.step(1f / 60f, 6, 2);
      time += 1f / 60f;
    }

    camera.update();
    batch.setProjectionMatrix(camera.combined);

    batch.begin();
    background.draw(batch);
    bounds.draw(shapeDrawer);
    for (Spring spring: springs) spring.draw(shapeDrawer);
    for (Node node: nodes) node.draw(shapeDrawer);
    batch.end();

    stage.getViewport().apply();
    stage.draw();
  }

  private Node findNode(Vector2 point) {
    for (Node n: nodes) {
      if (point.dst2(n.position) < (Node.RADIUS_SQUARED*1)) return n;
    }
    return null;
  }

  private Spring findSpring(Vector2 point) {
    Spring found = null;
    float foundDistance = Float.MAX_VALUE;
    for (Spring s: springs) {
      var d = Intersector.distanceSegmentPoint(s.a.position, s.b.position, point);
      if (d < Spring.WIDTH && d < foundDistance) {
        found = s;
        foundDistance = d;
      }
    }
    return found;
  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, false);
    stage.getViewport().update(width, height, true);
    table.setPosition(10, stage.getHeight() - 160);
  }

  @Override
  public void dispose() {
    batch.dispose();
  }
}
