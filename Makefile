ezcode-lc3.jar: bin/*.class
	jar cfe ezcode-lc3.jar Tester -C bin .

bin/*.class: src/*.java
	javac -d bin src/*.java
