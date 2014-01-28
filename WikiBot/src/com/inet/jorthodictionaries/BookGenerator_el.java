/*
 *  JOrtho
 *
 *  Copyright (C) 2005-2013 by i-net software
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 *  
 * Created on 13.12.2007
 */
package com.inet.jorthodictionaries;

/**
 * A book generator for Greek.
 *
 * @author Volker Berlin
 */
public class BookGenerator_el extends BookGenerator {

    /* (non-Javadoc)
     * @see com.inet.jorthodictionaries.BookGenerator#isValidLanguage(java.lang.String, java.lang.String)
     */
    @Override
    boolean isValidLanguage( String word, String wikiText ) {
        if( wikiText.indexOf( "{{-el-}}" ) < 0 ) {
            return false;
        }

        return true;
    }

}