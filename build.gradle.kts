import com.vanniktech.maven.publish.SonatypeHost

group = "io.github.grahamdaley"
version = "3.0.1-SNAPSHOT"

object Meta {
    const val NAME = "lambda-runtime-graalvm"
    const val DESC = "Based on FormKiQ Lambda Runtime Graalvm by Mike Friesen"
    const val LICENSE = "Apache-2.0"
    const val GITHUB_REPO = "grahamdaley/lambda-runtime-graalvm"
}

repositories {
    mavenCentral()
}

plugins {
    java
    checkstyle
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.spotless)
}

dependencies {
    implementation(libs.aws.lambda.core)
    implementation(libs.gson)

    testImplementation(libs.aws.lambda.events)
    testImplementation(libs.junit)
    testImplementation(libs.mock.server.netty)
    testImplementation(libs.slf4j.simple)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

checkstyle {
    toolVersion = "10.14.2"
    configFile = file("${rootDir.path}/config/checkstyle/checkstyle.xml")
    configProperties = mapOf("project_loc" to projectDir.path)
    maxWarnings = 0
}

spotbugs {
    excludeFilter = file("${rootDir.path}/config/spotbugs/spotbugs-exclude.xml")
}

spotless {
    java {
        googleJavaFormat()
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01, automaticRelease = true)
    signAllPublications()
    coordinates(project.group.toString(), rootProject.name, project.version.toString())

    pom {
        name.set("Lambda Runtime Graalvm")
        description.set(Meta.DESC)
        inceptionYear.set("2024")
        url.set("https://github.com/${Meta.GITHUB_REPO}")
        licenses {
            license {
                name.set(Meta.LICENSE)
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        developers {
            developer {
                id.set("mfriesen")
                name.set("Mike Friesen")
                email.set("mike@formkiq.com")
            }
            developer {
                id.set("grahamdaley")
                name.set("Graham Daley")
                email.set("graham@daleybread.com")
            }
        }
        scm {
            url.set("https://github.com/${Meta.GITHUB_REPO}.git")
            connection.set("scm:git:git://github.com/${Meta.GITHUB_REPO}.git")
            developerConnection.set("scm:git:git://github.com/#${Meta.GITHUB_REPO}.git")
        }
        issueManagement {
            url.set("https://github.com/${Meta.GITHUB_REPO}/issues")
        }
    }
}

tasks {
    spotbugsMain {
        reports.create("html") {
            required = true
            outputLocation = file(layout.buildDirectory.dir("reports/spotbugs"))
            setStylesheet("fancy-hist.xsl")
        }
    }

    named("checkstyleMain").configure {
        dependsOn("spotlessApply")
    }

    configureEach {
        if (name == "generateMetadataFileForMavenPublication") {
            mustRunAfter("plainJavadocJar")
        }
    }

    withType<Javadoc> {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}

afterEvaluate {
    tasks.named("spotlessCheck").configure {
        dependsOn("spotlessApply")
    }
}
