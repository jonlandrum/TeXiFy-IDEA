import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

// Include the Gradle plugins which help building everything.
// Supersedes the use of "buildscript" block and "apply plugin:"
plugins {
    id("org.jetbrains.intellij") version "0.6.4"
    kotlin("jvm") version("1.4.30-M1")

    // Plugin which can check for Gradle dependencies, use the help/dependencyUpdates task.
    id("com.github.ben-manes.versions") version "0.35.0"

    // Plugin which can update Gradle dependencies, use the help/useLatestVersions task.
    id("se.patrikerdes.use-latest-versions") version "0.2.15"

    // Used to debug in a different IDE
    maven
    id("de.undercouch.download") version "4.1.1"

    // Test coverage
    jacoco

    // Linting
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
}

group = "nl.hannahsten"
version = "0.7.2-rc2"

repositories {
    mavenCentral()
}

sourceSets {
    getByName("main").apply {
        java.srcDirs("src", "gen")
        resources.srcDirs("resources")
    }

    getByName("test").apply {
        java.srcDirs("test")
        resources.srcDirs("test/resources")
    }
}

// Java target version
java.sourceCompatibility = JavaVersion.VERSION_1_8

// Specify the right jvm target for Kotlin
tasks.compileKotlin {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

// Same for Kotlin tests
tasks.compileTestKotlin {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()

    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=enable")
    }
}

dependencies {
    // Local dependencies
    implementation(files("lib/pretty-tools-JDDE-2.1.0.jar"))
    implementation(files("lib/JavaDDE.dll"))
    implementation(files("lib/JavaDDEx64.dll"))

    // From Kotlin documentation
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib")

    // D-Bus Java bindings
    implementation("com.github.hypfvieh:dbus-java:3.2.3")
    implementation("org.slf4j:slf4j-simple:2.0.0-alpha1")

    // Test dependencies

    // Also implementation junit 4, just in case
    testImplementation("junit:junit:4.13.1")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.7.0")

    // Use junit 5 for test cases
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")

    // Enable use of the JUnitPlatform Runner within the IDE
    testImplementation("org.junit.platform:junit-platform-runner:1.7.0")

    // just in case
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime")

    testImplementation("io.mockk:mockk:1.10.2")

    // Add custom ruleset from github.com/slideclimb/ktlint-ruleset
    ktlintRuleset(files("lib/ktlint-ruleset-0.1.jar"))
}

// Special resource dependencies
tasks.processResources {
    from("lib") {
        include("pretty-tools-JDDE-2.1.0.jar")
        include("JavaDDE.dll")
        include("JavaDDEx64.dll")
    }
}

intellij {
    pluginName = "TeXiFy-IDEA"

    setPlugins("tanvd.grazi", "java")

    // Use the since build number from plugin.xml
    updateSinceUntilBuild = false
    // Keep an open until build, to avoid automatic downgrades to very old versions of the plugin
    sameSinceUntilBuild = true

    // Comment out to use the latest EAP snapshot
    // Docs: https://github.com/JetBrains/gradle-intellij-plugin#intellij-platform-properties
    // All snapshot versions: https://www.jetbrains.com/intellij-repository/snapshots/
    version = "2020.3"
//    version = "PY-203.5419.8-EAP-SNAPSHOT"
//    type = "PY"

    // Example to use a different, locally installed, IDE
    // If you get the error "Cannot find builtin plugin java for IDE", remove the "java" plugin above
    // Also disable "version" above
    // If it doesn't work (Could not resolve all files for configuration ':detachedConfiguration4'.), specify 'version' instead
//    localPath = "/home/thomas/.local/share/JetBrains/Toolbox/apps/PyCharm-P/ch-1/203.5419.8/"
}

// Allow publishing to the Jetbrains repo via a Gradle task
// This requires to put a Jetbrains Hub token, see http://www.jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html for more details
// Generate a Hub token at https://hub.jetbrains.com/users/me?tab=authentification
// You should provide it either via environment variables (ORG_GRADLE_PROJECT_intellijPublishToken) or Gradle task parameters (-Dorg.gradle.project.intellijPublishToken=mytoken)
tasks.publishPlugin {
    token(properties["intellijPublishToken"])

    // Specify channel as per the tutorial.
    // More documentation: https://github.com/JetBrains/gradle-intellij-plugin/blob/master/README.md#publishing-dsl
    channels("alpha")
}

tasks.test {
    // For MockK. Make sure it finds the libattach.so in the lib folder.
    jvmArgs = listOf("-Djdk.attach.allowAttachSelf=true", "-Djava.library.path=lib/")
    // Enable JUnit 5 (Gradle 4.6+).
    useJUnitPlatform()
    // Show test results
    testLogging {
        events(TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// Test coverage reporting
tasks.jacocoTestReport {
    // Enable xml for codecov
    reports {
        html.isEnabled = true
        xml.isEnabled = true
        xml.destination = file("$buildDir/reports/jacoco/test/jacocoTestReport.xml")
    }

    sourceSets(project.sourceSets.getByName("main"))
}

ktlint {
    verbose.set(true)
}