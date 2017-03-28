
install:
	./gradlew install

publish: clean
ifndef VERSION
	$(error VERSION is undefined for Newton Release)
endif
	echo "#DO NOT EDIT MANUALLY. Change in Makefile">gradle.properties
	echo version=$(VERSION)>>gradle.properties
	echo group=io.muoncore.newton>>gradle.properties
	echo exclude=>>gradle.properties
	#./gradlew artifactoryPublish
	#git add gradle.properties
	#git commit -m "Update version to $(VERSION )while publishing"
	#git push origin

test:
	SHORT_TEST=true ./gradlew check

clean:
	./gradlew clean

testextended:
	./gradlew check

