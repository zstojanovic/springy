package org.springy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Spring {
  private static final float WIDTH = 0.05f;
  static final Set<Spring> springs = new HashSet<>();
  private static float time = 0;

  static Iterator<Spring> iterator() {
    return springs.iterator();
  }

  static void create(World world, Node a, Node b, float amplitude, float phase) {
    springs.add(new Spring(world, a, b, amplitude, phase));
  }

  static Spring find(Vector2 point) {
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

  static void drawAll(ShapeDrawer shapeDrawer) {
    for (Spring spring: springs) spring.draw(shapeDrawer);
  }

  static void resetAll() {
    time = 0;
    for (Spring spring: springs) spring.resetRestLength();
  }

  static void act(float delta) {
    for (Spring spring: springs) {
      if (spring.amplitude != 0) {
        float length = (float)(spring.restLength +
          (spring.amplitude * spring.restLength) * Math.sin(time * spring.frequency * 6.28 + (spring.phase/180*3.14)));
        spring.joint.setLength(length);
      }
    }
    time += delta;
  }

  static void remove(Spring spring) {
    springs.remove(spring);
    spring.dispose();
  }

  static boolean remove(Vector2 position) {
    var spring = find(position);
    if (spring != null) {
      remove(spring);
      return true;
    }
    return false;
  }

  World world;
  DistanceJoint joint;
  float amplitude;
  float frequency = 0.5f; // TODO hm, why?
  float phase;
  float restLength;
  Node a, b;
  boolean selected = false;

  private Spring(World world, Node a, Node b, float amplitude, float phase) {
    DistanceJointDef jointDef = new DistanceJointDef();
    jointDef.initialize(a.body, b.body, a.body.getWorldCenter(), b.body.getWorldCenter());
    jointDef.frequencyHz = 10;
    jointDef.dampingRatio = 0.9f;
    joint = (DistanceJoint)world.createJoint(jointDef);
    this.restLength = joint.getLength();

    this.world = world;
    this.a = a;
    this.b = b;
    this.amplitude = amplitude;
    this.phase = phase;
  }

  void resetRestLength() {
    joint.setLength(a.position.dst(b.position));
  }

  private void draw(ShapeDrawer shapeDrawer) {
    if (selected) {
      shapeDrawer.setColor(Color.YELLOW);
      shapeDrawer.setDefaultLineWidth(WIDTH * 1.5f);
      shapeDrawer.line(a.body.getPosition(), b.body.getPosition());
    }

    if (amplitude == 0) shapeDrawer.setColor(Color.WHITE); else shapeDrawer.setColor(Color.BLUE);
    if (selected) shapeDrawer.setDefaultLineWidth(WIDTH * 0.5f); else shapeDrawer.setDefaultLineWidth(WIDTH);
    shapeDrawer.line(a.body.getPosition(), b.body.getPosition());
  }

  private void dispose() {
    a = null;
    b = null;
    world.destroyJoint(joint);
    world = null;
  }
}
