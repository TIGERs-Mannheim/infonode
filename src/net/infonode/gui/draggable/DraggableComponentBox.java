/** 
 * Copyright (C) 2004 NNL Technology AB
 * Visit www.infonode.net for information about InfoNode(R) 
 * products and how to contact NNL Technology AB.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA 02111-1307, USA.
 */


// $Id: DraggableComponentBox.java,v 1.7 2004/07/06 15:08:44 jesper Exp $
package net.infonode.gui.draggable;

import net.infonode.gui.ComponentUtils;
import net.infonode.gui.ScrollableBox;
import net.infonode.gui.layout.DirectionLayout;
import net.infonode.gui.panel.SimplePanel;
import net.infonode.util.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;

public class DraggableComponentBox extends SimplePanel {
  private JComponent componentBox;
  private JComponent componentContainer;
  private Direction componentDirection = Direction.UP;
  private Direction scrollButtonDirection = Direction.UP;
  private boolean scrollEnabled = true;
  private boolean ensureSelectedVisible = false;
  private boolean autoSelect = true;
  private int scrollOffset = 0;
  private DraggableComponent selectedComponent;
  private ArrayList listeners;
  private ArrayList componentList = new ArrayList(4);

  private DraggableComponentListener draggableComponentListener = new DraggableComponentListener() {
    public void changed(DraggableComponentEvent event) {
      fireChangedEvent(event);
    }

    public void selected(DraggableComponentEvent event) {
      doSelectComponent(event.getSource());
    }

    public void dragged(DraggableComponentEvent event) {
      fireDraggedEvent(event);
    }

    public void dropped(DraggableComponentEvent event) {
      fireDroppedEvent(event);
    }

    public void dragAborted(DraggableComponentEvent event) {
      fireNotDroppedEvent(event);
    }
  };

  public DraggableComponentBox() {
    // Fix minimum size when flipping direction
    final DirectionLayout layout = new DirectionLayout(componentDirection == Direction.UP ? Direction.RIGHT :
                                                       componentDirection == Direction.LEFT ? Direction.DOWN :
                                                       componentDirection == Direction.DOWN ? Direction.RIGHT :
                                                       Direction.DOWN) {
      public Dimension minimumLayoutSize(Container parent) {
        Dimension min = super.minimumLayoutSize(parent);
        Dimension pref = super.preferredLayoutSize(parent);
        return componentDirection.isHorizontal() ? new Dimension(pref.width, min.height) : new Dimension(min.width, pref.height);
      }
    };
    componentBox = new SimplePanel(layout);
    componentBox.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        fireChangedEvent();
      }

      public void componentMoved(ComponentEvent e) {
        fireChangedEvent();
      }
    });

    initialize();
  }

  public void addListener(DraggableComponentBoxListener listener) {
    if (listeners == null)
      listeners = new ArrayList(2);

    listeners.add(listener);
  }

  public void removeListener(DraggableComponentBoxListener listener) {
    if (listeners != null) {
      listeners.remove(listener);

      if (listeners.size() == 0)
        listeners = null;
    }
  }

  public void addDraggableComponent(DraggableComponent component) {
    insertDraggableComponent(component, -1);
  }

  public void insertDraggableComponent(DraggableComponent component, int index) {
    component.addListener(draggableComponentListener);
    if (index < 0)
      componentBox.add(component.getComponent());
    else
      componentBox.add(component.getComponent(), index);

    componentList.add(component);
    component.setOuterParentArea(this);
    fireAddedEvent(component);
    if (autoSelect && componentBox.getComponentCount() == 1 && selectedComponent == null && component.isEnabled())
      doSelectComponent(component);
  }

  public void insertDraggableComponent(DraggableComponent component, Point p) {
    int componentIndex = ComponentUtils.getComponentIndex(componentBox.getComponentAt(SwingUtilities.convertPoint(this, p, componentBox)));
    if (componentIndex != -1 && componentBox.getComponentCount() > 0)
      insertDraggableComponent(component, componentIndex);
    else
      insertDraggableComponent(component, -1);
  }

  public void selectDraggableComponent(DraggableComponent component) {
    if (component == null) {
      if (selectedComponent != null) {
        DraggableComponent oldSelected = selectedComponent;
        selectedComponent = null;
        fireSelectedEvent(selectedComponent, oldSelected);
        componentBox.repaint();
      }
    }
    else
      component.select();
  }

  public void removeDraggableComponent(DraggableComponent component) {
    if (component != null && component.getComponent().getParent() == componentBox) {
      int index = ComponentUtils.getComponentIndex(component.getComponent());
      component.removeListener(draggableComponentListener);
      if (componentBox.getComponentCount() > 1 && selectedComponent != null) {
        if (selectedComponent == component) {
          if (autoSelect) {
            int selectIndex = findSelectableComponentIndex(index);
            if (selectIndex > -1)
              selectDraggableComponent(findDraggableComponent(componentBox.getComponent(selectIndex)));
            else
              selectedComponent = null;
          }
          else {
            selectDraggableComponent(null);
          }
        }
      }
      else {
        if (selectedComponent != null) {
          DraggableComponent oldSelected = selectedComponent;
          selectedComponent = null;
          fireSelectedEvent(selectedComponent, oldSelected);
        }
      }
      componentList.remove(component);
      componentBox.remove(component.getComponent());
      fireRemovedEvent(component);
    }
  }

  public DraggableComponent getSelectedDraggableComponent() {
    return selectedComponent;
  }

  public int getDraggableComponentCount() {
    return componentBox.getComponentCount();
  }

  public DraggableComponent getDraggableComponentAt(int index) {
    return findDraggableComponent(componentBox.getComponent(index));
  }

  public static int getDraggableComponentIndex(DraggableComponent component) {
    return ComponentUtils.getComponentIndex(component.getComponent());
  }

  public boolean isScrollEnabled() {
    return scrollEnabled;
  }

  public void setScrollEnabled(boolean scrollEnabled) {
    if (scrollEnabled != this.scrollEnabled) {
      this.scrollEnabled = scrollEnabled;
      initialize();
    }
  }

  public int getScrollOffset() {
    return scrollOffset;
  }

  public void setScrollOffset(int scrollOffset) {
    if (scrollOffset != this.scrollOffset) {
      this.scrollOffset = scrollOffset;
      initialize();
    }
  }

  public int getComponentSpacing() {
    return getDirectionLayout().getComponentSpacing();
  }

  public void setComponentSpacing(int componentSpacing) {
    if (componentSpacing != getDirectionLayout().getComponentSpacing()) {
      getDirectionLayout().setComponentSpacing(componentSpacing);
      initialize();
    }
  }

  public boolean isEnsureSelectedVisible() {
    return ensureSelectedVisible;
  }

  public void setEnsureSelectedVisible(boolean ensureSelectedVisible) {
    this.ensureSelectedVisible = ensureSelectedVisible;
  }

  public Direction getScrollButtonDirection() {
    return scrollButtonDirection;
  }

  public void setScrollButtonDirection(Direction scrollButtonDirection) {
    if (scrollButtonDirection != this.scrollButtonDirection) {
      this.scrollButtonDirection = scrollButtonDirection;
      initialize();
    }
  }

  public boolean isAutoSelect() {
    return autoSelect;
  }

  public void setAutoSelect(boolean autoSelect) {
    this.autoSelect = autoSelect;
  }

  public Direction getComponentDirection() {
    return componentDirection;
  }

  public void setComponentDirection(Direction componentDirection) {
    if (componentDirection != this.componentDirection) {
      this.componentDirection = componentDirection;
      initialize();
    }
  }

  public void dragDraggableComponent(DraggableComponent component, Point p) {
    component.drag(SwingUtilities.convertPoint(this, p, component.getComponent()));
  }

  private void doSelectComponent(DraggableComponent component) {
    if (selectedComponent != null) {
      DraggableComponent oldSelected = selectedComponent;
      selectedComponent = component;
      ensureSelectedVisible();
      fireSelectedEvent(selectedComponent, oldSelected);
    } else {
      selectedComponent = component;
      ensureSelectedVisible();
      fireSelectedEvent(selectedComponent, null);
    }
  }

  private int findSelectableComponentIndex(int index) {
    int selectIndex = -1;
    int k;
    for (int i = 0; i < componentBox.getComponentCount(); i++) {
      if ((findDraggableComponent(componentBox.getComponent(i))).isEnabled() && i != index) {
        k = selectIndex;
        selectIndex = i;
        if (k < index && selectIndex > index)
          return selectIndex;
        else if (k > index && selectIndex > index)
          return k;
      }
    }

    return selectIndex;
  }

  private DraggableComponent findDraggableComponent(Component c) {
    for (int i = 0; i < componentList.size(); i++)
      if (((DraggableComponent)componentList.get(i)).getComponent() == c)
        return (DraggableComponent)componentList.get(i);

    return null;
  }

  private DirectionLayout getDirectionLayout() {
    return (DirectionLayout)componentBox.getLayout();
  }

  private void initialize() {
    if (componentContainer != null)
      remove(componentContainer);
    DirectionLayout layout = getDirectionLayout();
    layout.setDirection(componentDirection == Direction.UP ? Direction.RIGHT :
                        componentDirection == Direction.LEFT ? Direction.DOWN :
                        componentDirection == Direction.DOWN ? Direction.RIGHT :
                        Direction.DOWN);
    layout.setCompressing(!scrollEnabled);

    if (scrollEnabled)
      componentContainer = new ScrollableBox(componentBox,
                                             componentDirection.isHorizontal(),
                                             ComponentUtils.getBorderLayoutOrientation(scrollButtonDirection),
                                             scrollOffset);
    else
      componentContainer = componentBox;

    add(componentContainer, BorderLayout.CENTER);
  }

  private void ensureSelectedVisible() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (scrollEnabled && ensureSelectedVisible && selectedComponent != null)
          ((ScrollableBox)componentContainer).ensureVisible(ComponentUtils.getComponentIndex(selectedComponent.getComponent()));
      }
    });
  }

  private void fireDraggedEvent(DraggableComponentEvent e) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, e.getSource(), e,
                                                                        SwingUtilities.convertPoint(e.getSource().getComponent(), e.getPoint(), this));
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).componentDragged(event);
    }
  }

  private void fireDroppedEvent(DraggableComponentEvent e) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, e.getSource(), e,
                                                                        SwingUtilities.convertPoint(e.getSource().getComponent(), e.getPoint(), this));
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).componentDropped(event);
    }
  }

  private void fireNotDroppedEvent(DraggableComponentEvent e) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, e.getSource(), e);
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).componentDragAborted(event);
    }
  }

  private void fireSelectedEvent(DraggableComponent component, DraggableComponent oldDraggableComponent) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, component, oldDraggableComponent);
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).componentSelected(event);
    }
  }

  private void fireAddedEvent(DraggableComponent component) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, component);
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).componentAdded(event);
    }
  }

  private void fireRemovedEvent(DraggableComponent component) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, component);
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).componentRemoved(event);
    }
  }

  private void fireChangedEvent(DraggableComponentEvent e) {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this, e.getSource(), e);
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).changed(event);
    }
  }

  private void fireChangedEvent() {
    if (listeners != null) {
      DraggableComponentBoxEvent event = new DraggableComponentBoxEvent(this);
      Object l[] = listeners.toArray();
      for (int i = 0; i < l.length; i++)
        ((DraggableComponentBoxListener)l[i]).changed(event);
    }
  }
}