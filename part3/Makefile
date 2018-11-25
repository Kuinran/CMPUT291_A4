GS = -g
JC = javac

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Driver.java \
	Main_States.java \
	Main.java \
	Formats.java \
	OpenDB.java \
	Query_Type.java

CLASSPATH = /usr/share/java/db-5.3.28.jar

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
