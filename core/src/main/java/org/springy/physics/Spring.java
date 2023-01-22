package org.springy.physics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import org.springy.data.SpringData;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Spring {
  static final float WIDTH = 0.05f;

  int id;
  World world;
  DistanceJoint joint;
  float amplitude;
  float frequency = 0.5f; // TODO hm, why?
  float phase;
  float restLength;
  Node a, b;
  boolean selected = false;

  Spring(int id, World world, Node a, Node b, float amplitude, float phase) {
    DistanceJointDef jointDef = new DistanceJointDef();
    jointDef.initialize(a.body, b.body, a.body.getWorldCenter(), b.body.getWorldCenter());
    jointDef.frequencyHz = 10;
    jointDef.dampingRatio = 0.9f;
    joint = (DistanceJoint)world.createJoint(jointDef);
    this.restLength = joint.getLength();

    this.id = id;
    this.world = world;
    this.a = a;
    this.b = b;
    this.amplitude = amplitude;
    this.phase = phase;
  }

  public float amplitude() {
    return amplitude;
  }

  public void setAmplitude(float amplitude) {
    this.amplitude = amplitude;
  }

  public float phase() {
    return phase;
  }

  public void setPhase(float phase) {
    this.phase = phase;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  void resetRestLength() {
    joint.setLength(a.position.dst(b.position));
  }

  void draw(ShapeDrawer shapeDrawer) {
    if (selected) {
      shapeDrawer.setColor(Color.YELLOW);
      shapeDrawer.setDefaultLineWidth(WIDTH * 1.5f);
      shapeDrawer.line(a.body.getPosition(), b.body.getPosition());
    }

    if (amplitude == 0) shapeDrawer.setColor(Color.WHITE); else shapeDrawer.setColor(Color.BLUE);
    if (selected) shapeDrawer.setDefaultLineWidth(WIDTH * 0.5f); else shapeDrawer.setDefaultLineWidth(WIDTH);
    shapeDrawer.line(a.body.getPosition(), b.body.getPosition());
  }

  void dispose() {
    a = null;
    b = null;
    world.destroyJoint(joint);
    world = null;
  }

  SpringData getData() {
    return new SpringData(id, a.id, b.id, amplitude, phase);
  }
}
