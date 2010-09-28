/*
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


// $Id: ComponentUtil.java,v 1.7 2004/09/22 14:35:04 jesper Exp $
package net.infonode.gui;

import net.infonode.util.Direction;

import javax.swing.*;
import java.awt.*;

public class ComponentUtil {
  private ComponentUtil() {
  }

  public static final int getComponentIndex(Component component) {
    if (component != null && component.getParent() != null) {
      Component[] c = component.getParent().getComponents();
      for (int i = 0; i < c.length; i++) {
        if (c[i] == component)
          return i;
      }
    }

    return -1;
  }

  public static final String getBorderLayoutOrientation(Direction d) {
    return d == Direction.UP ? BorderLayout.NORTH :
           d == Direction.LEFT ? BorderLayout.WEST :
           d == Direction.DOWN ? BorderLayout.SOUTH :
           BorderLayout.EAST;
  }

  public static Color getBackgroundColor(Component component) {
    return component == null ? null : component.isOpaque() ? component.getBackground() : getBackgroundColor(component.getParent());
  }

  public static int countComponents(Container c) {
    int num = 1;
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component comp = c.getComponent(i);
      if (comp instanceof Container)
        num += countComponents((Container) comp);
      else
        num++;
    }

    return num;
  }

  public static int getVisibleChildrenCount(Component c) {
    if (c == null || !(c instanceof Container))
      return 0;

    int count = 0;
    Component[] comp = ((Container) c).getComponents();

    for (int i = 0; i < comp.length; i++)
      if (comp[i].isVisible())
        count++;

    return count;
  }

  public static boolean hasVisibleChildren(Component c) {
    return getVisibleChildrenCount(c) > 0;
  }

  public static boolean isOnlyVisibleComponent(Component c) {
    return c != null && c.isVisible() && getVisibleChildrenCount(c.getParent()) == 1;
  }

  public static boolean isOnlyVisibleComponents(Component[] c) {
    if (c != null && c.length > 0) {
      boolean visible = getVisibleChildrenCount(c[0].getParent()) == c.length;
      if (visible)
        for (int i = 0; i < c.length; i++)
          visible = visible && c[i].isVisible();
      return visible;
    }
    return false;
  }

  public static Component findFirstComponentOfType(Component comp, Class c) {
    if (c.isInstance(comp))
      return comp;

    if (comp instanceof Container) {
      Component[] children = ((Container) comp).getComponents();
      for (int i = 0; i < children.length; i++) {
        Component comp2 = findFirstComponentOfType(children[i], c);
        if (comp2 != null)
          return comp2;
      }
    }
    return null;
  }

  public static boolean isFocusable(Component c) {
    return c.isFocusable() && c.isDisplayable() && c.isVisible() && c.isEnabled();
  }

  /**
   * Requests focus unless the component already has focus. For some weird reason calling
   * {@link Component#requestFocusInWindow()} when the component is focus owner changes focus owner to another
   * component!
   *
   * @param component the component to request focus for
   * @return true if the component has focus or probably will get focus, otherwise false
   */
  public static boolean requestFocus(Component component) {
/*    System.out.println("Owner: " + System.identityHashCode(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner()) +
                       ", " + System.identityHashCode(component) + ", " +
                       (KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == component));*/
    return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == component || component.requestFocusInWindow();
  }

  /**
   * Requests focus for a component. If that's not possible it's {@link FocusTraversalPolicy} is checked. If that
   * doesn't work all it's children is recursively checked with this method.
   *
   * @param component the component to request focus for
   * @return the component which has focus or probably will obtain focus, null if no component will receive focus
   */
  public static Component smartRequestFocus(Component component) {
    if (requestFocus(component))
      return component;

    if (component instanceof JComponent) {
      FocusTraversalPolicy policy = ((JComponent) component).getFocusTraversalPolicy();

      if (policy != null) {
        Component focusComponent = policy.getDefaultComponent((Container) component);

        if (focusComponent != null && requestFocus(focusComponent)) {
          return focusComponent;
        }
      }
    }

    if (component instanceof Container) {
      Component[] children = ((Container) component).getComponents();

      for (int i = 0; i < children.length; i++) {
        component = smartRequestFocus(children[i]);

        if (component != null)
          return component;
      }
    }

    return null;
  }

  /**
   * Calculates preferred max height for the given components without checking isVisible.
   *
   * @param components Components to check
   * @return max height
   */
  public static int getPreferredMaxHeight(Component[] components) {
    int height = 0;
    for (int i = 0; i < components.length; i++) {
      int k = (int) components[i].getPreferredSize().getHeight();
      if (k > height)
        height = k;
    }
    return height;
  }

  /**
   * Calculates preferred max width for the given components without checking isVisible.
   *
   * @param components Components to check
   * @return max width
   */
  public static int getPreferredMaxWidth(Component[] components) {
    int width = 0;
    for (int i = 0; i < components.length; i++) {
      int k = (int) components[i].getPreferredSize().getWidth();
      if (k > width)
        width = k;
    }
    return width;
  }
}