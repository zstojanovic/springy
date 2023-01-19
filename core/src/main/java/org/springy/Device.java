package org.springy;

import com.badlogic.gdx.physics.box2d.World;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Device {
  private World world;

  Device(World world) {
    this.world = world;
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
