//
// $Id: FileData.java 95 2007-05-02 03:27:05Z
// /C=DE/ST=Baden-Wuerttemberg/O=ISDN4Linux/OU=Fritz
// Elfert/CN=svn-felfert@isdn4linux.de/emailAddress=fritz@fritz-elfert.de $
//
// jupload - A file upload applet.
// Copyright 2007 The JUpload Team
//
// Created: 2006-11-20
// Creator: etienne_sf
// Last modified: $Date: 2010-06-28 13:35:55 +0200 (lun., 28 juin 2010) $
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

package wjhk.jupload2.filedata;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.policies.UploadPolicy;
import wjhk.jupload2.upload.FileUploadThread;
import wjhk.jupload2.upload.helper.ByteArrayEncoder;

/**
 * This class contains all data and methods for a file to upload. The current
 * {@link wjhk.jupload2.policies.UploadPolicy} contains the necessary parameters
 * to personalize the way files must be handled. <BR>
 * The JUpload package provides a default implementation of this class in
 * {@link DefaultFileData}. This default implementation contains all necessary
 * methods to allow upload. You can override it to add new file behaviour. For
 * instance, you could add a XMLFileData, that would check that XML is valid
 * before upload. See the <a href="package-summary.html">package summary</a> for
 * more details about that. <BR>
 * This class is the interface that all FileData must implement. The
 * {@link DefaultFileData} class contains the default implementation for this
 * interface. The {@link PictureFileData} contains another implementation of
 * this interface, adapted to manage pictures (rotation, resizing...). <BR>
 * The instance of FileData is created by the
 * {@link UploadPolicy#createFileData(File, File)} method. This method can be
 * overrided in a new upoad policy, to create an instance of another FileData.
 * See {@link PictureFileData} for an example about FileData customization.
 * 
 * @author etienne_sf
 */

public interface FileData {

	/**
	 * Called during the upload, by the {@link FileUploadThread}. The FileData
	 * instance should then call the
	 * {@link ByteArrayEncoder#appendTextProperty(String, String, int)} method
	 * to add each file property to the current upload.
	 * 
	 * @param bae
	 *            The byte encoder, where the properties must be added
	 * @param index
	 *            Index of the file concerned by this value. -1 if this is a
	 *            global parameter.
	 * @throws JUploadIOException
	 *             Encapsulation of the IOException, if any would occurs.
	 * @see ByteArrayEncoder#appendTextProperty(String, String, int)
	 */
	public void appendFileProperties(ByteArrayEncoder bae, int index)
			throws JUploadIOException;

	/**
	 * Prepare the fileData to upload. For instance, picture data can be resized
	 * before upload (see {@link PictureFileData}. This method is called before
	 * the upload of this file. If no exception is thrown, then the file is
	 * correctly prepared.
	 * 
	 * @see FileUploadThread
	 * @throws JUploadException
	 *             Encapsulation of the Exception, if any error would occurs.
	 */
	public void beforeUpload() throws JUploadException;

	/**
	 * Get size of upload, which may be different from the actual file length.
	 * This call is valid only after a call to {@link #beforeUpload()} and
	 * before the call to {@link #afterUpload()}.
	 * 
	 * @return The length of upload. In this class, this is the size of the
	 *         file, as it isn't transformed for upload. This size may change if
	 *         encoding is necessary (needs a new FileData class), or if picture
	 *         is to be resized or rotated.
	 * @see PictureFileData
	 */
	public long getUploadLength();

	/**
	 * This function is called after upload, whether it is successful or not. It
	 * allows fileData to free any resource created for the upload. For
	 * instance, {@link PictureFileData#afterUpload()} removes the temporary
	 * file, if any was created.
	 */
	public void afterUpload();

	/**
	 * This function creates an InputStream from this file. The
	 * {@link FileUploadThread} class then reads bytes from it and transfers
	 * them to the webserver. The caller is responsible for closing this stream.<BR>
	 * This method may only be called when {@link #isPreparedForUpload()}
	 * returns true.
	 * 
	 * @throws JUploadException
	 *             Encapsulation of the Exception, if any would occurs.
	 * @throws IllegalStateException
	 *             When the upload is not prepared (before a call to
	 *             {@link #beforeUpload()} or after a call to
	 *             {@link #afterUpload()}
	 * @return An InputStream, representing this instance.
	 */
	public InputStream getInputStream() throws JUploadException;

	/**
	 * Get the original filename. This is the name of the file, into the local
	 * hardrive
	 * 
	 * @return The original filename
	 */
	public String getFileName();

	/**
	 * @return The extension for the original file.
	 */
	public String getFileExtension();

	/**
	 * @return The length of the original file.
	 */
	public long getFileLength();

	/**
	 * @return The original file date.
	 */
	public Date getLastModified();

	/**
	 * Get the directory of the file.
	 * 
	 * @return The directory where this file is stored.
	 */
	public String getDirectory();

	/**
	 * Retrieves the MD5 sum of the file.<BR>
	 * Since 5.0.0, this method is is in DefaultFileData, and is calculated
	 * depending on the sendMD5Sum applet parameter, during the file preparation
	 * file.
	 * 
	 * @return The corresponding MD5 sum.
	 * @throws JUploadException 
	 * @see #beforeUpload()
	 */
	public String getMD5() throws JUploadException;

	/**
	 * This function return the FileData content type.
	 * 
	 * @return The mimeType for the file.
	 */
	public String getMimeType();

	/**
	 * Indicate if this file can be read. Take care of the File.canRead()
	 * methods, that seems to be wrong from time to time.
	 * 
	 * @return indicates whether the file can be read or not.
	 */
	public boolean canRead();

	/**
	 * Standard getter, for the file described by the FileData instance.
	 * 
	 * @return the File instance associated with this row.
	 */
	public File getFile();

	/**
	 * Retrieves the path of this file relative to it's root dir
	 * 
	 * @return This instance's relative path or an empty string if it was not
	 *         created using a root parameter.
	 */
	public String getRelativeDir();

	/**
	 * Indicates whether the file can be uploaded or not. This boolean should be
	 * set to true in the call to {@link #beforeUpload()}, and the to false in
	 * the call to {@link #afterUpload()}.
	 * 
	 * @return True if the file is ready for upload.
	 * @throws IllegalStateException
	 *             When the upload is not prepared (before a call to
	 *             {@link #beforeUpload()} or after a call to
	 *             {@link #afterUpload()}
	 */
	public boolean isPreparedForUpload();
}
