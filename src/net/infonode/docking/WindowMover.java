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


// $Id: WindowMover.java,v 1.4 2004/06/24 13:24:08 jesper Exp $
package net.infonode.docking;

import net.infonode.gui.draggable.DraggableComponent;
import net.infonode.gui.draggable.DraggableComponentAdapter;
import net.infonode.gui.draggable.DraggableComponentEvent;

import javax.swing.*;
import java.awt.*;

/**
 * @author $Author: jesper $
 * @version $Revision: 1.4 $
 */
class WindowMover {
  private DockingWindow window;
  private DraggableComponent draggableComponent;
  private WindowDragger dragger;

  WindowMover(DockingWindow _window, final WindowProvider windowProvider) {
    this.window = _window;
    draggableComponent = new DraggableComponent(window);
    draggableComponent.setReorderEnabled(false);
    draggableComponent.setEnableInsideDrag(true);

    draggableComponent.addListener(new DraggableComponentAdapter() {
      public void dragAborted(DraggableComponentEvent event) {
        abort();
      }

      public void dragged(DraggableComponentEvent event) {
        if (dragger == null) {
          DockingWindow w = windowProvider.getWindow(event.getPoint());

          if (w == null)
            return;

          dragger = new WindowDragger(w);
        }

        dragger.dragTo(convertPoint(event));
      }

      public void dropped(DraggableComponentEvent event) {
        if (dragger != null) {
          dragger.drop(convertPoint(event));
          dragger = null;
        }
      }
    });
  }

  void setAbortDragKey(int key) {
    draggableComponent.setAbortDragKeyCode(key);
  }

  private void abort() {
    if (dragger != null)
      dragger.abort();

    dragger = null;
  }

  private Point convertPoint(DraggableComponentEvent event) {
    return SwingUtilities.convertPoint(window, event.getPoint(), window.getRootWindow());
  }

}