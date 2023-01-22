package org.springy.data;

public class DeviceData {
  public NodeData[] nodes;
  public SpringData[] springs;

  DeviceData() {
  }

  public DeviceData(NodeData[] nodes, SpringData[] springs) {
    this.nodes = nodes;
    this.springs = springs;
  }
}
