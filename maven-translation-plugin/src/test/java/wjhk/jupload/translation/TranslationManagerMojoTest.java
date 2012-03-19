/**
 * 
 */
package wjhk.jupload.translation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author etienne_sf
 */
public class TranslationManagerMojoTest {

	final static String TEST_COPPERMINE_FOLDER = "src/test/resources/lang.coppermine.utf-8";

	final static String TEST_LANG_UTF8_FOLDER = "src/test/resources/lang.utf-8";

	final static String TEST_LANG_UTF16_FOLDER = "src/test/resources/lang.utf-16";

	final static String TEST_LANG_KO_FOLDER = "src/test/resources/lang.ko";

	final static String DEFAULT_DOC_FOLDER = "target/translationTest-docFolder";

	final static String DEFAULT_INPUT_FOLDER = "target/translationTest-inputFolder";

	final static String DEFAULT_RESOURCE_LANG_FOLDER = "target/translationTest-resourceLangFolder";

	final static String VERIF_UTF8_FOLDER = "src/test/resources/lang.verif.utf-8";

	final static String VERIF_UTF16_FOLDER = "src/test/resources/lang.verif.utf-16";

	final static String VERIF_UTF16_APT_FOLDER = "src/test/resources/lang.verif.utf-16_to_apt";

	final static String VERIF_UTF16_TO_UNICODE_FOLDER = "src/test/resources/lang.verif.utf-16_to_unicode";

	final static String RESOURCE_ENCODING = "UTF-8";

	TranslationManagerMojo translationManagerMojo = null;

	File projectRoot = null;

	File docFolder = null;

	File inputFolder = null;

	File resourceLangFolder = null;

	File verifUTF8Folder = null;

	File verifUTF16Folder = null;

	File verifUTF16AptFolder = null;

	FilenameFilter noSvnFilenameFilter = new NoSvnFilenameFilter();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Let's get the current folder.
		projectRoot = new File(".");
		String currentDir = projectRoot.getAbsolutePath();
		if (projectRoot.getAbsolutePath().endsWith(".")) {
			// This should always be the case.
			currentDir = projectRoot.getAbsolutePath();
			currentDir = currentDir.substring(0, currentDir.length() - 2);
			projectRoot = new File(currentDir);
		}

		if (currentDir.endsWith("jupload")) {
			currentDir = projectRoot.getAbsolutePath() + File.separator
					+ "jupload-translation";
			projectRoot = new File(currentDir);
		} else if (!currentDir.endsWith("maven-translation-plugin")) {
			Assert.fail("Non-managed current folder: " + currentDir);
		}

		verifUTF8Folder = new File(projectRoot, VERIF_UTF8_FOLDER);
		verifUTF16Folder = new File(projectRoot, VERIF_UTF16_FOLDER);
		verifUTF16AptFolder = new File(projectRoot, VERIF_UTF16_APT_FOLDER);

		docFolder = new File(projectRoot, DEFAULT_DOC_FOLDER);
		if (!docFolder.exists()) {
			docFolder.mkdirs();
		} else if (!docFolder.isDirectory()) {
			Assert.fail(docFolder.getAbsolutePath()
					+ " must be a valid directory");
		} else {
			TranslationManagerMojo.emptyFolder(docFolder);
		}

		inputFolder = new File(projectRoot, DEFAULT_INPUT_FOLDER);
		if (!inputFolder.exists()) {
			inputFolder.mkdirs();
		} else if (!inputFolder.isDirectory()) {
			Assert.fail(inputFolder.getAbsolutePath()
					+ " must be a valid directory");
		} else {
			TranslationManagerMojo.emptyFolder(inputFolder);
		}

		resourceLangFolder = new File(projectRoot, DEFAULT_RESOURCE_LANG_FOLDER);
		if (!resourceLangFolder.exists()) {
			resourceLangFolder.mkdirs();
		} else if (!resourceLangFolder.isDirectory()) {
			Assert.fail(resourceLangFolder.getAbsolutePath()
					+ " must be a valid directory");
		} else {
			TranslationManagerMojo.emptyFolder(resourceLangFolder);
		}

		File originalTestLangFolder = new File(projectRoot,
				TEST_LANG_UTF8_FOLDER);
		if (!inputFolder.isDirectory()) {
			Assert.fail(inputFolder.getAbsolutePath()
					+ " must be a valid directory");
		}

		// Test initialization.
		translationManagerMojo = new TranslationManagerMojo();
		translationManagerMojo.setDocFolder(docFolder);
		translationManagerMojo.setInputFolder(inputFolder);
		translationManagerMojo.setResourceLangFolder(resourceLangFolder);
		translationManagerMojo
				.setTemplateAvailableTranslation("src/test/resources/available_translations.template");
		translationManagerMojo
				.setTemplateOneTranslation("src/test/resources/one_translation.template");

		// The next setter will create the folder, if it doesn't exist.
		translationManagerMojo.setWorkFolder(new File(projectRoot,
				"target/translation-workFolder"));

		// Let's copy the lang files from src/test/resources to
		// target/inputFolder
		// The original files will then remain unchanged by the runned tests.
		copyInputFilesForTest(originalTestLangFolder, RESOURCE_ENCODING);
	}

	/**
	 * Copy files for the current test. This inputFolder is emptied before
	 * copying the files to it, to prevent 'interaction' between tests.
	 * 
	 * @param testCaseInputFolder
	 *            The folder which contains the files for the current test.
	 * @throws IOException
	 */
	private void copyInputFilesForTest(File testCaseInputFolder,
			String fileEncoding) throws IOException {
		// First step: emptying input folder.
		File[] files = inputFolder.listFiles(noSvnFilenameFilter);
		for (int i = 0; i < files.length; i += 1) {
			files[i].delete();
		}

		// Second step, copy the source files to the inputFolder.
		files = testCaseInputFolder.listFiles(noSvnFilenameFilter);
		for (int i = 0; i < files.length; i += 1) {
			if (files[i].getName().endsWith(".properties")) {
				translationManagerMojo
						.copyFile(files[i], fileEncoding, new File(inputFolder,
								files[i].getName()), fileEncoding);
			}
		}
	}

	/**
	 * @param source
	 * @param target
	 * @throws IOException
	 * @throws MojoExecutionException
	 */
	private void compareFiles(File source, File target) throws IOException,
			MojoExecutionException {
		if (!source.isFile()) {
			throw new MojoExecutionException(
					"The source file to compare to doesn't exist ("
							+ source.getAbsolutePath() + ")");
		} else if (!target.isFile()) {
			throw new MojoExecutionException(
					"The target file to compare to doesn't exist ("
							+ target.getAbsolutePath() + ")");
		}
		long lenSource = source.length();
 		Assert.assertEquals(
				"Length of the files must be equal ("
						+ source.getAbsolutePath() + " and "
						+ target.getAbsolutePath(), lenSource, target.length());

		BufferedReader brSource = new BufferedReader(new FileReader(source));
		BufferedReader brTarget = new BufferedReader(new FileReader(target));
		String s, t;
		while ((s = brSource.readLine()) != null
				&& (t = brTarget.readLine()) != null) {
			// As the file length are equals, the above and below test are
			// enough (no need to check that we found the end of a file before
			// the other one.
			Assert.assertEquals("Content of the files must be equal ("
					+ source.getAbsolutePath() + " and "
					+ target.getAbsolutePath(), s, t);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	@Test
	public void testExecute_UTF8() throws MojoExecutionException, IOException {
		String filename = "lang_fr.properties";
		File fileLangFR_utf8 = new File(verifUTF8Folder, filename + ".utf-8");
		File fileLangFRUnicode = new File(verifUTF8Folder, filename
				+ ".unicode");
		translationManagerMojo.execute();

		// Let's check the inputFolder. The lang file should remain unchanged.
		// The lang_fr should be formatted.
		Assert.assertEquals("Number of generated files", 2, inputFolder
				.listFiles(noSvnFilenameFilter).length);
		compareFiles(new File(verifUTF8Folder, "lang.properties"), new File(
				inputFolder, "lang.properties"));
		compareFiles(new File(inputFolder + File.separator + filename),
				fileLangFR_utf8);

		// Let's check the resourceLangFolder. The lang file should remain
		// unchanged. The lang_fr should be formatted, and 'unicoded'.
		Assert.assertEquals("Number of generated files", 2, resourceLangFolder
				.listFiles(noSvnFilenameFilter).length);
		compareFiles(new File(verifUTF8Folder, "lang.properties"), new File(
				resourceLangFolder, "lang.properties"));
		compareFiles(new File(resourceLangFolder + File.separator + filename),
				fileLangFRUnicode);

		// Let's check the docFolder.
		Assert.assertEquals("Number of generated files", 3, docFolder
				.listFiles(noSvnFilenameFilter).length);
		// We just check that the file exists. The file generation is tested in
		// testExecute_UTF16()
		// No file extension has been given ...
		filename = "available_translations";
		Assert
				.assertTrue("The file '" + filename + "' must exist in the "
						+ docFolder + " folder", new File(docFolder, filename)
						.isFile());
		filename = "lang.properties";
		Assert
				.assertTrue("The file '" + filename + "' must exist in the "
						+ docFolder + " folder", new File(docFolder, filename)
						.isFile());
		filename = "lang_fr.properties";
		Assert
				.assertTrue("The file '" + filename + "' must exist in the "
						+ docFolder + " folder", new File(docFolder, filename)
						.isFile());
	}

	/**
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	@Test
	public void testExecute_UTF16() throws MojoExecutionException, IOException {
		// First step: initialize this test case with the UTF-16 files
		File originalTestLangFolder = new File(projectRoot,
				TEST_LANG_UTF16_FOLDER);
		if (!inputFolder.isDirectory()) {
			Assert.fail(inputFolder.getAbsolutePath()
					+ " must be a valid directory");
		}
		// Let's copy the lang files from src/test/resources to
		// target/inputFolder
		copyInputFilesForTest(originalTestLangFolder, "UTF-16");
		File verifUTF16ToUnicodeFolder = new File(projectRoot,
				VERIF_UTF16_TO_UNICODE_FOLDER);

		// Let's execute the whole process
		translationManagerMojo.setCoppermineFolder(new File(projectRoot,
				TEST_COPPERMINE_FOLDER));
		translationManagerMojo.setInputEncoding("UTF-16");
		translationManagerMojo.setDocFileExtension(".apt");
		translationManagerMojo.execute();

		// Let's test, now.
		Assert
				.assertEquals(
						"Number of generated files (check of the verification folders)",
						verifUTF16Folder.listFiles(noSvnFilenameFilter).length,
						verifUTF16ToUnicodeFolder
								.listFiles(noSvnFilenameFilter).length);
		Assert.assertEquals("Number of generated files (UTF-16)",
				verifUTF16Folder.listFiles(noSvnFilenameFilter).length,
				inputFolder.listFiles(noSvnFilenameFilter).length);
		Assert.assertEquals("Number of generated files (unicode)",
				verifUTF16Folder.listFiles(noSvnFilenameFilter).length,
				resourceLangFolder.listFiles(noSvnFilenameFilter).length);

		// Check of the inputFolder result.
		File[] inputFiles = inputFolder.listFiles(noSvnFilenameFilter);
		for (int i = 0; i < inputFiles.length; i += 1) {
			compareFiles(new File(verifUTF16Folder, inputFiles[i].getName()),
					inputFiles[i]);
			compareFiles(new File(verifUTF16ToUnicodeFolder, inputFiles[i]
					.getName()), new File(translationManagerMojo
					.getResourceLangFolder(), inputFiles[i].getName()));
		}// for

		// Check of the docFolder content, based on the inputFolder files.
		File[] htmlFiles = docFolder.listFiles(noSvnFilenameFilter);
		Assert
				.assertEquals(
						"Number of html generated files, including the available_translations.html file.",
						inputFiles.length + 1, htmlFiles.length);
		for (int i = 0; i < htmlFiles.length; i += 1) {
			File fileToCompare = new File(verifUTF16AptFolder, htmlFiles[i]
					.getName());
			compareFiles(fileToCompare, htmlFiles[i]);
		}// for
	}

	/**
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	@Test
	public void testExecute_KO() throws MojoExecutionException, IOException {
		// Test with a null inputFolder.
		try {
			translationManagerMojo.inputFolder = null;
			translationManagerMojo.execute();
			Assert.fail("Null inputFolder is prohibited");
		} catch (NullPointerException e) {
			// Success.
		}
		// Test with a non directory folder.
		try {
			translationManagerMojo.inputFolder = new File(projectRoot,
					"not a valid file");
			translationManagerMojo.execute();
			Assert.fail("inputFolder must be directory");
		} catch (MojoExecutionException e) {
			// Success.
		}
	}

	/**
	 * @throws MojoExecutionException
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testLoadOneLangFile_OK() throws MojoExecutionException,
			UnsupportedEncodingException {
		File propFile = new File(projectRoot, DEFAULT_INPUT_FOLDER
				+ File.separator + "lang.properties");
		Properties props = translationManagerMojo.loadOneLangFile(propFile,
				"ASCII");
		Assert.assertEquals("The lang.properties file contains 5 entries", 5,
				props.size());
		Assert
				.assertEquals(
						"The lang.properties file contains this entry",
						"This is a key.With a new line in it, and with a \n character!",
						props.get("aKey"));
		Assert
				.assertEquals(
						"The lang.properties file contains this entry",
						"This is another key.Also with a new line in it, and also with a \n character!",
						props.get("anotherKey"));
		Assert.assertEquals("The lang.properties file contains this entry",
				"this it the last key", props.get("aLastKey"));

		propFile = new File(projectRoot, DEFAULT_INPUT_FOLDER + File.separator
				+ "lang_fr.properties");
		props = translationManagerMojo.loadOneLangFile(propFile, "UTF-8");
		Assert.assertEquals("The lang_fr.properties file contains 4 entries",
				4, props.size());
		Assert.assertEquals("The lang_fr.properties file contains this entry",
				"C'est une clef", props.get("aKey"));
		// The properties files may not be in UTF-8, but our file is !
		// Let's read the property, based on its UTF-8 inputEncoding.
		String lastValue = ((String) props.get("aLastKey"));
		Assert.assertEquals("The lang_fr.properties file contains this entry",
				"C'est une derni\u00e8re clef\nAvec un passage à la ligne!",
				lastValue);

		propFile = new File(projectRoot, TEST_LANG_UTF16_FOLDER
				+ File.separator + "lang_fr.properties");
		props = translationManagerMojo.loadOneLangFile(propFile, "UTF-16");
		Assert.assertEquals("The lang_fr.properties file contains 82 entries",
				82, props.size());
		// The properties files may not be in UTF-8, but our file is !
		// Let's read the property, based on its UTF-8 inputEncoding.
		String propName = "preparingFile";
		String propValueNotEncoded = (String) props.get(propName);
		Assert.assertNotNull("The '" + propName + "' should exist",
				propValueNotEncoded);
		Assert.assertEquals(
				"The lang_fr.properties file contains this entry: '" + propName
						+ "'", "Pr\u00E9paration du fichier %1$d/%2$d",
				propValueNotEncoded);
	}

	/**
	 */
	@Test
	public void testLoadOneLangFile_KO() {
		File propFile = new File(projectRoot, DEFAULT_INPUT_FOLDER
				+ File.separator + "a_non_existing_file");
		try {
			translationManagerMojo.loadOneLangFile(propFile, "UTF-8");
			Assert.fail(propFile.getAbsolutePath()
					+ " doesn't exist: should throw an error");
		} catch (MojoExecutionException e) {
			// Success
		}
	}

	/**
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	@Test
	public void testFormatOneLangFile_utf8_OK() throws MojoExecutionException,
			IOException {
		File propFile = new File(projectRoot, DEFAULT_INPUT_FOLDER
				+ File.separator + "lang_fr.properties");
		translationManagerMojo.formatOneLangFile(propFile, RESOURCE_ENCODING);

		String generatedFile = translationManagerMojo.getWorkFolder()
				.getAbsolutePath()
				+ File.separator + propFile.getName();
		File verifFile = new File(verifUTF8Folder, propFile.getName()
				+ ".utf-8");
		compareFiles(new File(generatedFile), verifFile);
	}

	/**
	 * @throws IOException
	 */
	@Test
	public void testFormatOneLangFile_utf8_KO() throws IOException {
		copyInputFilesForTest(new File(projectRoot, TEST_LANG_KO_FOLDER),
				RESOURCE_ENCODING);
		File propFile = new File(projectRoot, DEFAULT_INPUT_FOLDER
				+ File.separator + "lang_fr.properties");
		try {
			translationManagerMojo.formatOneLangFile(propFile,
					RESOURCE_ENCODING);
			Assert.fail("processing lang.KO should throw an exception");
		} catch (MojoExecutionException e) {
			// Success!
		}
	}

	/**
	 * @throws FileNotFoundException
	 */
	@Test
	public void testCloseStream() throws FileNotFoundException {
		// Closing a null Closeable is allowed.
		translationManagerMojo.closeStream(null,
				"A null FileInputStream should not raise an exception");

		FileInputStream fis = new FileInputStream(new File(inputFolder,
				"lang.properties"));
		translationManagerMojo.closeStream(fis, "A test FileInputStream");
		// The stream must be closed. The only way to check that, is to try to
		// read from it.
		try {
			fis.read();
			Assert.fail("The stream should be closed!");
		} catch (IOException e) {
			// Ok !
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testCopyAllLangFilesToLangFolder() throws Exception {
		File source = new File(translationManagerMojo.getWorkFolder(),
				"lang_fr.properties");
		File target = new File(translationManagerMojo.getInputFolder(),
				"lang_fr.properties");
		try {
			compareFiles(source, target);
			throw new Exception(
					source.getAbsolutePath()
							+ " and "
							+ target.getAbsolutePath()
							+ " should be different, before copying it back to the lang folder");
		} catch (java.lang.AssertionError e0) {
			// OK
		} catch (MojoExecutionException e1) {
			// OK
		}

		translationManagerMojo.copyFile(new File(projectRoot,
				DEFAULT_INPUT_FOLDER + File.separator + "lang_fr.properties"),
				"UTF-8", source, "UTF-8");

		translationManagerMojo.copyAllLangFilesToInputFolder();
		// Then the files must be equals
		compareFiles(source, target);
	}

	/**
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	@Test
	public void testCopyAllLangFilesToResourcesLangFolder()
			throws MojoExecutionException, IOException {
		File propFile = new File(projectRoot, DEFAULT_INPUT_FOLDER
				+ File.separator + "lang_fr.properties");
		translationManagerMojo.formatOneLangFile(propFile, RESOURCE_ENCODING);
		translationManagerMojo.copyAllLangFilesToResourcesLangFolder();

		String generatedFile = translationManagerMojo.getResourceLangFolder()
				.getAbsolutePath()
				+ File.separator + propFile.getName();
		File verifFile = new File(verifUTF8Folder, propFile.getName()
				+ ".unicode");
		compareFiles(new File(generatedFile), verifFile);
	}

	/**
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	@Test
	public void testGenerateDocForOneTranslation()
			throws MojoExecutionException, IOException {
		File file = new File(verifUTF16Folder, "lang_fr.properties");
		Properties props = new Properties();
		props.setProperty("language", "French_for_test");
		translationManagerMojo.setDocFileExtension(".apt");
		translationManagerMojo.generateDocForOneTranslation(file, props);

		File generatedFile = new File(translationManagerMojo.getDocFolder(),
				"lang_fr.properties.apt");
		File verifFolder = new File(projectRoot,
				"src/test/resources/testGenerateDocForOneTranslation");
		File verifFile = new File(verifFolder, "lang_fr.properties.apt");
		compareFiles(generatedFile, verifFile);
	}

	/**
	 * Test of the setCoppermineFolder parameter. Tests its constaints
	 * 
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetCoppermineFolder() throws MojoExecutionException {
		// null folder are allowed.
		translationManagerMojo.setCoppermineFolder(null);
		Assert.assertNull("coppermineFolder may be null",
				translationManagerMojo.coppermineFolder);
		Assert.assertNull("coppermineFolder may be null",
				translationManagerMojo.getCoppermineFolder());

		// If it exists, it must be a folder.
		try {
			translationManagerMojo.setCoppermineFolder(new File(projectRoot,
					DEFAULT_INPUT_FOLDER + File.separator + "lang.properties"));
			Assert.fail("If it exists, it must be a folder");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		// If it is not null, it must exist
		try {
			translationManagerMojo.setCoppermineFolder(new File(projectRoot,
					File.separator + "not_target" + File.separator
							+ "this_folder_does_not_exist"));
			Assert
					.fail("coppermineFolder which doesn't exist is allowed, only if the path contains /target/");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		// coppermineFolder must exist. We take a valid folder.
		File coppermineFolder = docFolder;
		translationManagerMojo.coppermineFolder = null;
		translationManagerMojo.setCoppermineFolder(coppermineFolder);
		Assert.assertEquals("coppermineFolder must be set", coppermineFolder,
				translationManagerMojo.coppermineFolder);
		Assert.assertEquals("coppermineFolder must be set", coppermineFolder,
				translationManagerMojo.getCoppermineFolder());
	}

	/**
	 * Test of the setHtmlFolder parameter. Tests its constaints
	 * 
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetDocFolder() throws MojoExecutionException {
		// null folder are allowed.
		translationManagerMojo.setDocFolder(null);
		Assert.assertNull("docFolder may be null",
				translationManagerMojo.docFolder);
		Assert.assertNull("docFolder may be null", translationManagerMojo
				.getDocFolder());

		// If it exists, it must be a folder.
		try {
			translationManagerMojo.setDocFolder(new File(projectRoot,
					DEFAULT_INPUT_FOLDER + File.separator + "lang.properties"));
			Assert.fail("If it exists, it must be a folder");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		// If it doesn't exist, it must be within the /target/ folder.
		translationManagerMojo.setDocFolder(new File(projectRoot,
				DEFAULT_INPUT_FOLDER + File.separator + "target"
						+ File.separator + "lang.properties"));
		try {
			translationManagerMojo.setDocFolder(new File(projectRoot,
					File.separator + "not_target" + File.separator
							+ "this_folder_does_not_exist"));
			Assert
					.fail("docFolder which doesn't exist is allowed, if the path contains /target/");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		translationManagerMojo.docFolder = null;
		translationManagerMojo.setDocFolder(docFolder);
		Assert.assertEquals("docFolder should be null", docFolder,
				translationManagerMojo.docFolder);
		Assert.assertEquals("docFolder should be null", docFolder,
				translationManagerMojo.getDocFolder());
	}

	/**
	 * Test of the setHtmlFolder parameter. Tests its constaints
	 * 
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetDocExtension() throws MojoExecutionException {
		// null folder are allowed.
		translationManagerMojo.setDocFileExtension(null);
		Assert.assertNull("docFileExtension may be null",
				translationManagerMojo.docFileExtension);
		Assert
				.assertEquals(
						"docFileExtension may be null. getDocFileExtension() should return an empty string",
						"", translationManagerMojo.getDocFileExtension());

		String docFileExtension = ".an_extension";
		translationManagerMojo.setDocFileExtension(docFileExtension);
		Assert.assertEquals("DocFileExtension must be set", docFileExtension,
				translationManagerMojo.docFileExtension);
		Assert
				.assertEquals(
						"When DocFileExtension is null, getDocFileExtension() should return an empty string",
						docFileExtension, translationManagerMojo
								.getDocFileExtension());
	}

	/**
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetInputEncoding() throws MojoExecutionException {
		Assert.assertEquals("Default inputEncoding is UTF-8",
				translationManagerMojo.inputEncoding, "UTF-8");

		translationManagerMojo.setInputEncoding("UTF-16");
		Assert.assertEquals(translationManagerMojo.inputEncoding, "UTF-16");

		translationManagerMojo.setInputEncoding("UTF-8");
		Assert.assertEquals("Setting to UTF-8",
				translationManagerMojo.inputEncoding, "UTF-8");

		try {
			translationManagerMojo.setInputEncoding(null);
			Assert.fail("inputEncoding null value should be prohibited");
		} catch (NullPointerException e) {
			// Success!
		}
		try {
			translationManagerMojo
					.setInputEncoding("Not a valid inputEncoding");
			Assert
					.fail("inputEncoding value has a limited set of valid values");
		} catch (MojoExecutionException e) {
			// Success!
		}

	}

	/** */
	@Test
	public void testGetLangFolder() {
		Assert.assertEquals(translationManagerMojo.inputFolder,
				translationManagerMojo.getInputFolder());
		translationManagerMojo.inputFolder = null;
		Assert.assertEquals(translationManagerMojo.inputFolder,
				translationManagerMojo.getInputFolder());
	}

	/**
	 * Test of the setLangFolder parameter. Tests its constaints
	 * 
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetInputFolder() throws MojoExecutionException {
		// null folder are forbidden.
		try {
			translationManagerMojo.setInputFolder(null);
			Assert.fail("null folder are forbidden");
		} catch (NullPointerException e) {
			// Ok !
		}

		// If it exists, it must be a folder.
		try {
			translationManagerMojo.setInputFolder(new File(projectRoot,
					DEFAULT_INPUT_FOLDER + File.separator + "lang.properties"));
			Assert.fail("If it exists, it must be a folder");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		translationManagerMojo.inputFolder = null;
		translationManagerMojo.setInputFolder(new File(projectRoot,
				DEFAULT_INPUT_FOLDER));
		Assert.assertEquals("inputFolder must be set", inputFolder,
				translationManagerMojo.inputFolder);

	}

	/**
	 */
	@Test
	public void testGetWorkDirectory() {
		Assert.assertEquals(translationManagerMojo.workFolder,
				translationManagerMojo.getWorkFolder());
	}

	/**
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetWorkDirectory() throws MojoExecutionException {
		File dummyWorkDirectory = new File("target/a_dummy_folder");
		Assert.assertNotSame(translationManagerMojo.getWorkFolder(),
				dummyWorkDirectory);

		// null folder are forbidden.
		try {
			translationManagerMojo.setWorkFolder(null);
			Assert.fail("null folder are forbidden");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		// If it exists, it must be a folder.
		try {
			translationManagerMojo.setWorkFolder(new File(projectRoot,
					DEFAULT_INPUT_FOLDER + File.separator + "lang.properties"));
			Assert.fail("If it exists, it must be a folder");
		} catch (MojoExecutionException e) {
			// Ok !
		}

		// We want to test the folder creation.
		if (dummyWorkDirectory.exists()) {
			dummyWorkDirectory.delete();
		}
		Assert.assertFalse("The " + dummyWorkDirectory.getAbsolutePath()
				+ " should not exist", dummyWorkDirectory.exists());

		// Let's go...
		translationManagerMojo.setWorkFolder(dummyWorkDirectory);
		Assert.assertEquals("After setter: the property should be set!",
				translationManagerMojo.getWorkFolder(), dummyWorkDirectory);
		Assert.assertTrue("The " + dummyWorkDirectory.getAbsolutePath()
				+ " should exist", dummyWorkDirectory.exists());
	}

	/**
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetResourceLangFolder() throws MojoExecutionException {
		translationManagerMojo.resourceLangFolder = null;
		translationManagerMojo.setResourceLangFolder(resourceLangFolder);
		Assert.assertEquals("resourceLangFolder must be set",
				resourceLangFolder, translationManagerMojo.resourceLangFolder);

		// null folder are forbidden.
		try {
			translationManagerMojo.setResourceLangFolder(null);
			Assert.fail("null folder are forbidden");
		} catch (NullPointerException e) {
			// Ok !
		}

		// If it exists, it must be a folder.
		try {
			translationManagerMojo.setResourceLangFolder(new File(projectRoot,
					DEFAULT_INPUT_FOLDER + File.separator + "lang.properties"));
			Assert.fail("If it exists, it must be a folder");
		} catch (MojoExecutionException e) {
			// Ok !
		}
	}

	/**
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetTemplateAvailableTranslation()
			throws MojoExecutionException {
		translationManagerMojo.templateAvailableTranslation = null;
		Assert.assertNull("templateAvailableTranslation should be null",
				translationManagerMojo.getTemplateAvailableTranslation());

		String s = "a_non_existing_template";
		try {
			translationManagerMojo.setTemplateAvailableTranslation(s);
			Assert
					.fail("Call to setTemplateAvailableTranslation must be done with a valid file as an argument");
		} catch (MojoExecutionException e) {
			// Success!
		}

		s = "src/test/resources/one_translation.template";
		translationManagerMojo.setTemplateAvailableTranslation(s);
		Assert.assertEquals("Test 1", s,
				translationManagerMojo.templateAvailableTranslation);
		Assert.assertEquals("Test 1", s, translationManagerMojo
				.getTemplateAvailableTranslation());

		translationManagerMojo.setTemplateAvailableTranslation(null);
		Assert.assertNull("templateAvailableTranslation should be null",
				translationManagerMojo.templateAvailableTranslation);
	}

	/**
	 * @throws MojoExecutionException
	 */
	@Test
	public void testSetTemplateOneTranslation() throws MojoExecutionException {
		translationManagerMojo.templateOneTranslation = null;
		Assert.assertNull("templateOneTranslation should be null",
				translationManagerMojo.getTemplateOneTranslation());

		String s = "a_non_existing_template";
		try {
			translationManagerMojo.setTemplateOneTranslation(s);
			Assert
					.fail("Call to setTemplateOneTranslation must be done with a valid file as an argument");
		} catch (MojoExecutionException e) {
			// Success!
		}

		s = "src/test/resources/one_translation.template";
		translationManagerMojo.setTemplateOneTranslation(s);
		Assert.assertEquals("Test 1", s,
				translationManagerMojo.templateOneTranslation);
		Assert.assertEquals("Test 1", s, translationManagerMojo
				.getTemplateOneTranslation());

		translationManagerMojo.setTemplateOneTranslation(null);
		Assert.assertNull("templateOneTranslation should be null",
				translationManagerMojo.templateOneTranslation);
	}
}
