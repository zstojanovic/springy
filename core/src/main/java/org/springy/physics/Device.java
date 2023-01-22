package org.springy.physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import org.springy.data.DeviceData;
import org.springy.data.NodeData;
import org.springy.data.SpringData;
import space.earlygrey.shapedrawer.ShapeDrawer;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Device {
  private World world;
  private AtomicInteger idSequence = new AtomicInteger();
  private Map<Integer, Node> nodeMap = new HashMap<>();
  private Set<Spring> springs = new HashSet<>();
  private float time = 0;

  public Device(World world) {
    this.world = world;
  }

  public void createNode(Vector2 position) {
    createNode(idSequence.getAndAdd(1), position);
  }

  private void createNode(int id, Vector2 position) {
    nodeMap.put(id, new Node(id, world, position));
  }

  public Node findNode(Vector2 position) {
    for (Node n: nodeMap.values()) {
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
      removeNode(node);
      return true;
    }
    return false;
  }

  private void removeNode(Node node) {
    var iterator = springs.iterator();
    while (iterator.hasNext()) {
      var spring = iterator.next();
      if (spring.a == node || spring.b == node) {
        iterator.remove();
        spring.dispose();
      }
    }
    nodeMap.remove(node.id);
    node.dispose();
  }

  public void createSpring(Node a, Node b, float amplitude, float phase) {
    createSpring(idSequence.getAndAdd(1), a, b, amplitude, phase);
  }

  private void createSpring(int id, Node a, Node b, float amplitude, float phase) {
    springs.add(new Spring(id, world, a, b, amplitude, phase));
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
      springs.remove(spring);
      spring.dispose();
      return true;
    }
    return false;
  }

  public void reset() {
    time = 0;
    nodeMap.values().forEach(Node::reset);
    springs.forEach(Spring::resetRestLength);
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
    springs.forEach(spring -> spring.draw(shapeDrawer));
    nodeMap.values().forEach(node -> node.draw(shapeDrawer));
  }

  public DeviceData getData() {
    return new DeviceData(
      nodeMap.values().stream().map(Node::getData).toArray(NodeData[]::new),
      springs.stream().map(Spring::getData).toArray(SpringData[]::new));
  }

  public void load(DeviceData data) {
    reset();
    springs.forEach(Spring::dispose);
    springs.removeIf(s -> true);
    nodeMap.values().forEach(Node::dispose);
    nodeMap.entrySet().removeIf(e -> true);

    Arrays.stream(data.nodes).forEach(d -> createNode(d.id, new Vector2(d.x, d.y)));
    Arrays.stream(data.springs).forEach(d -> {
      var a = nodeMap.get(d.nodeAId);
      var b = nodeMap.get(d.nodeBId);
      createSpring(d.id, a, b, d.amplitude, d.phase);
    });

    var maxNodeId = Arrays.stream(data.nodes).mapToInt(n -> n.id).max().orElse(0);
    var maxSpringId = Arrays.stream(data.springs).mapToInt(s -> s.id).max().orElse(0);

    idSequence.set(Integer.max(maxNodeId, maxSpringId) + 1);
  }
}
