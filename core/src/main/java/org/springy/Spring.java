package org.springy;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Spring {
  DistanceJoint joint;
  float amplitude = 0;
  float frequency = 0.5f; // TODO hm, why?
  float phase = 0;
  float restLength;
  Node a, b;

  public Spring(World world, Node a, Node b) {
    //DistanceJoint joint, float amplitude, float frequency, float phase) {
    DistanceJointDef jointDef = new DistanceJointDef();
    jointDef.initialize(a.body, b.body, a.body.getWorldCenter(), b.body.getWorldCenter());
    jointDef.frequencyHz = 10;
    jointDef.dampingRatio = 0.9f;
    joint = (DistanceJoint)world.createJoint(jointDef);
    this.restLength = joint.getLength();

    this.a = a;
    this.b = b;
  }

  public void draw(ShapeDrawer shapeDrawer) {
    shapeDrawer.line(a.position, b.position);
  }
}
