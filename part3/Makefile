GS = -g
JC = javac -classpath /usr/share/java/db-5.3.28.jar

.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Main_States.java \
	Query_Type.java \
	Formats.java \
	OpenDB.java \
	Driver.java \
	Main.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
