package org.springy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Node {
  static final float RADIUS = 0.1f;
  static final float RADIUS_SQUARED = RADIUS * RADIUS;

  World world;
  Body body;
  Vector2 position;
  float density;

  public Node(World world, Vector2 position) {
    this.world = world;
    this.position = position;
    BodyDef bodyDef = new BodyDef();
    bodyDef.type = BodyDef.BodyType.DynamicBody;
    bodyDef.position.set(position);
    body = world.createBody(bodyDef);
    Shape shape = new CircleShape();
    shape.setRadius(RADIUS);
    FixtureDef fixtureDef = new FixtureDef();
    fixtureDef.shape = shape;
    fixtureDef.density = 0.1f;
    fixtureDef.restitution = 0.4f;
    body.createFixture(fixtureDef);
    density = fixtureDef.density; // TODO is this needed?
    shape.dispose();
  }

  public void setPosition(float x, float y) {
    position.set(x, y);
    body.setTransform(position, 0);
  }

  public void draw(ShapeDrawer shapeDrawer) {
    shapeDrawer.filledCircle(body.getPosition(), RADIUS);
  }

  public void dispose() {
    world.destroyBody(body); // TODO other box2d objects too
    world = null;
  }
}
