plugins {
    id("java")
    id("application")
    id("com.diffplug.spotless") version "6.22.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.3")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.24.2")

}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "com.timholdaway.CsvProcessorApplication"
}

tasks.withType<JavaExec>() {
    standardInput = System.`in`
}

spotless {
    format("misc") {
        target("*.gradle", "*.md", ".gitignore")
        trimTrailingWhitespace()
        indentWithTabs() // or spaces. Takes an integer argument if you don't like 4
        endWithNewline()
    }
    java {
        googleJavaFormat().aosp()
        formatAnnotations()
        licenseHeader("/* (C)2023 Tim Holdaway */")
    }
}