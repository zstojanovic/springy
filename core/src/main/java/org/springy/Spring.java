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
  private static final Set<Spring> springs = new HashSet<>();
  private static float time = 0;

  static Iterator<Spring> iterator() {
    return springs.iterator();
  }

  static void create(World world, Node a, Node b, float amplitude) {
    springs.add(new Spring(world, a, b, amplitude));
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

  static void act(float delta) {
    for (Spring spring: springs) {
      if (spring.amplitude != 0) {
        float length = (float)(spring.restLength + (spring.amplitude * spring.restLength) * Math.sin(time * spring.frequency * 6.28 + spring.phase));
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
    var spring = Spring.find(position);
    if (spring != null) {
      remove(spring);
      return true;
    }
    return false;
  }

  World world;
  DistanceJoint joint;
  float amplitude = 0;
  float frequency = 0.5f; // TODO hm, why?
  float phase = 0;
  float restLength;
  Node a, b;
  boolean selected = false;

  private Spring(World world, Node a, Node b, float amplitude) {
    //DistanceJoint joint, float amplitude, float frequency, float phase) {
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
  }

  private void draw(ShapeDrawer shapeDrawer) {
    if (selected) shapeDrawer.setColor(Color.YELLOW); else shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.setDefaultLineWidth(WIDTH);
    shapeDrawer.line(a.body.getPosition(), b.body.getPosition());
  }

  private void dispose() {
    a = null;
    b = null;
    world.destroyJoint(joint);
    world = null;
  }
}
