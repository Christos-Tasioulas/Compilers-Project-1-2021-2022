all: compile

compile:
	jflex scanner.flex
	java -jar ../java-cup-11b.jar -interface -parser Parser parser.cup
	javac -cp ../java-cup-11b-runtime.jar *.java

execute1:
	java -cp ../java-cup-11b-runtime.jar:. Parsexe input_example1

execute2:	
	java -cp ../java-cup-11b-runtime.jar:. Parsexe input_example2

execute3:	
	java -cp ../java-cup-11b-runtime.jar:. Parsexe input_example3

clean:
	rm -f *.class *~
	rm -f Scanner.java
	rm -f Parser.java
	rm -f sym.java
	rm -f Main.java
