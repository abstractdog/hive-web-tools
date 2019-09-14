mvn clean install -DskipTests
mvn spring-boot:run -Dhive.tools.classpath=$(mvn -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath")