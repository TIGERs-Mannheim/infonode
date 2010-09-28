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


// $Id: WindowBar.java,v 1.24 2004/08/11 13:47:58 jesper Exp $
package net.infonode.docking;

import net.infonode.docking.properties.WindowBarProperties;
import net.infonode.docking.util.DockingUtil;
import net.infonode.gui.panel.ResizablePanel;
import net.infonode.properties.propertymap.PropertyMap;
import net.infonode.tabbedpanel.*;
import net.infonode.util.Direction;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

/**
 * A window bar is located at the edge of a root window.
 * It's a tabbed panel where the content panel is dynamically shown and hidden.
 * A window bar is enabled and disabled using the {[@link #setEnabled} method.
 *
 * @author $Author: jesper $
 * @version $Revision: 1.24 $
 */
public class WindowBar extends AbstractTabWindow {
  private RootWindow rootWindow;
  private Direction direction;
  private WindowBarProperties windowBarProperties;
  private TabbedPanelContentPanel contentPanel;
  private HashMap tabListeners = new HashMap(4);
  private ResizablePanel edgePanel;

  WindowBar(RootWindow rootWindow, Direction _direction) {
    super(false);

    this.rootWindow = rootWindow;
    contentPanel = new TabbedPanelContentPanel(getTabbedPanel(), new TabContentPanel(getTabbedPanel()));

    this.direction = _direction;

    {
      WindowBarProperties properties = new WindowBarProperties();
      properties.getTabWindowProperties().addSuperObject(rootWindow.getRootWindowProperties().getTabWindowProperties());
      this.windowBarProperties = new WindowBarProperties(properties);
      this.windowBarProperties.addSuperObject(WindowBarProperties.createDefault(direction));
    }

    getTabbedPanel().addTabListener(new TabAdapter() {
      public void tabAdded(TabEvent event) {
        final Tab tab = event.getTab();

        MouseListener listener = new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2 && getRootWindow() != null && getRootWindow().getRootWindowProperties().getDoubleClickRestoresWindow())
              ((WindowTab) tab).getWindow().restore();
          }
        };

        tabListeners.put(tab, listener);
        tab.addMouseListener(listener);
      }

      public void tabRemoved(TabRemovedEvent event) {
        event.getTab().removeMouseListener((MouseListener) tabListeners.remove(event.getTab()));
      }
    });

    edgePanel = new ResizablePanel(direction.getOpposite());
    edgePanel.setPreferredSize(new Dimension(200, 200));
    edgePanel.setVisible(false);
    edgePanel.setComponent(contentPanel);

    setTabWindowProperties(windowBarProperties.getTabWindowProperties());
    init();
  }

  /**
   * Returns the property values for this window bar.
   *
   * @return the property values for this window bar
   */
  public WindowBarProperties getWindowBarProperties() {
    return windowBarProperties;
  }

  /**
   * Sets the size of the content panel.
   * If the window bar is located on the left or right side, the panel width is set otherwise the panel height.
   *
   * @param size
   */
  public void setContentPanelSize(int size) {
    edgePanel.setPreferredSize(direction.isHorizontal() ? new Dimension(size, 0) : new Dimension(0, size));
  }

  /**
   * Returns the size of the content panel.
   * If the window bar is located on the left or right side, the panel width is returned otherwise the panel height.
   *
   * @return the size of the content panel
   */
  public int getContentPanelSize() {
    Dimension d = edgePanel.getPreferredSize();
    return direction.isHorizontal() ? d.width : d.height;
  }

  public RootWindow getRootWindow() {
    return rootWindow;
  }

  void childGainedFocus(DockingWindow child, View view) {
    super.childGainedFocus(child, view);
    int index = getChildWindowIndex(child);

    if (index != -1)
      setSelectedTab(index);
  }

  ResizablePanel getEdgePanel() {
    return edgePanel;
  }

  protected void update() {
    edgePanel.setResizeWidth(windowBarProperties.getContentPanelEdgeResizeDistance());
    windowBarProperties.getComponentProperties().applyTo(this);
  }

  public Dimension getPreferredSize() {
    if (isEnabled()) {
      Dimension d = super.getPreferredSize();
      int minWidth = windowBarProperties.getMinimumWidth();
      return new Dimension(Math.max(minWidth, d.width), Math.max(minWidth, d.height));
    }
    else
      return super.getPreferredSize();
  }

  protected void tabSelected(final WindowTab tab) {
/*    getRootWindow().ignoreFocusChanges(new Runnable() {
      public void run() {
        System.out.println("Select start");
        FocusUtil.blockFocusChanges();

        try {
          edgePanel.setVisible(tab != null);
        }
        finally {
          FocusUtil.unblockFocusChanges();
          System.out.println("Select end");
        }
      }
    });
*/
    edgePanel.setVisible(tab != null);
    super.tabSelected(tab);
  }

  protected void clearFocus(View view) {
    super.clearFocus(view);

    if (view != null && !DockingUtil.isAncestor(this, view)) {
      getTabbedPanel().setSelectedTab(null);
    }
  }

  public boolean isMinimized() {
    return true;
  }

  protected boolean isSplittable() {
    return false;
  }

  DockingWindow acceptDrop(Point p, DockingWindow window) {
    return isEnabled() ? super.acceptDrop(p, window) : null;
  }

  protected PropertyMap getPropertyObject() {
    return windowBarProperties.getMap();
  }

  protected PropertyMap createPropertyObject() {
    return new WindowBarProperties().getMap();
  }

  protected void write(ObjectOutputStream out, WriteContext context) throws IOException {
    super.write(out, context);
    out.writeInt(getContentPanelSize());
    out.writeBoolean(isEnabled());
  }

  protected DockingWindow read(ObjectInputStream in, ReadContext context) throws IOException {
    super.read(in, context);
    setContentPanelSize(in.readInt());
    setEnabled(in.readBoolean());
    return this;
  }
}