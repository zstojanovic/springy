package org.springy.physics;

import com.badlogic.gdx.graphics.Color;
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
  boolean selected = false;

  Node(World world, Vector2 position) {
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
    fixtureDef.friction = 1;
    body.createFixture(fixtureDef);
    body.setFixedRotation(true);
    density = fixtureDef.density; // TODO is this needed?
    shape.dispose();
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  void reset() {
    body.setAngularVelocity(0);
    body.setLinearVelocity(0,0);
    body.setTransform(position, 0);
  }

  void setPosition(Vector2 position) {
    this.position.set(position.x, position.y);
    body.setTransform(position, 0);
  }

  void draw(ShapeDrawer shapeDrawer) {
    shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.filledCircle(body.getPosition(), RADIUS);
  }

  void dispose() {
    world.destroyBody(body);
    world = null;
  }
}
