If you are using tomcat, you need to define the following property in your connector:
useBodyEncodingForURI="true"

Also, depending on your SO encoding, you'll have to define some initializion properties for tomcat.
On Ubuntu, that would be adding the following line on the catalina.sh file:
JAVA_OPTS="$JAVA_OPTS -Djavax.servlet.request.encoding=windows-1252 -Dfile.encoding=windows-1252"