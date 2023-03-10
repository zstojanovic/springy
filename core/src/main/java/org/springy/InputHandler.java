package org.springy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import org.springy.physics.Node;
import org.springy.physics.Spring;

public class InputHandler extends InputAdapter {
  private MainScreen screen;
  private Node lastNode, movingNode, selectedNode;
  Spring selectedSpring;
  private Vector3 lastPanPoint;

  InputHandler(MainScreen screen) {
    this.screen = screen;
  }

  void unselectSpring() {
    if (selectedSpring != null) {
      selectedSpring.setSelected(false);
      selectedSpring = null;
    }
  }

  private Vector2 getMousePosition() {
    var position = screen.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    return new Vector2(position.x, position.y);
  }

  @Override
  public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    var position = getMousePosition();
    switch (button) {
      case Input.Buttons.LEFT:
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
          screen.device.createNode(position);
        } else if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) {
          var node = screen.device.findNode(position);
          if (node != null) {
            if (lastNode != null) {
              screen.device.createSpring(lastNode, node, 0, 0);
            }
            lastNode = node;
          }
        }
        break;
      case Input.Buttons.RIGHT:
        var nodeRemoved = screen.device.removeNode(position);
        if (!nodeRemoved) screen.device.removeSpring(position);
    }
    return true;
  }

  @Override
  public boolean touchDragged(int screenX, int screenY, int pointer) {
    if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
      var panPoint = new Vector3(screenX, screenY, 0);
      var pan = screen.camera.unproject(new Vector3(lastPanPoint)).sub(screen.camera.unproject(new Vector3(panPoint)));
      if (!pan.isZero()) {
        screen.camera.translate(pan);
      }
      lastPanPoint.set(panPoint);
    } else if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
      if (movingNode != null) {
        screen.device.repositionNode(movingNode, getMousePosition());
      }
    }
    return true;
  }

  @Override
  public boolean touchDown(int screenX, int screenY, int pointer, int button) {
    switch (button) {
      case Input.Buttons.RIGHT:
        lastPanPoint = new Vector3(screenX, screenY, 0);
        break;
      case Input.Buttons.LEFT:
        if (!Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) && !Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
          var position = getMousePosition();
          movingNode = screen.device.findNode(position);
          if (movingNode != null) {
            if (selectedNode != null) selectedNode.setSelected(false);
            selectedNode = movingNode;
            selectedNode.setSelected(true);
          } else {
            var spring = screen.device.findSpring(position);
            if (spring != null) {
              if (selectedSpring != null) selectedSpring.setSelected(false);
              selectedSpring = spring;
              selectedSpring.setSelected(true);
              screen.onSpringSelected();
            }
          }
        }
    }
    return true;
  }

  @Override
  public boolean keyUp(int keycode) {
    if (keycode == Input.Keys.CONTROL_LEFT) {
      lastNode = null;
    }
    return true;
  }

  @Override
  public boolean scrolled(float amountX, float amountY) {
    var zoom = screen.camera.zoom * (1 + amountY/20f);
    if (zoom < 0.2) zoom = 0.2f;
    if (zoom > 5) zoom = 5;
    screen.camera.zoom = zoom;
    return true;
  }
}
