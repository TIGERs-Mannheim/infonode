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


// $Id: Map.java,v 1.3 2004/07/06 15:08:44 jesper Exp $
package net.infonode.util.collection.map.base;

import net.infonode.util.collection.Collection;

/**
 * A map.
 *
 * @author $Author: jesper $
 * @version $Revision: 1.3 $
 */
public interface Map extends ConstMap, Collection {
  /**
   * Associate a key with a value.
   * This will overwrite any existing association.
   *
   * @param key the key
   * @param value the value
   * @return the old value associated with this key, null if no value existed
   */
  Object put(Object key, Object value);

  /**
   * Removes a key and it's value.
   *
   * @param key the key
   * @return the value associated with the key, null if no value existed
   */
  Object remove(Object key);

  /**
   * Returns an iterator for this map.
   *
   * @return an iterator for this map
   */
  MapIterator iterator();
}