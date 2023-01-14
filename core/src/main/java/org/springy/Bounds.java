package org.springy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Bounds {
  private Array<Vector2> vertices;

  Bounds(World world, Vector2[] vertices) {
    this.vertices = new Array<>(vertices);
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
    shapeDrawer.path(vertices, true);
  }
}
