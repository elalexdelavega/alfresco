/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.app.context;

/**
 * Interface used to allow Beans to register themselves as interested in UI context events.
 * <p>
 * Beans supporting this interface should be register against the UIContextService. Then Beans
 * which wish to indicate that the UI should refresh itself i.e. dump all cached data and settings,
 * call the UIContextService.notifyBeans() to inform all registered instances of the change. 
 * 
 * @author Kevin Roast
 */
public interface IContextListener
{
   /**
    * Method called by UIContextService.notifyBeans() to inform all registered beans that
    * all UI Beans should refresh dump all cached data and settings. 
    */
   public void contextUpdated();
}
