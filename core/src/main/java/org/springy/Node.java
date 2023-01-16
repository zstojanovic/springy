package org.springy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashSet;
import java.util.Set;

public class Node {
  private static final float RADIUS = 0.1f;
  private static final float RADIUS_SQUARED = RADIUS * RADIUS;
  private static final Set<Node> nodes = new HashSet<>();

  static void create(World world, Vector2 position) {
    nodes.add(new Node(world, position));
  }

  static Node find(Vector2 point) {
    for (Node n: nodes) {
      if (point.dst2(n.position) < RADIUS_SQUARED) return n;
    }
    return null;
  }

  static void drawAll(ShapeDrawer shapeDrawer) {
    for (Node node: nodes) node.draw(shapeDrawer);
  }

  static void remove(Node node) {
    nodes.remove(node);
    node.dispose();
  }

  static boolean remove(Vector2 position) {
    var node = find(position);
    if (node != null) {
      var iterator = Spring.iterator();
      while (iterator.hasNext()) {
        var spring = iterator.next();
        if (spring.a == node || spring.b == node) {
          Spring.remove(spring);
        }
      }
      remove(node);
      return true;
    }
    return false;
  }

  World world;
  Body body;
  Vector2 position;
  float density;
  boolean selected = false;

  private Node(World world, Vector2 position) {
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

  void setPosition(Vector2 position) {
    this.position.set(position.x, position.y);
    body.setTransform(position, 0);
  }

  private void draw(ShapeDrawer shapeDrawer) {
    if (selected) shapeDrawer.setColor(Color.YELLOW); else shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.filledCircle(body.getPosition(), RADIUS);
  }

  private void dispose() {
    world.destroyBody(body); // TODO other box2d objects too
    world = null;
  }
}
