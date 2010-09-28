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


// $Id: SplitWindow.java,v 1.12 2004/08/11 13:47:58 jesper Exp $
package net.infonode.docking;

import net.infonode.docking.location.WindowLocation;
import net.infonode.docking.location.WindowSplitLocation;
import net.infonode.docking.properties.SplitWindowProperties;
import net.infonode.gui.SimpleSplitPane;
import net.infonode.properties.propertymap.PropertyMap;
import net.infonode.properties.propertymap.PropertyMapManager;
import net.infonode.util.Direction;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A window with a split pane that contains two child windows.
 *
 * @author $Author: jesper $
 * @version $Revision: 1.12 $
 */
public class SplitWindow extends DockingWindow {
  private SplitWindowProperties rootProperties = new SplitWindowProperties();
  private SplitWindowProperties splitWindowProperties = new SplitWindowProperties(rootProperties);
  private SimpleSplitPane splitPane;

  /**
   * Creates a split window.
   *
   * @param horizontal true if the split is horizontal
   */
  public SplitWindow(boolean horizontal) {
    splitPane = new SimpleSplitPane(horizontal);
    splitPane.setContinuousLayout(true);
    setComponent(splitPane);
    init();
  }

  /**
   * Creates a split window with with the given child windows.
   *
   * @param horizontal true if the split is horizontal
   * @param leftWindow the left/upper window
   * @param rightWindow the right/lower window
   */
  public SplitWindow(boolean horizontal, DockingWindow leftWindow, DockingWindow rightWindow) {
    this(horizontal);
    setWindows(leftWindow, rightWindow);
  }

  /**
   * Creates a split window with with the given child windows.
   *
   * @param horizontal true if the split is horizontal
   * @param dividerLocation the divider location, 0 - 1
   * @param leftWindow the left/upper window
   * @param rightWindow the right/lower window
   */
  public SplitWindow(boolean horizontal, float dividerLocation, DockingWindow leftWindow, DockingWindow rightWindow) {
    this(horizontal, leftWindow, rightWindow);
    setDividerLocation(dividerLocation);
  }

  /**
   * Returns the property values for this split window.
   *
   * @return the property values for this split window
   */
  public SplitWindowProperties getSplitWindowProperties() {
    return splitWindowProperties;
  }

  /**
   * Returns the left/upper child window.
   *
   * @return the left/upper child window
   */
  public DockingWindow getLeftWindow() {
    return (DockingWindow) splitPane.getLeftComponent();
  }

  /**
   * Returns the right/lower child window.
   *
   * @return the right/lower child window
   */
  public DockingWindow getRightWindow() {
    return (DockingWindow) splitPane.getRightComponent();
  }

  /**
   * Sets the divider location as a fraction of this split window's size.
   *
   * @param dividerLocation the divider location as a fraction of this split window's size
   */
  public void setDividerLocation(float dividerLocation) {
    splitPane.setDividerLocation(dividerLocation);
  }

  /**
   * Returns the divider location as a fraction of this split window's size.
   * @return the divider location as a fraction of this split window's size
   */
  public float getDividerLocation() {
    return splitPane.getDividerLocation();
  }

  /**
   * Sets the child windows of this split window.
   *
   * @param leftWindow the left/upper child window
   * @param rightWindow the right/lower child window
   */
  public void setWindows(DockingWindow leftWindow, DockingWindow rightWindow) {
    splitPane.setLeftComponent(addWindow(leftWindow));
    splitPane.setRightComponent(addWindow(rightWindow));
    fireTitleChanged();
//    leftWindow.setVisible(true);
//    rightWindow.setVisible(true);
  }

  protected void update() {
    splitPane.setDividerSize(splitWindowProperties.getDividerSize());
  }

  protected void optimizeWindowLayout() {
    DockingWindow parent = getWindowParent();

    if (parent != null && (getRightWindow() == null || getLeftWindow() == null)) {
      DockingWindow w = getRightWindow() == null ? getLeftWindow() : getRightWindow();
      parent.replaceChildWindow(this, w);
    }
  }

  protected WindowLocation getWindowLocation(DockingWindow window) {
    if (getLeftWindow() == null || getRightWindow() == null)
      return getWindowLocation();

    boolean left = window == getLeftWindow();
    return new WindowSplitLocation((left ? getRightWindow() : getLeftWindow()).getLocationWindow(),
                                   getWindowLocation(),
                                   left ? (splitPane.isHorizontal() ? Direction.LEFT : Direction.UP) :
                                   (splitPane.isHorizontal() ? Direction.RIGHT : Direction.DOWN),
                                   getDividerLocation());
  }

  public DockingWindow getChildWindow(int index) {
    return getWindows()[index];
  }

  private DockingWindow[] getWindows() {
    return getLeftWindow() == null ? getRightWindow() == null ? new DockingWindow[0] : new DockingWindow[]{getRightWindow()} :
        getRightWindow() == null ? new DockingWindow[]{getLeftWindow()} : new DockingWindow[]{getLeftWindow(), getRightWindow()};
  }

  public int getChildWindowCount() {
    return getWindows().length;
  }

  public Icon getIcon() {
    return getLeftWindow() == null ? (getRightWindow() == null ? null : getRightWindow().getIcon()) : getLeftWindow().getIcon();
  }

  protected void doReplace(DockingWindow oldWindow, DockingWindow newWindow) {
    if (getLeftWindow() == oldWindow) {
      splitPane.setLeftComponent(newWindow);
    }
    else {
      splitPane.setRightComponent(newWindow);
    }

    validate();
  }

  protected void doRemoveWindow(DockingWindow window) {
    if (window == getLeftWindow()) {
      splitPane.setLeftComponent(null);
    }
    else {
      splitPane.setRightComponent(null);
    }
  }

  protected void write(ObjectOutputStream out, WriteContext context) throws IOException {
    out.writeInt(WindowIds.SPLIT);
    out.writeBoolean(splitPane.isHorizontal());
    out.writeFloat(getDividerLocation());
    getLeftWindow().write(out, context);
    getRightWindow().write(out, context);
    super.write(out, context);
  }

  protected DockingWindow read(ObjectInputStream in, ReadContext context) throws IOException {
    splitPane.setHorizontal(in.readBoolean());
    splitPane.setDividerLocation(in.readFloat());
    DockingWindow leftWindow = WindowDecoder.decodeWindow(in, context);
    DockingWindow rightWindow = WindowDecoder.decodeWindow(in, context);
    super.read(in, context);
    
    if (leftWindow != null && rightWindow != null) {
      setWindows(leftWindow, rightWindow);
      return this;
    }
    else
      return leftWindow != null ? leftWindow : rightWindow != null ? rightWindow : null;
  }

  protected boolean acceptsCenterDrop() {
    return getRootWindow().getRootWindowProperties().getRecursiveTabsEnabled();
  }

  protected void rootChanged(final RootWindow oldRoot, final RootWindow newRoot) {
    super.rootChanged(oldRoot, newRoot);
    PropertyMapManager.runBatch(new Runnable() {
      public void run() {
        if (oldRoot != null)
          rootProperties.getMap().removeSuperMap();

        if (newRoot != null) {
          rootProperties.addSuperObject(newRoot.getRootWindowProperties().getSplitWindowProperties());
        }
      }
    });
  }

  protected PropertyMap getPropertyObject() {
    return splitWindowProperties.getMap();
  }

  protected PropertyMap createPropertyObject() {
    return new SplitWindowProperties().getMap();
  }
}