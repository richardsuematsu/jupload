//
// $Id: CustomizedNbFilesPerRequestUploadPolicy.java 441 2008-04-16 07:58:02Z
// etienne_sf $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-05-06
// Creator: etienne_sf
// Last modified: $Date: 2009-05-15 00:15:38 +0200 (ven., 15 mai 2009) $
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
 * These is a now deprecated specialization of
 * {@link wjhk.jupload2.policies.DefaultUploadPolicy}. The DefaultUploadPolicy
 * now reads itself the nbFilesPerRequest applet parameter. <BR>
 * 
 * @author etienne_sf
 * @version $Revision: 755 $
 * @see #CustomizedNbFilesPerRequestUploadPolicy(JUploadContext)
 * @deprecated This class is of no use, as it actually behaves exactly as the
 *             {@link wjhk.jupload2.policies.DefaultUploadPolicy}.
 */
@Deprecated
public class CustomizedNbFilesPerRequestUploadPolicy extends
        DefaultUploadPolicy {

    /**
     * @param theContext The applet to whom the UploadPolicy must apply.
     * @throws JUploadException
     * @see UploadPolicy
     */
    public CustomizedNbFilesPerRequestUploadPolicy(JUploadContext theContext)
            throws JUploadException {
        super(theContext);
    }

}
