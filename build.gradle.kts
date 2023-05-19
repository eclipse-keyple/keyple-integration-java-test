///////////////////////////////////////////////////////////////////////////////
//  GRADLE CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
plugins {
    java
    id("com.diffplug.spotless") version "5.10.2"
}
buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://repo.eclipse.org/service/local/repositories/maven_central/content")
        mavenCentral()
    }
    dependencies {
        classpath("org.eclipse.keyple:keyple-gradle:0.2.+") { isChanging = true }
    }
}
apply(plugin = "org.eclipse.keyple")

///////////////////////////////////////////////////////////////////////////////
//  APP CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
repositories {
    mavenLocal()
    maven(url = "https://repo.eclipse.org/service/local/repositories/maven_central/content")
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots")
}
dependencies {
    testImplementation("org.calypsonet.terminal:calypsonet-terminal-reader-java-api:1.3.0-SNAPSHOT") {isChanging=true}
    testImplementation("org.calypsonet.terminal:calypsonet-terminal-calypso-java-api:1.8.0")
    testImplementation("org.eclipse.keyple:keyple-common-java-api:2.0.0")
    testImplementation("org.eclipse.keyple:keyple-distributed-network-java-lib:2.2.0")
    testImplementation("org.eclipse.keyple:keyple-distributed-local-java-lib:2.2.0")
    testImplementation("org.eclipse.keyple:keyple-distributed-remote-java-lib:2.2.1")
    testImplementation("org.eclipse.keyple:keyple-service-java-lib:2.2.2-SNAPSHOT") {isChanging=true}
    testImplementation("org.eclipse.keyple:keyple-service-resource-java-lib:2.1.1")
    testImplementation("org.eclipse.keyple:keyple-plugin-cardresource-java-lib:1.0.1")
    testImplementation("org.eclipse.keyple:keyple-plugin-stub-java-lib:2.1.0")
    testImplementation("org.eclipse.keyple:keyple-card-calypso-java-lib:2.3.5")
    testImplementation("org.eclipse.keyple:keyple-card-generic-java-lib:2.0.2")
    testImplementation("org.eclipse.keyple:keyple-util-java-lib:2.3.0")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.slf4j:slf4j-api:2.0.5")
    testImplementation("org.slf4j:slf4j-simple:2.0.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
}

val javaSourceLevel: String by project
val javaTargetLevel: String by project
java {
    sourceCompatibility = JavaVersion.toVersion(javaSourceLevel)
    targetCompatibility = JavaVersion.toVersion(javaTargetLevel)
    println("Compiling Java $sourceCompatibility to Java $targetCompatibility.")
}

///////////////////////////////////////////////////////////////////////////////
//  TASKS CONFIGURATION
///////////////////////////////////////////////////////////////////////////////
tasks {
    spotless {
        java {
            target("src/**/*.java")
            licenseHeaderFile("${project.rootDir}/LICENSE_HEADER")
            importOrder("java", "javax", "org", "com", "")
            removeUnusedImports()
            googleJavaFormat()
        }
    }
    test {
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
