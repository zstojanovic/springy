package org.springy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
  private Sprite background, logo;
  private Stage stage;
  private Table table;

  private World world = new World(new Vector2(0, -10), true);
  private List<Node> nodes = new ArrayList<>();
  private List<Spring> springs = new ArrayList<>();
  private boolean isRunning = false;
  private Node lastNode;

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
    shapeDrawer.setDefaultLineWidth(0.05f);

    Texture bgTexture = new Texture(Gdx.files.internal("bluegrid.png"), true);
    bgTexture.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.MipMapNearestLinear);
    bgTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

    background = new Sprite(bgTexture, 2*20*64, (int)(2*11.25*64)); // 20x11,25
    background.setOrigin(0, 0);
    background.setScale(0.00625f);

    logo = new Sprite(new Texture("logo.png"));
    logo.setOriginCenter();
    logo.setCenter(0, 0);

    createUI();
  }

  private void createUI() {
    stage = new Stage(new ScreenViewport());
    Skin skin = new Skin(Gdx.files.internal("skin/test/uiskin.json"));
    Gdx.input.setInputProcessor(stage);

    table = new Table();
    table.setBackground(skin.getDrawable("default-pane"));
    //table.setFillParent(true);
    table.setPosition(10, stage.getHeight() - 160);
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

  private void handleInput() {
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) camera.translate(0.01f, 0);
    if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) camera.translate(-0.01f, 0);
    if (Gdx.input.isKeyPressed(Input.Keys.UP)) camera.translate(0, 0.01f);
    if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) camera.translate(0, -0.01f);

    if (Gdx.input.isKeyPressed(Input.Keys.Q)) camera.zoom -= 0.01;
    if (Gdx.input.isKeyPressed(Input.Keys.A)) camera.zoom += 0.01;

    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
      var c = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
      nodes.add(new Node(world, new Vector2(c.x, c.y)));
      System.out.println(c.x + " " + c.y);
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT)) {
      System.out.println("reset");
      lastNode = null;
    }
    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
      var c = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
      var node = findNode(c.x, c.y);
      System.out.println("node " + node);
      if (node != null) {
        if (lastNode == null) {
          lastNode = node;
        } else {
          springs.add(new Spring(world, lastNode, node));
          lastNode = node;
        }
      }
    }
  }

  @Override
  public void render(float delta) {
    Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    handleInput();

    if (isRunning) {
//			for (OldSpring spring: springs) {
//				if (spring.amplitude != 0) {
//					float length = (float)(spring.restLength + (spring.amplitude * spring.restLength) * Math.sin(time * spring.frequency * 6.28 + spring.phase));
//					spring.joint.setLength(length);
//				}
//			}
      world.step(1f / 60f, 6, 2);
      //time += 1f / 60f;
    }

    camera.update();
    batch.setProjectionMatrix(camera.combined);

    batch.begin();
    background.draw(batch);
    for (Spring spring: springs) spring.draw(shapeDrawer);
    for (Node node: nodes) node.draw(shapeDrawer);
    batch.end();

    stage.getViewport().apply();
    stage.draw();
  }

  private Node findNode(float x, float y) {
    Vector2 v = new Vector2(x, y);
    for (Node n: nodes) {
      if (v.dst2(n.position) < (Node.RADIUS_SQUARED*1)) return n;
    }
    return null;
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
