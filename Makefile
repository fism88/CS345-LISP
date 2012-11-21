Parser.class: Parser.java
	javac *java
	jar cmf mainClass Parser.jar *class
	jar tf Parser.jar

Parser.java: Parser.jj 
	javacc Parser.jj

clean:
	rm *.class Parser.java ParserConstants.java ParserTokenManager.java ParseException.java SimpleCharStream.java Token.java TokenMgrError.java Parser.jar

