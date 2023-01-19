package org.springy.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.HashSet;
import java.util.Set;

public class Device {
  private World world;
  private Set<Node> nodes = new HashSet<>();
  private Set<Spring> springs = new HashSet<>();
  private float time = 0;

  public Device(World world) {
    this.world = world;
  }

  public void createNode(Vector2 position) {
    nodes.add(new Node(world, position));
  }

  public Node findNode(Vector2 position) {
    for (Node n: nodes) {
      if (position.dst2(n.position) < Node.RADIUS_SQUARED) return n;
    }
    return null;
  }

  public void repositionNode(Node node, Vector2 position) {
    node.setPosition(position);
    for (Spring spring: springs) {
      if (spring.a == node || spring.b == node) {
        spring.resetRestLength();
      }
    }
  }

  public boolean removeNode(Vector2 position) {
    var node = findNode(position);
    if (node != null) {
      var iterator = springs.iterator();
      while (iterator.hasNext()) {
        var spring = iterator.next();
        if (spring.a == node || spring.b == node) {
          removeSpring(spring);
        }
      }
      nodes.remove(node);
      node.dispose();
      return true;
    }
    return false;
  }

  public void createSpring(Node a, Node b, float amplitude, float phase) {
    springs.add(new Spring(world, a, b, amplitude, phase));
  }

  public Spring findSpring(Vector2 position) {
    Spring found = null;
    float foundDistance = Float.MAX_VALUE;
    for (Spring s: springs) {
      var d = Intersector.distanceSegmentPoint(s.a.position, s.b.position, position);
      if (d < Spring.WIDTH && d < foundDistance) {
        found = s;
        foundDistance = d;
      }
    }
    return found;
  }

  public boolean removeSpring(Vector2 position) {
    var spring = findSpring(position);
    if (spring != null) {
      removeSpring(spring);
      return true;
    }
    return false;
  }

  private void removeSpring(Spring spring) {
    springs.remove(spring);
    spring.dispose();
  }

  public void reset() {
    time = 0;
    for (Node node: nodes) node.reset();
    for (Spring spring: springs) spring.resetRestLength();
  }

  public void act(float delta) {
    for (Spring spring: springs) {
      if (spring.amplitude != 0) {
        float length = (float)(spring.restLength +
          (spring.amplitude * spring.restLength) * Math.sin(time * spring.frequency * 6.28 + (spring.phase/180*3.14)));
        spring.joint.setLength(length);
      }
    }
    time += delta;
  }

  public void draw(ShapeDrawer shapeDrawer) {
    for (Spring spring: springs) spring.draw(shapeDrawer);
    for (Node node: nodes) node.draw(shapeDrawer);
  }
}
