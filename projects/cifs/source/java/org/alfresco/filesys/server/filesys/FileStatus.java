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
package org.alfresco.filesys.server.filesys;

/**
 * File Status Class
 */
public class FileStatus
{

    // File status constants

    public final static int Unknown = -1;
    public final static int NotExist = 0;
    public final static int FileExists = 1;
    public final static int DirectoryExists = 2;

    /**
     * Return the file status as a string
     * 
     * @param sts int
     * @return String
     */
    public final static String asString(int sts)
    {

        // Convert the status to a string

        String ret = "";

        switch (sts)
        {
        case Unknown:
            ret = "Unknown";
            break;
        case NotExist:
            ret = "NotExist";
            break;
        case FileExists:
            ret = "FileExists";
            break;
        case DirectoryExists:
            ret = "DirExists";
            break;
        }

        return ret;
    }
}
