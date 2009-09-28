MAKE TARGETS
============

help (default): display the README file			

updatedeps:	fetch the jars of all jtem projects that this project 
	depends on (list is read from dependencies.txt) dependencies 
	will only be checked once a day, to force an update, remove 
	the corresponding archiv. Add third party archives to the 
	same directory (default is "lib", see the variable LIBDIR in 
	the Makefile) 
	
binaries: compile the java classes

test: run the JUnit tests
	
javadoc: generate the api documentation with javadoc

web: produce and send the snippets for the projectspecific 
	part of the jtem website
	
release: generate the archives for a release and put them on the 
	jTEM server for download

clean: remove all generated files (see the variable 
	CLEAN in the makefile)

debug: print the values of some generated variables



MAKEFILE CUSTOMIZATION
======================

change the variables at the beginning of the Makefile, or override variables via:
	make <VARIABLENAME>="my value" <target>
	
