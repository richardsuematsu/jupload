//
// $Id: JUploadIOException.java 887 2009-11-06 21:07:21Z etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-11-20
// Creator: etienne_sf
// Last modified: $Date: 2009-11-06 22:07:21 +0100 (ven., 06 nov. 2009) $
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; either version 2 of the License, or (at your option) any later
// version. This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details. You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software Foundation, Inc.,
// 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.exception;

/**
 * This class should be used for all implementations of FileData or UploadPolicy
 * that want to throw an IO exception, and need to be conform with the interface
 * definition.
 * 
 * @author etienne_sf
 * @version $Revision: 887 $
 */

public class JUploadIOException extends JUploadException {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = 4202340617039827612L;

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param msg The message for this instance.
     */
    public JUploadIOException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new exception with the specified cause.
     * 
     * @param cause The cause for this instance.
     */
    public JUploadIOException(Exception cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param msg The message for this instance.
     * @param cause The cause for this instance.
     */
    public JUploadIOException(String msg, Exception cause) {
        super(msg, cause);
    }

}
