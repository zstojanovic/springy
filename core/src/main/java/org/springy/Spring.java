package org.springy;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Spring {
  public static final float WIDTH = 0.05f;

  World world;
  DistanceJoint joint;
  float amplitude = 0;
  float frequency = 0.5f; // TODO hm, why?
  float phase = 0;
  float restLength;
  Node a, b;
  boolean selected = false;

  public Spring(World world, Node a, Node b) {
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
  }

  public void draw(ShapeDrawer shapeDrawer) {
    if (selected) shapeDrawer.setColor(Color.YELLOW); else shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.setDefaultLineWidth(WIDTH);
    shapeDrawer.line(a.body.getPosition(), b.body.getPosition());
  }

  public void dispose() {
    a = null;
    b = null;
    world.destroyJoint(joint);
    world = null;
  }
}
