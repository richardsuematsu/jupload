//
// $Id: JUploadExceptionTooBigFile.java 918 2010-01-08 22:21:44Z etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: ?
// Creator: William JinHua Kwong
// Last modified: $Date: 2010-01-08 23:21:44 +0100 (ven., 08 janv. 2010) $
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

import wjhk.jupload2.gui.filepanel.SizeRenderer;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * This exception indicates, that the file that is to be uploaded is too big.
 * Note: the file to upload may be smaller than the file selected by the user.
 * For instance, a picture may be reduced before upload.
 */
public class JUploadExceptionTooBigFile extends JUploadException {

    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = 4842380093113396023L;

    /**
     * Creates a new instance.
     * 
     * @param filename The filename for the file in error
     * @param uploadLength The length of this file
     * @param uploadPolicy The current upload policy.
     */
    public JUploadExceptionTooBigFile(String filename, long uploadLength,
            UploadPolicy uploadPolicy) {
        super(uploadPolicy.getLocalizedString("errFileTooBig", filename,
                SizeRenderer.formatFileSize(uploadLength, uploadPolicy)));
    }
}
