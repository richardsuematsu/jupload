//
// $Id: DateRenderer.java 95 2007-05-02 03:27:05Z
// /C=DE/ST=Baden-Wuerttemberg/O=ISDN4Linux/OU=Fritz
// Elfert/CN=svn-felfert@isdn4linux.de/emailAddress=fritz@fritz-elfert.de $
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

package wjhk.jupload2.gui.filepanel;

import java.awt.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import wjhk.jupload2.policies.UploadPolicy;

/**
 * Technical class, used to display dates. Used in
 * {@link wjhk.jupload2.gui.filepanel.FilePanelJTable}.
 */
public class DateRenderer extends DefaultTableCellRenderer {
    /** A generated serialVersionUID, to avoid warning during compilation */
    private static final long serialVersionUID = -7171473761133675782L;

    private SimpleDateFormat df;

    /**
     * Creates a new instance.
     * 
     * @param uploadPolicy The policy to be used for providing the translated
     *            format string.
     */
    public DateRenderer(UploadPolicy uploadPolicy) {
        super();
        this.df = new SimpleDateFormat(uploadPolicy
                .getLocalizedString("dateformat"));
    }

    /**
     * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
     *      java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);

        if (value instanceof Date)
            setValue(this.df.format(value));
        super.setHorizontalAlignment(RIGHT);
        return cell;
    }
}
