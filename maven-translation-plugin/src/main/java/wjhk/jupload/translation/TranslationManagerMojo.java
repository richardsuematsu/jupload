package wjhk.jupload.translation;


import java.io.Closeable;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;

/**
 * We'll check all files, out of the .svn folder, which exist when we're working
 * from a SVN checkout.
 */
class NoSvnFilenameFilter implements FilenameFilter {
	public boolean accept(File arg0, String arg1) {
		// We accept all files and folders, out of the ".svn" folder
		return !(arg1.equals(".svn"));
	}
}

/**
 * This Maven plugin is manages the JUpload translation. It allows: <DIR> <LI>
 * translators to manage human readable files. <LI>Encoding of the translations
 * in unicode, as requested for Java property files.<LI>Back formatting of each
 * 'human readable' translation file, according to the default translation
 * (lang.properties). This allows all files to have the same format (including
 * comment, order of the translation properties...)<LI>Generate the
 * documentation, describing the existing translations </DIR>
 * 
 * @phase generate-sources
 * @goal translate
 * @author etienne_sf
 */
// TODO Document this (in the howto-translate.apt file)

public class TranslationManagerMojo extends AbstractMojo {
	/** Logger for this class */
	protected final Logger logger = Logger
			.getLogger(TranslationManagerMojo.class);

	final static String DEFAULT_ENCODING = "UTF-8";

	final static String NATIVE_ENCODING = "NATIVE";

	final static String FILENAME_PREFIX = "lang";

	final static String FILENAME_SUFFIX = ".properties";

	final static Pattern PROPERTY_PATTERN = Pattern.compile("^(.*) = (.*)$");

	final static String TEMPLATE_FILENAME = "lang.template.properties";

	/**
	 * The default file is the lang.properties file. It will be unchanged. It
	 * actually acts as the file template to format all other files.
	 */
	File defaultFile = null;

	/**
	 * The instance of velocityEngine, which will be used to render the velocity
	 * templates.
	 */
	VelocityEngine velocityEngine = null;

	/**
	 * This files filter, when used in the
	 * {@link File#listFiles(FilenameFilter)} method, allows to get rid of the
	 * '.svn' folder, which we don't care when manipulating translations.
	 */
	private static FilenameFilter noSvnFilenameFilter = new NoSvnFilenameFilter();

	/*********************************************************************************************************
	 ********************************** LIST OF MOJO PARAMETERS **********************************************
	 *********************************************************************************************************/

	/**
	 * The doc folder is the target folder, which will contain the generated
	 * documentation for each property file, found in the inputFolder. It is
	 * used to generate the documentation about the project translation. <BR>
	 * If the doc folder is a subfolder of the project target folder, then it
	 * may not exist. The plugin will create it. Otherwise, the specified folder
	 * must exist.<BR>
	 * It creates a file for each translation file. It uses the following
	 * properties, reading them from each translation language file:<DIR>
	 * <UL>
	 * <I>generateHtmlFileForTranslators</I>: true if a doc file should be
	 * generated for this file. Default to true: if this parameter is not set,
	 * the documentation is generated.
	 * <UL>
	 * <I>language</I>: The language for this file. This could be 'guessed' from
	 * the file name (lang_de is German...). Perhaps in a future release...
	 * <UL>
	 * <I>contributor</I>: The name of the contributor(s). It's both a thank to
	 * them ... and the possibility to email them, and ask them to complete the
	 * current translation when new text are added.
	 * <UL>
	 * <I></DIR> This parameter is not mandatory. If it is not defined, no
	 * documentation is generated.
	 * 
	 * @parameter default-value=null
	 */
	protected File docFolder = null;

	/**
	 * This parameter is not directly related to the applet. Its aim is to group
	 * translation for the applet itself, and for a plugin for the Coppermine
	 * picture gallery. <BR>
	 * The hope is: that people who translate the applet lang file would also
	 * translate the Coppermine, at the time.<BR>
	 * This parameter is the folder which contain the Coppermine translation
	 * files. These files are of the form english.php or german.php. The
	 * language property contain the language, and thus allows to construct the
	 * filename of the Coppermine file associated with a lang_XX.property file.<BR>
	 * Note: the Coppermine files are supposed to be encoded in UTF-8.
	 * 
	 * @parameter default-value=null
	 */
	protected File coppermineFolder = null;

	/**
	 * This parameter controls the suffix which will be appended to the
	 * generated doc files. For instance, it can be ".apt" for an APT file (used
	 * in Maven generating steps) or ".html" for HTML generated files. It's up
	 * to your templates (see templateXxx parameters) to generate a file which
	 * content is valid according to your file extension.
	 * 
	 * @parameter default-value=""
	 */
	protected String docFileExtension = null;

	/**
	 * The character inputEncoding scheme to be applied when filtering
	 * resources. It may be one of UTF-8 and UTF-16.
	 * 
	 * @parameter expression="${inputEncoding}" default-value="UTF-8"
	 */
	protected String inputEncoding = DEFAULT_ENCODING;

	/**
	 * The source folder, which contains the original lang files. These files
	 * are the original translation files, for translator. They are encoded
	 * according to the inputEncoding parameter value. These files are read,
	 * then transformed within this folder (no target folder).
	 * 
	 * @parameter default-value="${project.basedir}/src/main/lang"
	 * @required
	 */
	protected File inputFolder = null;

	/**
	 * These folder is the target for the files used by Java. They are the
	 * result of the native2ascii transformation of the files that are in the
	 * inputFolder.
	 * 
	 * @parameter default-value="${project.basedir}/src/main/resources/lang"
	 * @required
	 */
	protected File resourceLangFolder = null;

	/**
	 * The template for the file which describes the format of the file, which
	 * will contain the description of each available translation. This template
	 * is a Velocity template, based on 1.6 version of Velocity. This template
	 * should be encoded in UTF-8 format. The generated file will also be
	 * encoded in UTF-8.
	 * <P>
	 * In this parameter, you'll have to put a path which can be find by the
	 * class loader.
	 * <P>
	 * If this parameter is empty, null or not defined, no template will be
	 * applied.
	 * </P>
	 * <P>
	 * The available variables are:
	 * </P>
	 * <DIR>
	 * <UL>
	 * translations: The list of all translation. A foreach loop like the sample
	 * below allows you to go through each item of this list.
	 * <UL>
	 * translation.filename: The filename for the property file which contains
	 * this translation (e.g.: lang_fr.properties)
	 * <UL>
	 * translation.language: The language for this translation (e.g.: French)
	 * <UL>
	 * translation.contributor: The contributor(s), who made this translation.
	 * It's both a thank to them and the possibility to ask them to translate
	 * new stuff. </DIR>
	 * <P>
	 * Here is a sample to generate a table, in an APT file, for instance for a
	 * maven site documentation:
	 * </P>
	 * 
	 * <PRE>
	 *            --------------------
	 *            JUpload - File Upload Applet - Applet translation (list of available translations)
	 *            --------------------
	 *            --------------------
	 *            --------------------
	 * 
	 *   bla bla bla ... 
	 * 
	 * *--------------------*--------------------*
	 * | Language | Contributor |
	 * #foreach ($translation in $translations)
	 * | {{{./${translation.filename}.html}${translation.language}}} | ${translation.contributor} |
	 * #end
	 *  * --------------------*--------------------*
	 * 
	 * @parameter expression="${templateAvailableTranslation}" default-value=null
	 * </PRE>
	 */
	protected String templateAvailableTranslation = null;

	/**
	 * The template which will be applied for each available translation. It is
	 * a Velocity template, based on 1.6 version of Velocity. This template
	 * should be encoded in UTF-8 format. The generated file will also be
	 * encoded in UTF-8.
	 * <P>
	 * In this parameter, you'll have to put a path which can be find by the
	 * class loader.
	 * <P>
	 * If this parameter is empty, null or not defined, no template will be
	 * applied.
	 * </P>
	 * <P>
	 * The available variables are:<DIR>
	 * <UL>
	 * language: The language for this file.
	 * <UL>
	 * applet_translation: The applet translation. This text is encoded in
	 * US-ASCII, with all unicode characters escaped in the format \\uNNNN
	 * <UL>
	 * coppermine_filename: null if no Coppermine translation given. If you
	 * manage only the applet, just ignore all Coppermine stuff. Read the plugin
	 * documentation for more information.
	 * <UL>
	 * coppermine_translation: The Coppermine translation in the current
	 * language, or in English, if no translation is available. </DIR>
	 * 
	 * <P>
	 * Here is a sample to generate a table, in an APT file, for instance for a
	 * maven site documentation:
	 * </P>
	 * 
	 * <PRE>
	 *            --------------------
	 *            JUpload - File Upload Applet - Applet translation (${language})
	 *            --------------------
	 *            --------------------
	 *            --------------------
	 * 
	 * 
	 * 
	 * Translation for the applet part
	 * 
	 * --------------
	 * 
	 * ${applet_translation}
	 * 
	 * --------------
	 * 
	 * #if (${coppermine_filename})
	 * 
	 * Translation for the CopperminePlugin part
	 * 
	 * #if (${coppermine_filename} == "english.php")
	 *    The translation for ${language} doesn't exist. Please create a new one, based on the English translation below... 
	 * #else
	 *    Content of the <${coppermine_filename}> file.
	 * #end
	 * 
	 * --------------
	 * 
	 * ${coppermine_translation}
	 * 
	 * --------------
	 * 
	 * #end
	 * </PRE>
	 * 
	 * @parameter expression="${templateOneTranslation}" default-value=null
	 */
	protected String templateOneTranslation = null;

	/**
	 * The work folder, used during the translation process. This folder will
	 * contain the generated files. Then, if everything is Ok, these files are
	 * copied back to the {@link #inputFolder} folder..
	 * 
	 * @parameter 
	 *            default-value="${project.build.directory}/translation-workFolder"
	 * @required
	 */
	protected File workFolder = null;

	/*************************************************************************************
	 ********************************************* METHODS
	 *************************************************************************************/

	/**
	 * The default constructor initializes the Mojo logging, and the Velocity
	 * engine.
	 * 
	 * @throws MojoExecutionException
	 */
	public TranslationManagerMojo() throws MojoExecutionException {
		try {
			initVelocity();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getClass().getName()
					+ " in TranslationManagerMojo constructor ("
					+ e.getMessage() + ")");
		}
	}

	/*************************************************************************************
	 ********************************************* METHODS
	 *************************************************************************************/

	/**
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException {
		// inputFolder must be ... a folder!
		if (inputFolder == null) {
			throw new NullPointerException("inputFolder may not be null");
		} else if (!inputFolder.isDirectory()) {
			throw new MojoExecutionException(inputFolder.getAbsolutePath()
					+ " must be a directory");
		}
		getLog().debug("inputFolder: " + inputFolder.getAbsolutePath());
		getLog().debug(
				"resourceLangFolder: " + resourceLangFolder.getAbsolutePath());
		getLog().debug("workFolder: " + workFolder.getAbsolutePath());

		defaultFile = new File(inputFolder + File.separator + FILENAME_PREFIX
				+ FILENAME_SUFFIX);
		getLog().debug("defaultFile: " + defaultFile.getAbsolutePath());

		// Let's go through all available files, and format each one, out of the
		// default one.
		File[] files = inputFolder.listFiles(noSvnFilenameFilter);
		for (int i = 0; i < files.length; i += 1) {
			// We format all files, out of the default one.
			if (files[i].getName().startsWith(FILENAME_PREFIX + "_")) {
				formatOneLangFile(files[i], inputEncoding);
			}
		}

		// If we succeed in formatting files, let's copy them back to the lang
		// folder.
		copyAllLangFilesToInputFolder();
		// Then we copy the file to the resources/lang/ folder
		copyAllLangFilesToResourcesLangFolder();
		// And we finish by generating the documentation, if the docFolder
		// has been defined.
		if (docFolder != null) {
			generateDocForAvailableTranslations();

		}
	}

	/**
	 * This method read one lang files, and return it as a loaded Properties
	 * instance.
	 * 
	 * @param propertyFile
	 *            The property file to load
	 * @param fileEncoding
	 *            The inputEncoding of this property file
	 * @throws MojoExecutionException
	 */
	Properties loadOneLangFile(File propertyFile, String fileEncoding)
			throws MojoExecutionException {
		Properties translation = new Properties();
		InputStream is = null;
		InputStreamReader isr = null;
		ByteArrayInputStream bais = null;
		try {
			is = new FileInputStream(propertyFile);
			isr = new InputStreamReader(is, fileEncoding);
			int i;
			StringBuffer sb = new StringBuffer();
			while ((i = isr.read()) > 0) {
				sb.append((char) i);
			}
			byte[] byteInUnicode = toUnicode(sb.toString()).getBytes("ASCII");
			bais = new ByteArrayInputStream(byteInUnicode);
			translation.load(bais);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getClass().getName()
					+ " while reading this lang file: "
					+ propertyFile.getAbsolutePath() + " (" + e.getMessage()
					+ ")");
		} finally {
			closeStream(isr, "isr");
			closeStream(is, propertyFile.getAbsolutePath());
			closeStream(bais, "bais");
		}

		return translation;
	}

	/**
	 * This method goes through the default files. It directly writes all
	 * comment and empty lines. For property lines, it tries to translated the
	 * text, based on the give property file. If this doesn't succeed a
	 * commented line, indicating that this translation is missing is written,
	 * with the English translation to allow an easy translation of missing
	 * texts in each translation properties file.
	 * 
	 * @throws MojoExecutionException
	 */
	// TODO document this method
	void formatOneLangFile(File file, String fileEncoding)
			throws MojoExecutionException {
		File targetFile = new File(workFolder + File.separator + file.getName());
		InputStream isDefaultFile = null;
		InputStreamReader isrDefaultFile = null;
		PushbackReader pbReader = null;
		OutputStream osTargetFile = null;
		OutputStreamWriter osrTargetFile = null;

		getLog().debug("Translating " + file.getAbsolutePath());
		Properties propTargetTranslation = loadOneLangFile(file, fileEncoding);

		try {
			isDefaultFile = new FileInputStream(defaultFile);
			isrDefaultFile = new InputStreamReader(isDefaultFile, fileEncoding);
			pbReader = new PushbackReader(isrDefaultFile, 1);

			osTargetFile = new FileOutputStream(targetFile);
			osrTargetFile = new OutputStreamWriter(osTargetFile, fileEncoding);

			// EOL may be the two characters (\r\n), or only one of them.
			int lineNumber = 0;
			boolean readingProperty = false;
			boolean lastCharWasEOL = true;
			boolean currentCharIsEOL = true;
			boolean eof = false;
			int iNextRead;
			StringBuffer sbPropertyLine = new StringBuffer();
			while (!eof) {
				iNextRead = pbReader.read();
				eof = (iNextRead < 0);
				char c = (char) iNextRead;

				// Let's manage the '\' character, for property lines, by
				// inspecting the next character, looking for special
				// combinations.
				if (readingProperty && c == '\\') {
					int charForAntiSlashCombination = pbReader.read();
					if (charForAntiSlashCombination < 0) {
						// We finish the stream. It's a standard '\' character.
						// Nothing to do (we go on in current loop).
					} else if (charForAntiSlashCombination == '\\') {
						// We have two '\', so it's actually ... one !
						// Nothing to do (we go on in current loop).
					} else if (charForAntiSlashCombination == '\n'
							|| charForAntiSlashCombination == '\r') {
						// We found an end of line. The current property is
						// going on next line. Let's loop to next character, and
						// ignore this one.

						// We must first skip any second end of line character.
						int nextEOLChar = pbReader.read();
						if (nextEOLChar >= 0) {
							// We found a new character in the input stream. If
							// it forms a two-char-EOL, with the last read
							// character, we skip it
							if (charForAntiSlashCombination == '\n'
									&& nextEOLChar == '\r') {
								if (!readingProperty) {
									// We want to read the nextEOLChar
									// character, in the next loop.
									pbReader.unread(nextEOLChar);
								} else {
									// We'reading a property line. We've just
									// skipped the end of line characters. We'll
									// go on with next char.
									// nothing to do
								}
							}
						}
						continue;
					} else {
						// It's not a special combination. We'll read next char
						// in the next loop, and go one with the current one.
						pbReader.unread(charForAntiSlashCombination);
					}
				}

				lastCharWasEOL = currentCharIsEOL;
				currentCharIsEOL = eof || (c == '\r' || c == '\n');

				if (lastCharWasEOL && !currentCharIsEOL) {
					// We're starting a new line. Is it a property line ?
					readingProperty = (c != '#');
					lineNumber += 1;
				}

				if (!readingProperty) {
					// We directly write characters that are not belonging to a
					// property line
					if (!eof) {
						osrTargetFile.write(c);
					}
				} else if (!currentCharIsEOL) {
					// We're reading the next char of the current property line.
					// Let's store it.
					sbPropertyLine.append(c);
				} else {
					// We reached the end of a property line
					// This line should be a property line, looking like:
					// key = bla bla bla
					// where "bla bla bla" is the default translation.
					String line = sbPropertyLine.toString();
					sbPropertyLine = new StringBuffer();
					readingProperty = false;// Preparing management of next
					// line.

					Matcher matcher = PROPERTY_PATTERN.matcher(line);
					if (matcher.matches()) {
						// Ok, we found a property line.
						String key = matcher.group(1);

						// Do we have this translation available, in the
						// target translation?
						String translation = propTargetTranslation
								.getProperty(key);
						if (translation == null) {
							osrTargetFile.write("#MISSING  " + line);
						} else {
							// We want to keep special characters.
							translation = translation
									.replaceAll("\\n", "\\\\n");
							osrTargetFile.write(key + " = " + translation);
						}
					} else {
						// Hum, hum. The line is not one of the expected
						// cases...
						throw new MojoExecutionException(
								"Error while processing the "
										+ defaultFile.getAbsolutePath()
										+ " translation. The format of this line "
										+ lineNumber + " is not recognized: ["
										+ line + "]");
					}
					// Let's write the current char (an EOL character)
					if (!eof) {
						osrTargetFile.write(c);
					}
				}
			}// while

			// The streams are closed in the finally clause.
		} catch (IOException e) {
			throw new MojoExecutionException(e.getClass().getName()
					+ " while writing this lang file: "
					+ targetFile.getAbsolutePath() + " (" + e.getMessage()
					+ ")");
		} finally {
			closeStream(pbReader, "PushBackReader");
			closeStream(isDefaultFile, "isrDefaultFile");
			closeStream(isDefaultFile, defaultFile.getAbsolutePath());
			closeStream(osrTargetFile, targetFile.getAbsolutePath());
			closeStream(osTargetFile, targetFile.getAbsolutePath());
		}

		// TODO Finish this method
	}

	/**
	 * A utility method, to close any object which implements the Closeable
	 * interface (InputStream, OutputStream, Reader...), without throwing any
	 * Exception. To be used in the finally clauses of try/catch blocs.
	 * 
	 * @param closeable
	 *            The object to close. If null, nothing is done. This allows
	 *            this method to be called whether the Stream (or Writer) has
	 *            been created or not.
	 * @param description
	 *            A description of the object to close. This description will be
	 *            logged if an error occured while closing this object.
	 */
	void closeStream(Closeable closeable, String description) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				getLog().warn(
						e.getClass().getName() + " while closing the "
								+ closeable.getClass().getName() + " ("
								+ description + ") [" + e.getMessage() + "]");
			}
		}
	}

	/**
	 * Transform a String, into a unicode encoded String, with characters not in
	 * the ASCII chart, transformed in "\\uNNNN" string.
	 * 
	 * @param src
	 * @return
	 */
	String toUnicode(String src) {
		if (src == null) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder(src.length() * 6);

			// Char by char conversion
			for (int i = 0; i < src.length(); i++) {
				int c = (int) src.charAt(i);

				// We keep ASCII characters as is
				if (c <= 127) {
					// No transformation for ASCII characters
					sb.append((char) c);
				} else {
					// Generates the \\uNNNN string
					sb.append(String.format("\\u%04x", c));
				}
			}

			return sb.toString();
		}
	}

	/**
	 * Copy a file, byte per byte, into a new one.
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	void copyFile(File source, String sourceEncoding, File target,
			String targetEncoding) throws IOException {
		InputStream is = new FileInputStream(source);
		byte[] bytes = new byte[(int) source.length()];
		is.read(bytes);
		String template = new String(bytes, sourceEncoding);
		OutputStream os = new FileOutputStream(target);
		if (targetEncoding.equals(NATIVE_ENCODING)) {
			os.write(toUnicode(template).getBytes("US-ASCII"));
		} else {
			os.write(template.getBytes(targetEncoding));
		}
		closeStream(is, "copyFile, inputStream: " + source.getAbsolutePath());
		closeStream(os, "copyFile, outputStream: " + target.getAbsolutePath());
	}

	/**
	 * This method copies all files to the lang folder, once they have all been
	 * transformed successfully.
	 * 
	 * @throws MojoExecutionException
	 */
	void copyAllLangFilesToInputFolder() throws MojoExecutionException {
		File[] workFiles = getWorkFolder().listFiles(noSvnFilenameFilter);
		File target;
		for (int i = 0; i < workFiles.length; i += 1) {
			target = new File(getInputFolder(), workFiles[i].getName());
			try {
				copyFile(workFiles[i], inputEncoding, target, inputEncoding);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getClass().getName()
						+ " while copying this lang file: "
						+ workFiles[i].getName() + " from "
						+ getWorkFolder().getAbsolutePath() + " to "
						+ getInputFolder().getAbsolutePath() + " ("
						+ e.getMessage() + ")");
			}
		}
	}

	/**
	 * This method copies all files to the ./src/main/resources/lang/ folder,
	 * once they have all been transformed successfully. During this copy, the
	 * files are encoded in ASCII characters. All non ASCII characters are
	 * replaced by the relevant \\uNNNN string.
	 * 
	 * @throws MojoExecutionException
	 */
	void copyAllLangFilesToResourcesLangFolder() throws MojoExecutionException {
		// First step : copy of the lang.properties files
		try {
			copyFile(new File(getInputFolder(), "lang.properties"),
					inputEncoding, new File(getResourceLangFolder(),
							"lang.properties"), NATIVE_ENCODING);
		} catch (IOException e1) {
			throw new MojoExecutionException(e1.getClass().getName()
					+ " while copying this lang.properties file: from "
					+ getInputFolder().getAbsolutePath() + " to "
					+ getResourceLangFolder().getAbsolutePath() + " ("
					+ e1.getMessage() + ")");
		}

		// Then, copy of all the 'unicoded' files.
		File[] files = getWorkFolder().listFiles(noSvnFilenameFilter);
		File target = resourceLangFolder;
		for (int i = 0; i < files.length; i += 1) {
			target = new File(getResourceLangFolder(), files[i].getName());
			try {
				copyFile(files[i], inputEncoding, target, NATIVE_ENCODING);
			} catch (IOException e2) {
				throw new MojoExecutionException(e2.getClass().getName()
						+ " while copying this lang file: "
						+ files[i].getName() + " from "
						+ getWorkFolder().getAbsolutePath() + " to "
						+ getInputFolder().getAbsolutePath() + " ("
						+ e2.getMessage() + ")");
			}
		}
	}

	/**
	 * Generates the availableTranslation, then one file for each translation,
	 * based on the templateAvailableTranslation and templateOneTranslation
	 * plugin parameters.
	 * 
	 * @throws MojoExecutionException
	 */
	public void generateDocForAvailableTranslations()
			throws MojoExecutionException {
		String action = null;
		PrintWriter pwAvailableTranslation = null;

		try {
			action = "Init of file writer";
			File fileAvailableTranslation = new File(getDocFolder(),
					"available_translations" + getDocFileExtension());
			action = "Opening Stream for "
					+ fileAvailableTranslation.getAbsolutePath();
			pwAvailableTranslation = new PrintWriter(fileAvailableTranslation,
					"UTF-8");

			action = "Velocity: template creation";
			Template template = velocityEngine
					.getTemplate(getTemplateAvailableTranslation());
			action = "Velocity: context creation";
			VelocityContext context = new VelocityContext();

			// Then, manage each translation (based on the properly unicoded
			// files we've just generated)
			File[] files = getResourceLangFolder().listFiles(
					noSvnFilenameFilter);
			List<Map<String, String>> translations = new ArrayList<Map<String, String>>();
			for (int i = 0; i < files.length; i += 1) {
				// Let's read these properties.
				Properties props = new Properties();
				action = "Reading the properties for "
						+ files[i].getAbsolutePath();
				props.load(new FileInputStream(files[i]));

				// Adding properties for Velocity template.
				SortedMap<String, String> translation = new TreeMap<String, String>();
				translation.put("filename", files[i].getName());
				translation.put("language", props.getProperty("language"));
				translation
						.put("contributor", props.getProperty("contributor"));
				translations.add(translation);

				// Let's generate the file
				action = "Generating "
						+ fileAvailableTranslation.getAbsolutePath();
				generateDocForOneTranslation(files[i], props);
			}// for

			// Ok, we fullfill all data for available translations. Let's
			// generate the page.
			context.put("translations", translations);
			template.merge(context, pwAvailableTranslation);
		} catch (MojoExecutionException e) {
			throw e;
		} catch (Exception e) {
			throw new MojoExecutionException(e.getClass().getName()
					+ " while: " + action + " (" + e.getMessage() + ")");
		} finally {
			closeStream(pwAvailableTranslation,
					"printwriterAvailableTranslation");
		}
	}

	VelocityEngine initVelocity() throws Exception {
		final String LOGGER_NAME = "velocityLogger";

		// TODO Remove these three test lines.
		BasicConfigurator.configure();

		velocityEngine = new VelocityEngine();
		velocityEngine.setProperty(
				RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
				"org.apache.velocity.runtime.log.Log4JLogChute");
		velocityEngine.setProperty("runtime.log.logsystem.log4j.logger",
				LOGGER_NAME);
		velocityEngine.init();

		return velocityEngine;
	}

	/**
	 * @throws MojoExecutionException
	 */
	void generateDocForOneTranslation(File file, Properties props)
			throws MojoExecutionException {
		InputStream isApplet = null;
		InputStreamReader isrApplet = null;
		InputStream isCoppermine = null;
		InputStreamReader isrCoppermine = null;
		OutputStream os = null;
		OutputStreamWriter osw = null;
		PrintWriter pw = null;
		String action = null;

		try {
			// Now the target file
			File target = new File(getDocFolder(), file.getName()
					+ getDocFileExtension());
			os = new FileOutputStream(target);
			// FIXME This should generate unicode escape strings (like \\uNNNN)
			osw = new OutputStreamWriter(os, "UTF-8");
			pw = new PrintWriter(osw);

			action = "Velocity: template creation";
			Template template = velocityEngine
					.getTemplate(getTemplateOneTranslation());
			action = "Velocity: context creation";
			VelocityContext context = new VelocityContext();

			// Let's open the applet input file, with the correct encoding
			// charset.
			action = "Reading applet translation";
			File fileAppletTranslation = new File(getInputFolder(), file
					.getName());
			isApplet = new FileInputStream(fileAppletTranslation);
			isrApplet = new InputStreamReader(isApplet, getInputEncoding());
			StringWriter appletTranslationWriter = new StringWriter(
					(int) fileAppletTranslation.length());
			// Let's read the applet translation, by translating the encoding,
			// if necessary. This translation is done by the Reader and Writer
			// previously opened.
			int c;
			while ((c = isrApplet.read()) != -1) {
				appletTranslationWriter.write(c);
			}
			context.put("language", props.get("language"));
			context.put("applet_translation", appletTranslationWriter
					.toString());

			// Should we have a Coppermine translation ?
			if (getCoppermineFolder() != null) {
				// Let's open the Coppermine input file, with the correct
				// encoding charset.
				String filename = props.getProperty("coppermine.language")
						.toLowerCase()
						+ ".php";
				File fileCoppermineTranslation = new File(
						getCoppermineFolder(), filename);
				if (!fileCoppermineTranslation.exists()) {
					// The translation doesn't exist. We'll display the default
					// language file, to allow contributor to translate.
					fileCoppermineTranslation = new File(getCoppermineFolder(),
							"english.php");
				}

				// Let's copy the Coppermine translation, by translating the
				// encoding, if necessary. This translation is done by the
				// Reader and Writer previously opened.
				isCoppermine = new FileInputStream(fileCoppermineTranslation);
				isrCoppermine = new InputStreamReader(isCoppermine, "UTF-8");
				StringWriter coppermineTranslationWriter = new StringWriter(
						(int) fileAppletTranslation.length());
				while ((c = isrCoppermine.read()) != -1) {
					coppermineTranslationWriter.write((char) c);
				}

				context.put("coppermine_filename", fileCoppermineTranslation
						.getName());
				context.put("coppermine_translation",
						coppermineTranslationWriter.toString());
			}// if(getCoppermineFolder()!=null)

			template.merge(context, pw);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getClass().getName()
					+ " while generating file for " + file.getAbsolutePath()
					+ " (" + e.getMessage() + ") [action = " + action + "]");
		} finally {
			closeStream(isApplet, "isApplet");
			closeStream(isrApplet, "isrApplet");
			closeStream(isCoppermine, "isCoppermine");
			closeStream(isrCoppermine, "isrCoppermine");
			closeStream(pw, "pw");
			closeStream(osw, "osw");
			closeStream(os, "os");
		}
	}

	/**
	 * @return the docFolder
	 */
	public File getDocFolder() {
		return docFolder;
	}

	/**
	 * @param docFolder
	 *            the docFolder to set. docFolder may be null.
	 * @throws MojoExecutionException
	 */
	public void setDocFolder(File docFolder) throws MojoExecutionException {
		// It seems like maven set null ... not to the File itself, but to its
		// name. That is: the folder would be: path/null !
		if (docFolder != null && docFolder.getName().equals("null")) {
			docFolder = null;
		}
		if (docFolder != null) {
			if (!docFolder.exists()) {
				// We accept target folder which doesn't exist, only if they are
				// in the target path.
				if (docFolder.getAbsolutePath().contains("/target/")
						|| docFolder.getAbsolutePath().contains("\\target\\")) {
					// Ok, let's create if.
					docFolder.mkdir();
				} else {
					throw new MojoExecutionException("docFolder: "
							+ docFolder.getAbsolutePath() + " must exist");
				}
			} else if (!docFolder.isDirectory()) {
				// docFolder exists, but is not a folder. Forbidden.
				throw new MojoExecutionException("docFolder: "
						+ docFolder.getAbsolutePath()
						+ " exists, but is not a folder");
			}
		}
		this.docFolder = docFolder;
	}

	/**
	 * @return the coppermineFolder
	 */
	public File getCoppermineFolder() {
		return coppermineFolder;
	}

	/**
	 * @param coppermineFolder
	 *            the coppermineFolder to set. coppermineFolder may be null. In
	 *            this case, the Coppermine files are ignored.
	 * @throws MojoExecutionException
	 */
	public void setCoppermineFolder(File coppermineFolder)
			throws MojoExecutionException {
		// It seems like maven set null ... not to the File itself, but to its
		// name. That is: the folder would be: path/null !
		if (coppermineFolder != null
				&& coppermineFolder.getName().equals("null")) {
			coppermineFolder = null;
		}
		if (coppermineFolder != null) {
			if (!coppermineFolder.exists()) {
				throw new MojoExecutionException("coppermineFolder: "
						+ coppermineFolder.getAbsolutePath() + " must exist");
			} else if (!coppermineFolder.isDirectory()) {
				// coppermineFolder exists, but is not a folder. Forbidden.
				throw new MojoExecutionException("coppermineFolder: "
						+ coppermineFolder.getAbsolutePath()
						+ " exists, but is not a folder");
			}
		}
		this.coppermineFolder = coppermineFolder;
	}

	/**
	 * @return the docFileExtension
	 */
	public String getDocFileExtension() {
		return (docFileExtension == null) ? "" : docFileExtension;
	}

	/**
	 * @param docFileExtension
	 *            the docFileExtension to set
	 */
	public void setDocFileExtension(String docFileExtension) {
		this.docFileExtension = docFileExtension;
	}

	/**
	 * @return the inputEncoding
	 */
	public String getInputEncoding() {
		return inputEncoding;
	}

	/**
	 * @param inputEncoding
	 *            the inputEncoding to set
	 * @throws MojoExecutionException
	 */
	public void setInputEncoding(String inputEncoding)
			throws MojoExecutionException {
		if (inputEncoding == null) {
			throw new NullPointerException("inputEncoding may noy be null");
		} else if (!inputEncoding.equals("UTF-8")
				&& !inputEncoding.equals("UTF-16")) {
			throw new MojoExecutionException(
					"Valid values for inputEncoding are: UTF-8 and UTF-16");
		}
		this.inputEncoding = inputEncoding;
	}

	/**
	 * @param inputFolder
	 *            the inputFolder to set
	 * @throws NullPointerException
	 *             When inputFolder is null
	 * @throws MojoExecutionException
	 *             When inputFolder is not a folder.
	 */
	public void setInputFolder(File inputFolder) throws MojoExecutionException {
		if (inputFolder == null) {
			throw new NullPointerException("inputFolder may not be set to null");
		} else if (!inputFolder.isDirectory()) {
			throw new MojoExecutionException("inputFolder: "
					+ inputFolder.getAbsolutePath() + " must be a directory");
		}

		this.inputFolder = inputFolder;

		// The default file must be set, according to this folder.
		defaultFile = new File(inputFolder + File.separator + FILENAME_PREFIX
				+ FILENAME_SUFFIX);
	}

	/**
	 * @return the inputFolder
	 */
	public File getInputFolder() {
		return inputFolder;
	}

	/**
	 * @return the resourceLangFolder
	 */
	public File getResourceLangFolder() {
		return resourceLangFolder;
	}

	/**
	 * @param resourceLangFolder
	 *            the resourceLangFolder to set. If the folder dosn't exist, it
	 *            will be created.
	 * @throws NullPointerException
	 *             When resourceLangFolder is null
	 * @throws MojoExecutionException
	 *             When resourceLangFolder exists, and is not a folder.
	 */
	public void setResourceLangFolder(File resourceLangFolder)
			throws MojoExecutionException {
		// resourceLangFolder must be ... a folder!
		if (resourceLangFolder == null) {
			throw new NullPointerException("resourceLangFolder may not ne null");
		} else if (!resourceLangFolder.isDirectory()) {
			throw new MojoExecutionException("resourceLangFolder: "
					+ resourceLangFolder.getAbsolutePath()
					+ " must be a directory");
		}

		// Ok !
		this.resourceLangFolder = resourceLangFolder;
	}

	/**
	 * @return the templateAvailableTranslation
	 */
	public String getTemplateAvailableTranslation() {
		return templateAvailableTranslation;
	}

	/**
	 * @param templateAvailableTranslation
	 *            the templateAvailableTranslation to set
	 * @throws MojoExecutionException
	 */
	public void setTemplateAvailableTranslation(
			String templateAvailableTranslation) throws MojoExecutionException {
		if (templateAvailableTranslation != null) {
			// Let's check if the file exist.
			File currFolder = new File(".");
			File f = new File(currFolder, templateAvailableTranslation);
			if (!f.isFile()) {
				throw new MojoExecutionException(
						"templateAvailableTranslation: "
								+ templateAvailableTranslation
								+ " must be a valid file");
			}
		}
		this.templateAvailableTranslation = templateAvailableTranslation;
	}

	/**
	 * @return the templateOneTranslation
	 */
	public String getTemplateOneTranslation() {
		return templateOneTranslation;
	}

	/**
	 * @param templateOneTranslation
	 *            the templateOneTranslation to set
	 * @throws MojoExecutionException
	 */
	public void setTemplateOneTranslation(String templateOneTranslation)
			throws MojoExecutionException {
		if (templateOneTranslation != null) {
			// Let's check if the file exist.
			File currFolder = new File(".");
			File f = new File(currFolder, templateOneTranslation);
			if (!f.isFile()) {
				throw new MojoExecutionException("templateOneTranslation: "
						+ templateOneTranslation + " must be a valid file");
			}
		}
		this.templateOneTranslation = templateOneTranslation;
	}

	/**
	 * The work folder may not be null, and should be in the target folder. If
	 * it doesn't exist, we create it.
	 * 
	 * @param workFolder
	 *            the workFolder to set
	 * @throws NullPointerException
	 *             When workFolder is null
	 * @throws MojoExecutionException
	 *             When workFolder exists, and is not a folder.
	 */
	public void setWorkFolder(File workFolder) throws MojoExecutionException {
		if (workFolder == null) {
			throw new MojoExecutionException(
					"workFolder may not be set to null (" + workFolder + ")");
		}

		// If this folder doesn't exist, we create it.
		if (!workFolder.exists()) {
			workFolder.mkdirs();
		}
		// It must be a folder (it may exist before as a file...)
		if (!workFolder.isDirectory()) {
			throw new MojoExecutionException(
					"The workFolder must be a folder (" + workFolder + ")");
		}

		this.workFolder = workFolder;

		// The work folder must be empty, before working, or we may get old file
		// in the execute process.
		emptyFolder(this.workFolder);
	}

	/**
	 * @return the workFolder
	 */
	public File getWorkFolder() {
		return workFolder;
	}

	/*******************************************************************************************
	 ************************** UTILITIES METHODS **********************************************
	 *******************************************************************************************/
	/**
	 * Remove all files in a given folder. The folder remains.
	 * 
	 * @param folder
	 */
	public static void emptyFolder(File folder) {
		// Let's throw a violent exception if folder is null or is not a folder.
		File[] files = folder.listFiles(noSvnFilenameFilter);
		for (int i = 0; i < files.length; i += 1) {
			files[i].delete();
		}
	}
}
