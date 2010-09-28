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


//$Id: CloseIcon.java,v 1.4 2004/08/12 07:27:42 jesper Exp $
package net.infonode.gui.icon.button;

import java.awt.*;

/**
 *
 *
 * @author $Author: jesper $
 * @version $Revision: 1.4 $
 */
public class CloseIcon extends AbstractButtonIcon {
  public CloseIcon() {
    super();
  }

  public CloseIcon(Color c) {
    super(c);
  }

  public CloseIcon(Color c, int size) {
    super(c, size);
  }

  public CloseIcon(int size) {
    super(size);
  }

  protected void paintIcon(Component c, Graphics g, int x1, int y1, int x2, int y2) {
    g.drawLine(x1, y1 + 1, x2 - 1, y2);
    g.drawLine(x1 + 1, y1 + 1, x2, y2);

    g.drawLine(x1 + 1, y2, x2, y1 + 1);
    g.drawLine(x1, y2, x2 - 1, y1 + 1);

  }
}