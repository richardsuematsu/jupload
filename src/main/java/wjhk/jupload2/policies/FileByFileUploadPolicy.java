//
// $Id: FileByFileUploadPolicy.java 822 2009-07-02 14:49:12Z etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-05-06
// Creator: etienne_sf
// Last modified: $Date: 2009-07-02 16:49:12 +0200 (jeu., 02 juil. 2009) $
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

package wjhk.jupload2.policies;

import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.exception.JUploadException;

/**
 * Specialization of
 * {@link wjhk.jupload2.policies.CustomizedNbFilesPerRequestUploadPolicy}, where
 * each upload HTTP request contains only one file. <BR>
 * <BR>
 * This policy :
 * <UL>
 * <LI>Upload files without transformation
 * <LI>File by file (uploading 5 files needs 5 HTTP request toward the server)
 * <UL>
 * <BR>
 * <BR>
 * The same behavior can be obtained by specifying no UploadPolicy (or
 * {@link FileByFileUploadPolicy}), and give the nbFilesPerRequest (with a value
 * set to 1) parameter.
 * 
 * @author etienne_sf
 * @version $Revision: 822 $
 * @deprecated You can use the applet, without the uploadPolicy applet
 *             parameter, and put the nbFilesPerRequest to 1. It does the
 *             same...
 * 
 */
@Deprecated
public class FileByFileUploadPolicy extends DefaultUploadPolicy {

    /**
     * @param juploadContext The applet on which the UploadPolicy should apply.
     * @throws JUploadException
     */
    public FileByFileUploadPolicy(JUploadContext juploadContext)
            throws JUploadException {
        super(juploadContext);

        setNbFilesPerRequest(1);
    }

}
