Hi,

  Thank you for downloading JUpload !


The jupload-X.Y.Z-src-X.Y.Z.zip file contains all you need to use, compile or hack JUpload. If you're
downloading it, you can check its integrity with the available md5 and sha files.

The whole project documentation is available on-line, here: http://jupload.sourceforge.net/
and in the ./site/folder of the zip release file.
 


Here is a description of the package content.
If you checked out the sources from SVN your paths will differ: you won't have 
the /site folder, only available in official releases. To generate the site, 
please read the doc.


QUICK START :

The main entry points are:
- Compiled applet: /site/wjhk.jupload.jar
- full sources :  /src
- all translation material: /site/translation
- demo web site, with all the JUPload documentation:  /site/index.html
  It contains links to all docs for the JUpload project.


PACKAGE CONTENT:

/src
  Full sources.
  The JUpload applet java sources are in /src/main/java/

/site
  The copy of the JUpload web site, available as a link from the jupload sourceforge project.
  Its main content is:
  
	Main files and folders:
	  /site/apidocs/
	    The JUpload javadoc.
	  /site/wjhk.jupload.jar
	    THE COMPILED APPLET, that can you use directly on your web site. You should sign it with your own certificate.
	    See the /site/README.txt file
	  /site/samples.java/
	    Some samples for a J2EE server, like tomcat. The parseRequest.jsp show an example of managing uploaded file
	    in Java.
	  /site/samples.PHP/
	    Some samples for a J2EE server, like tomcat. The parseRequest.jsp show an example of managing uploaded file
	    in Java.

	Main files:
	  /site/advanced_js_demo.html
	    Allows you to test the applet with various parameters.
	  /site/applet-basic-picture.html
	    Example of the applet in picture mode (uses the PictureUploadPolicy upload policy)
	  /site/applet-basic.html
	    Basic applet demo.
	  /site/common.js
	    Some javascript used by the other pages.
	  /site/howto-customize.html
	    Documentation how you can make the applet match to your needs.
	  /site/howto-debug.html
	    How to analyse what's happening.
	  /site/howto-support.html
	    How and where to get support.
	  /site/howto-translate.html
	    We accept all translations !!!   ;-)
	  /site/index.html
	    ENTRY POINT FOR THE DEMO WEB SITE
	  /site/jakarta-commons-oro.jar
	    the jakarata library, signed by our demo certificate. Used in FTP mode only.
	  /site/nocache.php
	    Used to prevent caching of the jar file, by the sourceforge hosting.
	  /site/RELEASE-NOTES.txt
	    What's new in this relese (and full history)
	  /site/upload_dummy.php
	    A dummy php file, that can receive uploads. Can be used as a (very) simple sample in PHP
	  /site/wjhk.jupload.jar
	    THE APPLET, READY TO USE.

	
	  /site/samples.java/
	    Java samples, as an exemple on how to embed JUpload in your java application
	  /site/samples.PHP/
	    PHP samples, as an exemple on how to embed JUpload in your PHP application

  
  You can use /site as the root for a J2EE application server, like Tomcat or Apache/PHP.