package org.wikipedia.test;

import java.io.IOException;

import org.wikipedia.WMFWiki;
import org.wikipedia.Wiki;

/**
 * The Class OtherTests.
 *
 * @(#)OtherTests.java
 * Copyright (C) 2011 MER-C
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */


/**
 *  Miscellaneous tests.
 *  @author MER-C
 */
public class OtherTests
{

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws java.io.IOException
    {
        // WMFWiki.getSiteMatrix()
        for (Wiki x : WMFWiki.getSiteMatrix())
            System.out.println(x.getDomain());
    }
}
