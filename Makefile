#base name of the project
NAME=halfedgetools

#space separated list of source directories
SRCDIRS=src

#where to put the binaries, also used to retrieve the binaries for the release archives
BINDIR=classes

#where to put the generated javadoc
DOCDIR=doc

#the snippets for the webpage are put here
WEBDIR=web
#the package summary file (source)
PACKAGEHTML=$(word 1,$(SRCDIRS))/de/jtem/$(NAME)/package-info.java
#the html page to read the websnippets of 
#(usually the processed PACKAGEHTML: package-summary.html) 
PACKAGESUMHTML=$(DOCDIR)/de/jtem/$(NAME)/package-summary.html

#location of the web site, may be empty
SERVER=
#directory of the website on the server, or local if SERVER is empty
SRVDIR=/net/www/pub/jtem

#directory for the dependencies
LIBDIR=lib

#a place to put the archives that constitute a release, before copy to 
#web site
RELDIR=rel

#directories of the JUnit tests, all files that match Test*.java or *Test.java will be executed 
TESTDIR=
TESTBINDIR=$(TESTDIR)
#exclude the following tests  
EXCLTESTS=
#where to find junit.jar
JUNIT=junit.jar#$(shell locate junit.jar |  grep '/junit.jar' | tail --lines=1)

#compile options
JAVACOPTS=-target 1.5 -source 1.5 -Xlint:unchecked

#javadoc options
JAVADOCOPTS= -author -protected -nodeprecated -nodeprecatedlist \
  -windowtitle 'de.jtem.$(NAME) package API documentation' \
  -header '<a href="http://www.jtem.de/$(NAME)" target="_top">$(NAME)</a> by<br><a href="http://www.jtem.de" target="_top">jTEM</a>' \
  -footer '<a href="http://www.jtem.de/$(NAME)" target="_top">$(NAME)</a> by<br><a href="http://www.jtem.de" target="_top">jTEM</a>' \
  -bottom '<font size=-1><b><a href="mailto:jtem@math.tu-berlin.de?subject=$(NAME):">jTEM</a></b></font>' \
  -link http://java.sun.com/javase/6/docs/api/ \
  $(foreach d, $(DEPNAMES), -link $(JTEMURL)/$(d)/api) \
  -d $(DOCDIR) -classpath "$(BINDIR):`find $(LIBDIR) -name '*.jar' -printf %p: 2> /dev/null `" \
  -sourcepath `echo $(SRCDIRS) | tr \  :` \
  $(DOCPACKAGES)
	   

#things that are removed recursively by the clean target
CLEAN=$(BINDIR) $(DOCDIR) $(WEBDIR) $(RELDIR) .testscompiled `find $(TESTDIR) -name '*.class' 2> /dev/null` \
	$(LIBDIR)/.lastUpdateDepsPlusADay $(LIBDIR)/.lastUpdateDepsCheck

#jtem site url
JTEMURL=http://www.math.tu-berlin.de/jtem



# ---------------------------------------------------------------------
# Everything below should be generic (feel free to adapt to your needs)
# ---------------------------------------------------------------------

SOURCEFILES=$(shell find $(SRCDIRS) -name '*.java')

TESTSOURCEFILES=$(shell find $(TESTDIR) -name '*.java' 2> /dev/null)
ifeq ($(strip $(TESTDIR)),) 
  ALLTESTS=
  et_=
  TESTS=
  ext_= 
else
  ALLTESTS=$(shell  find $(TESTDIR) -name '*Test*.java' 2> /dev/null\
    | sed -e 's,$(TESTDIR)/,,g' -e 's/.java//g' -e 'y,/,.,' )
  et_=$(addprefix %,$(EXCLTESTS:.java=))
  TESTS=$(filter-out $(et_), $(ALLTESTS))
  ext_=$(filter $(et_), $(ALLTESTS))
endif
  
DEPNAMES=$(shell cat dependencies.txt 2> /dev/null | grep -v '^\#' )
DEPS=$(patsubst %,$(LIBDIR)/%.jar, $(DEPNAMES))
DOCPACKAGES=$(shell find $(SRCDIRS) -name '*.java' -printf "%h\n" | \
	sed -e 'y,/,.,' $(foreach d,$(SRCDIRS), -e 's/$(d)\.//') | sort -u)
DOWNLOADDEPS=$(JTEMURL)/downloads

#function to copy to SRVDIR
ifeq ($(strip $(SERVER)),)
  copy_to_website=cp $(1) $(SRVDIR)/$(strip $(2)); echo " - copy \"$(1)\" to \" $(SRVDIR)/$(strip $(2))\" "
else  
  copy_to_website=scp -r $(1) $(SERVER):$(SRVDIR)/$(strip $(2)); echo " - copy \"$(1)\" to \" $(SRVDIR)/$(strip $(2))\" "
endif
#function to execute on SERVER 
ifeq ($(strip $(SERVER)),)
  exec_on_server=$(1) 
else  
  exec_on_server=ssh $(SERVER) "$(1)"
endif


# ---Targets --

.PHONY: help
help:
	@cat README.txt
	@echo "CURRENT VALUES OF SOME VARIABLES"; echo "================================"; echo
	@echo "project name (NAME): $(NAME)"
	@echo "find sources in (SRCDIRS): $(SRCDIRS)"
	@echo "compiled classes in (BINDIR): $(BINDIR)"
	@echo "generat api documentation in (DOCDIR): $(DOCDIR)"
	@echo "generat snippets for the web site in (WEBDIR): $(WEBDIR)"
	@echo "package-summary.html to produce the web snipptes (PACKAGESUMHTML): $(PACKAGESUMHTML)"
	@echo "server of the website - may be empty(SERVER): $(SERVER)"
	@echo "directory of the web site on the server (SRVDIR): $(SRVDIR)"
	@echo "directory for the dependencies - put other archives here too (LIBDIR): $(LIBDIR)"
	@echo "directory of release achives (RELDIR): $(RELDIR)"
	@echo "directory of JUnit tests (TESTDIR): $(TESTDIR)"
	@echo "excluded JUnitTests (EXCLTESTS): $(EXCLTESTS)"
	@echo "directory of compiled JUnit test (TESTBINDIR): $(TESTBINDIR)"
	@echo "junit.jar (JUNIT): $(JUNIT)"
	@echo "jtem url (JTEMURL): $(JTEMURL)"


# --- updatedeps ----
#dependencies will only be checked once a day and wget only fetches them, 
#if the files are newer on the server or have different sizes

.PHONY: updatedeps $(DEPS)
updatedeps: $(DEPS)
$(DEPS): 
	@if [ ! -d $(LIBDIR) ]; then mkdir $(LIBDIR); fi
	@cd $(LIBDIR); \
	touch .lastUpdateDepsCheck; \
	if [ ! -f .lastUpdateDepsPlusADay \
			-o .lastUpdateDepsPlusADay -ot  .lastUpdateDepsCheck \
			$(foreach d, $(subst $(LIBDIR)/,,$(DEPS)), -o ! -f $d) ]; \
		then wget --timestamping $(subst $(LIBDIR), $(DOWNLOADDEPS), $(DEPS)) || echo "ERROR: could not reach jTEM site to fetch dependencies!" ;\
		touch -t `date --date="+1 day" +%Y%m%d%H%M` .lastUpdateDepsPlusADay; \
	fi


# --- binaries ---
#compile the SOURCFILES into BINDIR

.PHONY: binaries
binaries: $(BINDIR)
$(BINDIR): $(SOURCEFILES) | $(DEPS)
	@if [ ! -d $(BINDIR) ]; then mkdir $(BINDIR); fi
	@cp=`find $(LIBDIR) -name '*.jar' -printf %p: 2> /dev/null` ; \
	javac $(JAVACOPTS) \
		`if [ -n "$${cp}" ]; then echo -classpath "$${cp}"; fi` \
		-d $(BINDIR)/ \
		$(SOURCEFILES) || { rm -rf $(BINDIR); echo "ERROR: compilation failed, folder \"$(BINDIR)\" removed"; exit 1; }
	@touch $(BINDIR)
	@echo " - compilation of sources in \"$(SRCDIRS)\" successfull, class files in \"$(BINDIR)\" "
	

# --- test ---
#compile and run JUnit tests form TESTDIR	

.PHONY: test
test: .testscompiled
#only runs tests if $(TESTDIR) is non empty
ifeq ($(strip $(TESTDIR)),)
	@echo "No tests, variable TESTDIR is empty."
else
	@for test in $(TESTS); do \
		echo "- JUnitTest: $$test"; \
		java -ea -classpath "`find $(LIBDIR) -name '*.jar' -printf %p: 2> /dev/null`$(JUNIT):$(BINDIR):$(TESTBINDIR)" \
			junit.textui.TestRunner $$test || { echo "JUnit Test failed!" ; exit 1; } \
		done;
	@if [ -n "$(ext_)" ]; then echo "WARNING: some tests where exluded, see variable EXCLTESTS"; fi
endif

.testscompiled: $(BINDIR) $(TESTSOURCEFILES)
#only compile tests if $(TESTDIR) is non empty
ifneq ($(strip $(TESTDIR)),)
	@if [ ! -d $(TESTBINDIR) ]; then mkdir $(TESTBINDIR); fi
	@javac $(JAVACOPTS) \
		-classpath "`find $(LIBDIR) -name '*.jar' -printf %p: 2> /dev/null`$(JUNIT):$(BINDIR)" \
		-d $(TESTBINDIR)/ \
		$(TESTSOURCEFILES)
	@touch .testscompiled 
	@echo " - compilation of test in \"$(TESTDIR)\" successfull, class files in \"$(TESTBINDIR)\" "
endif


# --- javadoc ---
#generate api documentation with javadoc

.PHONY: javadoc
javadoc: $(DOCDIR)
$(DOCDIR): $(shell find $(SRCDIRS)  -path "*.svn" -prune -o -print ) | $(DEPS) 
	@if [ ! -d $(DOCDIR) ]; then mkdir $(DOCDIR); fi
	@javadoc $(JAVADOCOPTS)
	@touch $(DOCDIR)


# --- web ---
#generate web snippets and put them on the server

.PHONY: web    
web: $(WEBDIR)/teaser.html $(WEBDIR)/content.html 
	@for f in $?; do $(call copy_to_website,$$f,$(NAME)/$${f#$(WEBDIR)}); done
	@if [ -d $(dir $(PACKAGEHTML))/doc-files ]; then $(call copy_to_website,$(dir $(PACKAGEHTML))/doc-files,$(NAME)/doc-files); fi
	@if [ -f  releasenotes.txt ]; then $(call copy_to_website, releasenotes.txt,downloads/$(NAME)); fi
	@-$(call exec_on_server, find $(SRVDIR) -user `whoami` | xargs chmod g+rw)
	
$(WEBDIR)/teaser.html: $(DOCDIR)
	@if [ ! -d $(WEBDIR) ]; then mkdir $(WEBDIR); fi
	@sed -e '0,/teaser start/d;/teaser end/,$$d' $(PACKAGESUMHTML) > $(WEBDIR)/teaser.html
$(WEBDIR)/content.html: $(DOCDIR)
	@if [ ! -d $(WEBDIR) ]; then mkdir $(WEBDIR); fi
	@sed -e '0,/teaser start/d; /START OF BOTTOM NAVBAR/,$$d' \
		-e 's,\(\.\./\)\+,$(JTEMURL)/$(NAME)/api/,g' \
		$(PACKAGESUMHTML) > $(WEBDIR)/content.html


# --- release ---
.PHONY: release
release: web test $(DOCDIR) $(RELDIR)/$(NAME).jar $(RELDIR)/$(NAME).tgz $(RELDIR)/$(NAME).zip $(RELDIR)/$(NAME)-api.tgz $(RELDIR)/current.txt
	@status=`svn status -u 2>&1 | grep -v "?" | head -n -1`; \
		if [ -n "$$status" ]; then \
	    	echo "STOP: Synchronize with repository first!"; exit 1 ;\
	    	else echo "- svn is synchronized"; fi
	@grep `cat $(RELDIR)/current.txt` releasenotes.txt 1>/dev/null 2>&1 || \
		if [ -f releasenotes.txt ]; then mv releasenotes.txt releasenotes-old.txt; fi; \
		cat $(RELDIR)/current.txt > releasenotes.txt; \
		if [ -f releasenotes-old.txt ]; then cat releasenotes-old.txt >> releasenotes.txt; fi
	@$(call copy_to_website, \
		$(RELDIR)/$(NAME).jar $(RELDIR)/$(NAME).tgz $(RELDIR)/$(NAME).zip $(RELDIR)/$(NAME)-api.tgz,downloads)
	@$(call copy_to_website, $(RELDIR)/$(NAME).jar,downloads/$(NAME)/$(NAME)_`cat $(RELDIR)/current.txt`.jar)
	@$(call copy_to_website, $(RELDIR)/$(NAME).tgz,downloads/$(NAME)/$(NAME)_`cat $(RELDIR)/current.txt`.tgz)
	@$(call copy_to_website, $(RELDIR)/$(NAME).zip,downloads/$(NAME)/$(NAME)_`cat $(RELDIR)/current.txt`.zip)
	@$(call copy_to_website, $(RELDIR)/$(NAME)-api.tgz,downloads/$(NAME)/$(NAME)-api_`cat $(RELDIR)/current.txt`.tgz)
	@$(call exec_on_server, if [ -d  $(SRVDIR)/$(NAME)/api ]; then rm -rf $(SRVDIR)/$(NAME)/api; fi)
	$(call copy_to_website, $(DOCDIR),$(NAME)/api)
	@$(call copy_to_website, $(RELDIR)/current.txt,downloads/$(NAME))
	@$(call copy_to_website, releasenotes.txt,downloads/$(NAME))
	@-$(call exec_on_server, find $(SRVDIR) -user `whoami` | xargs chmod g+rw)
	@echo " - release `cat rel/current.txt` succesfully deployed."

#jar of compiled classes
$(RELDIR)/$(NAME).jar: $(BINDIR) $(RELDIR)/manifest.txt
	@if [ ! -d $(RELDIR) ]; then mkdir $(RELDIR); fi
	@jar cmf $(RELDIR)/manifest.txt $(RELDIR)/$(NAME).jar -C $(BINDIR) .

#archive of source and jars of all dependencies
$(RELDIR)/$(NAME).tgz:  $(shell find $(SRCDIRS)) | updatedeps 
	@if [ ! -d $(RELDIR) ]; then mkdir $(RELDIR); fi
	@tar czf $(RELDIR)/$(NAME).tgz $(SRCDIRS) $(LIBDIR) --exclude-vcs
	
#jar and jars of all dependencies
$(RELDIR)/$(NAME).zip:  $(RELDIR)/$(NAME).jar | updatedeps 
	@if [ ! -d $(RELDIR) ]; then mkdir $(RELDIR); fi
	@cd $(RELDIR); zip $(NAME).zip $(NAME).jar 
	@if [ ! $(LIBDIR)="" -a -d $(LIBDIR) ]; then cd $(LIBDIR); zip -g ../$(RELDIR)/$(NAME).zip *; fi
	
#archive of api-documentation
$(RELDIR)/$(NAME)-api.tgz:  $(DOCDIR)
	@if [ ! -d $(RELDIR) ]; then mkdir $(RELDIR); fi
	@tar czf $(RELDIR)/$(NAME)-api.tgz $(DOCDIR) --exclude-vcs

#Manifest
$(RELDIR)/manifest.txt: $(RELDIR)/current.txt
	@if [ ! -d $(RELDIR) ]; then mkdir $(RELDIR); fi
	@echo "Implementation-Title: $(NAME)" > $(RELDIR)/manifest.txt
	@echo "Implementation-Version: "`cat $(RELDIR)/current.txt` >> $(RELDIR)/manifest.txt
	@echo "Implementation-Vendor: jTEM ($(JTEMURL))" >> $(RELDIR)/manifest.txt
	@echo "Implementation-URL: $(JTEMURL)/downloads/$(NAME).jar" >> $(RELDIR)/manifest.txt
	
#release date
.PHONY: $(RELDIR)/current.txt
$(RELDIR)/current.txt:
	@if [ ! -d $(RELDIR) ]; then mkdir $(RELDIR); fi
	@echo `date +%F`_rev`svn info --xml -r HEAD  \
		| sed -n -e '/<entry/,/>/p' \
		| sed -n -e '/revision/s/.*"\([0-9]*\)".*/\1/p'`\
		> $(RELDIR)/current.txt


# --- debug ---
.PHONY: debug
debug:
	@echo SOURCEFILES=$(SOURCEFILES); echo 
	@echo CLEAN=$(CLEAN); echo
	@echo TESTSOURCEFILES=$(TESTSOURCEFILES); echo 
	@echo TESTS=$(TESTS); echo
	@echo JUNIT=$(JUNIT); echo
	@echo DOCPACKAGES=$(DOCPACKAGES); echo
	@echo PACKAGESUMHTML=$(PACKAGESUMHTML); echo
	@echo PACKAGEHTML=$(PACKAGEHTML); echo
	@echo DEPS=$(DEPS); echo

.PHONY: clean
clean:
	@for f in $(CLEAN); do if [ -e $$f ]; then rm -rf $$f; fi; done
