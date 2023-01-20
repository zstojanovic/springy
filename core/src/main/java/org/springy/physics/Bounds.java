package org.springy.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Bounds {
  private Array<Vector2> path;

  public Bounds(World world) {
    var vertices = new Vector2[] { new Vector2(0, 9), new Vector2(0, 2),
      new Vector2(8, 0), new Vector2(16, 0), new Vector2(16, 9) };
    path = new Array<>(vertices);
    BodyDef bodyDef1 = new BodyDef();
    bodyDef1.type = BodyDef.BodyType.StaticBody;
    FixtureDef fixtureDef1 = new FixtureDef();
    fixtureDef1.friction = 1;
    ChainShape shape1 = new ChainShape();
    shape1.createChain(vertices);
    fixtureDef1.shape = shape1;
    Body ground1 = world.createBody(bodyDef1);
    ground1.createFixture(fixtureDef1);
    shape1.dispose();
  }

  public void draw(ShapeDrawer shapeDrawer) {
    shapeDrawer.setDefaultLineWidth(0.05f);
    shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.path(path, true);
  }
}
