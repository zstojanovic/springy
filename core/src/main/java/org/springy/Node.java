package org.springy;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class Node {
  static final float RADIUS = 0.1f;
  static final float RADIUS_SQUARED = RADIUS * RADIUS;

  Body body;
  Vector2 position;
//  float x;
//  float y;
  float density;
  Sprite sprite;

  public Node(World world, Vector2 position) {
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

    sprite = new Sprite(new Texture("node.png"));
    sprite.setScale(0.5f);
    sprite.setOriginCenter();
    sprite.setCenter(position.x, position.y);
  }

  public void draw(Batch batch) {
    sprite.draw(batch);
  }
}
