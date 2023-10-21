plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

//application {
//    mainClassName =  if (project.hasProperty("mainClass")) project.getProperty("mainClass") else "NULL"
//}

application {
    mainClass = "com.timholdaway.Main"
}