package org.springy;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Device {
  private World world;

  Device(World world) {
    this.world = world;
  }

  void createNode(Vector2 position) {
    Node.create(world, position);
  }

  Node findNode(Vector2 position) {
    return Node.find(position);
  }

  void repositionNode(Node node, Vector2 position) {
    node.setPosition(position);
    for (Spring spring: Spring.springs) {
      if (spring.a == node || spring.b == node) {
        spring.resetRestLength();
      }
    }
  }

  boolean removeNode(Vector2 position) {
    return Node.remove(position);
  }

  void createSpring(Node a, Node b, float amplitude, float phase) {
    Spring.create(world, a, b, amplitude, phase);
  }

  Spring findSpring(Vector2 position) {
    return Spring.find(position);
  }

  boolean removeSpring(Vector2 position) {
    return Spring.remove(position);
  }

  void reset() {
    Node.resetAll();
    Spring.resetAll();
  }

  void act(float delta) {
    Spring.act(delta);
  }

  void draw(ShapeDrawer shapeDrawer) {
    Spring.drawAll(shapeDrawer);
    Node.drawAll(shapeDrawer);
  }
}
