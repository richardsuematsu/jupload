//
// $Id$
//
// jupload - A file upload applet.
//
// Copyright 2008 The JUpload Team
//
// Created: 12 feb. 08
// Creator: etienne_sf
// Last modified: $Date$
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

package wjhk.jupload2.filedata.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.filedata.DefaultFileData;
import wjhk.jupload2.filedata.PictureFileData;
import wjhk.jupload2.policies.PictureUploadPolicy;

/**
 * 
 * This package provides low level methods, for picture management. It is used
 * by {@link PictureFileData} to simplify its processing.
 * 
 * @author etienne_sf
 * 
 */
public class ImageReaderWriterHelper {

    /**
     * File input, from which the original picture should be read.
     */
    FileImageInputStream fileImageInputStream = null;

    /**
     * File output stream for the current transformation.
     */
    FileImageOutputStream fileImageOutputStream;

    /**
     * The {@link PictureFileData} that this helper will have to help.
     */
    PictureFileData pictureFileData;

    /**
     * Current ImageReader. Initialized by {@link #initImageReader()}
     */
    ImageReader imageReader = null;

    /**
     * Current ImageWriter. Initialized by {@link #initImageWriter()}
     */
    ImageWriter imageWriter = null;

    /**
     * Current ImageWriter. Initialized by {@link #initImageWriter()}
     */
    ImageWriteParam imageWriterParam = null;

    /**
     * Contains the target picture format (in lowercase): gif, jpg... It is used
     * to find an ImageWriter, and to define the target filename.
     */
    String targetPictureFormat;

    /**
     * The current upload policy must be a {@link PictureUploadPolicy}
     */
    PictureUploadPolicy uploadPolicy;

    // ////////////////////////////////////////////////////////////////////
    // //////////////////// CONSTRUCTOR
    // ////////////////////////////////////////////////////////////////////

    /**
     * Standard constructor.
     * 
     * @param uploadPolicy The current upload policy.
     * @param pictureFileData The file data to be 'helped'.
     */
    public ImageReaderWriterHelper(PictureUploadPolicy uploadPolicy,
            PictureFileData pictureFileData) {
        this.uploadPolicy = uploadPolicy;
        this.pictureFileData = pictureFileData;

        this.targetPictureFormat = uploadPolicy.getImageFileConversionInfo()
                .getTargetFormat(pictureFileData.getFileExtension());
    }

    // ////////////////////////////////////////////////////////////////////
    // //////////////////// PUBLIC METHODS
    // ////////////////////////////////////////////////////////////////////

    /**
     * returns the target picture format (lowercase, may be the same as the file
     * extension)
     * 
     * @return the target picture format (lowercase, may be the same as the file
     *         extension)
     */
    public String getTargetPictureFormat() {
        return this.targetPictureFormat;
    }

    /**
     * Creates a FileImageOutputStream, and assign it as the output to the
     * imageWriter.
     * 
     * @param file The file where the output stream must write.
     * @throws JUploadIOException Any error...
     */
    public void setOutput(File file) throws JUploadIOException {
        // We first initialize internal variable.
        initImageWriter();

        try {
            this.fileImageOutputStream = new FileImageOutputStream(file);
        } catch (IOException e) {
            throw new JUploadIOException("ImageReaderWriterHelper.setOutput()",
                    e);
        }
        this.imageWriter.setOutput(this.fileImageOutputStream);
    }

    /**
     * Free all reserved resource by this helper. Includes closing of any input
     * or output stream.
     * 
     * @throws JUploadIOException Any IO Exception
     */
    public void dispose() throws JUploadIOException {
        // First: let's free any resource used by ImageWriter.
        if (this.imageWriter != null) {
            // An imageWriter was initialized. Let's free it.
            this.imageWriter.dispose();
            if (this.fileImageOutputStream != null) {
                try {
                    this.fileImageOutputStream.close();
                } catch (IOException e) {
                    throw new JUploadIOException(
                            "ImageReaderWriterHelper.dispose() [fileImageOutputStream]",
                            e);
                }
            }
            this.imageWriter = null;
            this.fileImageOutputStream = null;
        }

        // Then, all about ImageReader
        if (this.imageReader != null) {
            // An imageReader was initialized. Let's free it.
            this.imageReader.dispose();
            try {
                this.fileImageInputStream.close();
            } catch (IOException e) {
                throw new JUploadIOException(
                        "ImageReaderWriterHelper.dispose() [fileImageInputStream]",
                        e);
            }
            this.imageReader = null;
            this.fileImageInputStream = null;
        }
    }

    /**
     * Call to imageReader.getNumImages(boolean). Caution: may be long, for big
     * files.
     * 
     * @param allowSearch
     * @return The number of image into this file.
     * @throws JUploadIOException
     */
    public int getNumImages(boolean allowSearch) throws JUploadIOException {
        initImageReader();
        try {
            return this.imageReader.getNumImages(allowSearch);
        } catch (IOException e) {
            throw new JUploadIOException(
                    "ImageReaderWriterHelper.getNumImages() [fileImageInputStream]",
                    e);
        }
    }

    /**
     * Call to ImageIO.read(fileImageInputStream).
     * 
     * @return The BufferedImage read
     * @throws JUploadIOException
     *
    public BufferedImage imageIORead() throws JUploadIOException {
        try {
            return ImageIO.read(this.pictureFileData.getWorkingSourceFile());
        } catch (IOException e) {
            throw new JUploadIOException(
                    "ImageReaderWriterHelper.ImageIORead()", e);
        }

    }
    */

    /**
     * Read an image, from the pictureFileData.
     * 
     * @param imageIndex The index number of the picture, in the file. 0 for the
     *            first picture (only valid value for picture containing one
     *            picture)
     * @return The image corresponding to this index, in the picture file.
     * @throws JUploadIOException
     * @throws IndexOutOfBoundsException Occurs when the imageIndex is wrong.
     */
    public BufferedImage readImage(int imageIndex) throws JUploadIOException,
            IndexOutOfBoundsException {
        initImageReader();
        try {
            this.uploadPolicy.displayDebug(
                    "ImageReaderWriterHelper: reading picture number "
                            + imageIndex + " of file "
                            + this.pictureFileData.getFileName(), 30);
            return this.imageReader.read(imageIndex);
        } catch (IndexOutOfBoundsException e) {
            // The IndexOutOfBoundsException is transmitted to the caller. It
            // indicates that the index is out of bound. It's the good way for
            // the caller to stop the loop through available pictures.
            throw e;
        } catch (IOException e) {
            throw new JUploadIOException("ImageReaderWriterHelper.readImage("
                    + imageIndex + ")", e);
        }
    }

    /**
     * Read an image, from the pictureFileData.
     * 
     * @param imageIndex The index number of the picture, in the file. 0 for the
     *            first picture (only valid value for picture containing one
     *            picture)
     * @return The full image data for this index
     * @throws JUploadIOException
     * @throws IndexOutOfBoundsException Occurs when the imageIndex is wrong.
     */
    public IIOImage readAll(int imageIndex) throws JUploadIOException,
            IndexOutOfBoundsException {
        initImageReader();
        try {
            this.uploadPolicy.displayDebug(
                    "ImageReaderWriterHelper: reading picture number "
                            + imageIndex + " of file "
                            + this.pictureFileData.getFileName(), 30);
            return this.imageReader.readAll(imageIndex, this.imageReader
                    .getDefaultReadParam());
        } catch (IndexOutOfBoundsException e) {
            // The IndexOutOfBoundsException is transmitted to the caller. It
            // indicates that the index is out of bound. It's the good way for
            // the caller to stop the loop through available pictures.
            throw e;
        } catch (IOException e) {
            throw new JUploadIOException("ImageReaderWriterHelper.readAll("
                    + imageIndex + ")", e);
        }
    }

    /**
     * Load the metadata associated with one picture in the picture file.
     * 
     * @param imageIndex
     * @return The metadata loaded
     * @throws JUploadIOException Any IOException is encapsulated in this
     *             exception
     */
    public IIOMetadata getImageMetadata(int imageIndex)
            throws JUploadIOException {
        // We must have the reader initialized
        initImageReader();

        // Ok, let's go
        try {
            this.uploadPolicy.displayDebug(
                    "ImageReaderWriterHelper: reading metadata for picture number "
                            + imageIndex + " of file "
                            + this.pictureFileData.getFileName(), 30);
            return this.imageReader.getImageMetadata(imageIndex);
        } catch (IOException e) {
            throw new JUploadIOException(
                    "ImageReaderWriterHelper.getImageMetadata()", e);
        }
    }

    /**
     * Write a picture in the output picture file. Called just before an upload.
     * 
     * @param numIndex The index of the image in the transformed picture file.
     * @param iioImage The image to write.
     * @param iwp The parameter to use to write this image.
     * @throws JUploadIOException
     */
    public void writeInsert(int numIndex, IIOImage iioImage, ImageWriteParam iwp)
            throws JUploadIOException {
        initImageWriter();
        try {
            this.imageWriter.writeInsert(numIndex, iioImage, iwp);
        } catch (IOException e) {
            throw new JUploadIOException(
                    "ImageReaderWriterHelper.writeInsert()", e);
        }
    }

    /**
     * Write a picture in the output picture file. Called just before an upload.
     * 
     * @param iioImage The image to write.
     * @throws JUploadIOException
     */
    public void write(IIOImage iioImage) throws JUploadIOException {
        initImageWriter();
        try {
            this.imageWriter.write(null, iioImage, this.imageWriterParam);
        } catch (IOException e) {
            throw new JUploadIOException("ImageReaderWriterHelper.write()", e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // //////////////////// PRIVATE METHODS
    // ////////////////////////////////////////////////////////////////////

    /**
     * Initialize the ImageWriter and the ImageWriteParam for the current
     * picture helper.
     * 
     * @throws JUploadIOException
     */
    private void initImageWriter() throws JUploadIOException {
        if (this.imageWriter == null) {
            // Get the writer (to choose the compression quality)
            // In the windows world, file extension may be in uppercase, which
            // is not compatible with the core Java API.
            Iterator<ImageWriter> iter = ImageIO
                    .getImageWritersByFormatName(this.targetPictureFormat);
            if (!iter.hasNext()) {
                // Too bad: no writer for the selected picture format

                // A particular case: no gif support in JRE 1.5. A JRE upgrade
                // must be done.
                if (this.targetPictureFormat.equals("gif")
                        && System.getProperty("java.version").startsWith("1.5")) {
                    throw new JUploadIOException(
                            "gif pictures are not supported in Java 1.5. Please switch to JRE 1.6.");
                }

                throw new JUploadIOException("No writer for the '"
                        + this.targetPictureFormat + "' picture format.");
            } else {
                this.imageWriter = iter.next();
                this.imageWriterParam = this.imageWriter.getDefaultWriteParam();

                // For jpeg pictures, we force the compression level.
                if (this.targetPictureFormat.equalsIgnoreCase("jpg")
                        || this.targetPictureFormat.equalsIgnoreCase("jpeg")) {
                    this.imageWriterParam
                            .setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    // Let's select a good compromise between picture size
                    // and quality.
                    this.imageWriterParam
                            .setCompressionQuality(this.uploadPolicy
                                    .getPictureCompressionQuality());
                    // In some case, we need to force the Huffman tables:
                    ((JPEGImageWriteParam) this.imageWriterParam)
                            .setOptimizeHuffmanTables(true);
                }

                //
                try {
                    this.uploadPolicy.displayDebug(
                            "ImageWriter1 (used), CompressionQuality="
                                    + this.imageWriterParam
                                            .getCompressionQuality(), 50);
                } catch (Exception e2) {
                    // If we come here, compression is not supported for
                    // this picture format, or parameters are not explicit
                    // mode, or ... (etc). May trigger several different
                    // errors. We just ignore them: this par of code is only
                    // to write some debug info.
                    this.uploadPolicy.displayWarn(e2.getClass().getName()
                            + " in ImageReaderWriterHelper.java");
                }
            }
        }
    }// initImageWriter

    /**
     * Initialize the ImageReader for the current helper.
     * 
     * @throws JUploadIOException
     */
    private void initImageReader() throws JUploadIOException {
        // First: we open a ImageInputStream
        try {
            this.fileImageInputStream = new FileImageInputStream(
                    this.pictureFileData.getWorkingSourceFile());
        } catch (IOException e) {
            throw new JUploadIOException(
                    "ImageReaderWriterHelper.initImageReader()", e);
        }

        // Then: we create an ImageReader, and assign the ImageInputStream to
        // it.
        if (this.imageReader == null) {
            String ext = DefaultFileData.getExtension(this.pictureFileData
                    .getFile());
            Iterator<ImageReader> iterator = ImageIO
                    .getImageReadersBySuffix(ext);
            if (iterator.hasNext()) {
                this.imageReader = iterator.next();
                this.imageReader.setInput(this.fileImageInputStream);
                this.uploadPolicy.displayDebug("Foud one reader for " + ext
                        + " extension", 50);
            }// while

            // Did we find our reader ?
            if (this.imageReader == null) {
                this.uploadPolicy.displayErr("Found no reader for " + ext
                        + " extension");
            } else if (this.uploadPolicy.getDebugLevel() >= 50) {
                // This call may be long, so we do it only if useful.
                try {
                    this.uploadPolicy.displayDebug("Nb images in "
                            + this.pictureFileData.getFileName() + ": "
                            + this.imageReader.getNumImages(true), 50);
                } catch (IOException e) {
                    // We mask the error, was just for debug...
                }
            }
        }
    }
}
