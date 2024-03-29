package wjhk.jupload2.upload.helper;

import wjhk.jupload2.context.JUploadContext;
import wjhk.jupload2.exception.JUploadIOException;
import wjhk.jupload2.upload.FileUploadThreadHTTP;

/**
 * This interface contains all technical methods to encode data, into a given
 * character encoding. This is especially useful to encode the HTTP output to
 * the server. <BR>
 * <BR>
 * Each appendXxxx method returns the current instance. This allows easy
 * concatanation of calls to this class. For instance:<BR>
 * 
 * <PRE>
 * bae.append(a).appendFileProperty(b, c).append(d);
 * </PRE>
 * 
 * @author etienne_sf
 * @see FileUploadThreadHTTP
 */
public interface ByteArrayEncoder {

    /**
     * Closes the encoding writer, and prepares the encoded length and byte
     * array. This method must be called before call to
     * {@link #getEncodedLength()} and {@link #getEncodedByteArray()}.
     * <B>Note:</B> After a call to this method, you can not append any new data
     * to the encoder.
     * 
     * @throws JUploadIOException Encapsulates any IO Exception
     */
    public void close() throws JUploadIOException;

    /**
     * Append a string, to be encoded at the current end of the byte array.
     * 
     * @param str The string to append and encode.
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException
     */
    public ByteArrayEncoder append(String str) throws JUploadIOException;

    /**
     * Append a byte, to be encoded at the current end of the byte array. he
     * byte to be written is the eight low-order bits of the argument b. The 24
     * high-order bits of b are ignored.
     * 
     * @param b Writes the specified byte to this output stream.
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException
     */
    public ByteArrayEncoder append(int b) throws JUploadIOException;

    /**
     * Append a stream, to be encoded at the current end of the byte array.
     * 
     * @param b
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException
     */
    public ByteArrayEncoder append(byte[] b) throws JUploadIOException;

    /**
     * Append a property, name and value. It will be encoded at the current end
     * of the byte array.<BR>
     * Note: After the last call to appendTextProperty, you should call
     * {@link #appendEndPropertyList()}, to properly finish the property list.
     * In HTTP mode, it will add the last boundary, at a specific format.
     * 
     * @param name Name of the property to be added
     * @param value Value of this property for the current file. It's up to the
     *            caller to call this method at the right time.
     * @param index Index of the file concerned by this value. -1 if this is a
     *            global parameter.
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException
     * @see #appendEndPropertyList()
     */
    public ByteArrayEncoder appendTextProperty(String name, String value,
            int index) throws JUploadIOException;

    /**
     * Finish a property list. In HTTP mode, the last boundary for the
     * form/multipart content is added. After a call to this method, no more
     * property may be written. If several ByteEncoder are used, it's up to the
     * called to call this mehod only once, for the ByteEncoder that will be
     * written last on the request.
     * 
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException
     */
    public ByteArrayEncoder appendEndPropertyList() throws JUploadIOException;

    /**
     * Add to the current encoder all properties contained in the given HTML
     * form. There is no index for the current file: all the form parameters are
     * global parameters (index will be set to -1 when calling
     * {@link #appendTextProperty(String, String, int)}.
     * 
     * @param formname The HTML form name. This method will get the data from
     *            this form, by using the {@link JUploadContext#getApplet()}
     *            method.
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException
     */
    public ByteArrayEncoder appendFormVariables(String formname)
            throws JUploadIOException;

    /**
     * Append a string, to be encoded at the current end of the byte array.
     * 
     * @param bae The ByteArrayEncoder whose encoding result should be appended
     *            to the current encoder. bae must be closed, before being
     *            appended.
     * @return Return the current ByteArrayEncoder, to allow chained call (see
     *         explanation, here above).
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public ByteArrayEncoder append(ByteArrayEncoder bae)
            throws JUploadIOException;

    /**
     * @return the closed
     */
    public boolean isClosed();

    /**
     * Gets the HTTP boundary, that separate the form variables.
     * 
     * @return The HTTP boundary, that was generated when the instance was
     *         created.
     */
    public String getBoundary();

    /**
     * @return the encoding
     */
    public String getEncoding();

    /**
     * Get the length of the encoded result. Can be called only once the encoder
     * has been closed.
     * 
     * @return the encodedLength
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public int getEncodedLength() throws JUploadIOException;

    /**
     * Get the encoded result. Can be called only once the encoder has been
     * closed.
     * 
     * @return the encodedByteArray
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public byte[] getEncodedByteArray() throws JUploadIOException;

    /**
     * Get the String that matches the encoded result. Can be called only once
     * the encoder has been closed.
     * 
     * @return the String that has been encoded.
     * @throws JUploadIOException This exception is thrown when this method is
     *             called on a non-closed encoder.
     */
    public String getString() throws JUploadIOException;

}