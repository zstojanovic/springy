package org.springy.data;

public class SpringData {
  public int id;
  public int nodeAId;
  public int nodeBId;
  public float amplitude;
  public float phase;

  SpringData(){
  }

  public SpringData(int id, int nodeAId, int nodeBId, float amplitude, float phase) {
    this.id = id;
    this.nodeAId = nodeAId;
    this.nodeBId = nodeBId;
    this.amplitude = amplitude;
    this.phase = phase;
  }
}
