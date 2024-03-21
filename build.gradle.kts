group = "io.github.grahamdaley"
version = "3.0.0"
object Meta {
    const val NAME = "lambda-runtime-graalvm"
    const val DESC = "Based on FormKiQ Lambda Runtime Graalvm by Mike Friesen"
    const val LICENSE = "Apache-2.0"
    const val GITHUB_REPO = "grahamdaley/lambda-runtime-graalvm"
    const val RELEASE = "https://s01.oss.sonatype.org/service/local/"
    const val SNAPSHOT = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    java
    `maven-publish`
    signing
    checkstyle
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.spotless)
    alias(libs.plugins.versions)
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
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

artifacts {
    archives(tasks.named("jar"))
    archives(tasks.named("javadocJar"))
    archives(tasks.named("sourcesJar"))
}

checkstyle {
    toolVersion = "8.29"
    configFile = file("config/checkstyle/checkstyle.xml")
    configProperties = mapOf("project_loc" to projectDir)
    maxWarnings = 0
}

spotbugs {
    excludeFilter = file("$rootDir/config/spotbugs/spotbugs-exclude.xml")
}

spotless {
    java {
        googleJavaFormat()
    }
}

signing {
    val signingKey =
        providers
            .environmentVariable("GPG_SIGNING_KEY")
            .orElse(providers.gradleProperty("gpg.key"))
    val signingPassphrase =
        providers
            .environmentVariable("GPG_SIGNING_PASSPHRASE")
            .orElse(providers.gradleProperty("gpg.passphrase"))

    if (signingKey.isPresent && signingPassphrase.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPassphrase.get())
        val extension = extensions.getByName("publishing") as PublishingExtension
        sign(extension.publications)
    }
}

publishing {
    publications {
        create<MavenPublication>(Meta.NAME) {
            groupId = project.group as String
            artifactId = Meta.NAME
            version = project.version as String
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            pom {
                name.set("Lambda Runtime Graalvm")
                description.set("Based on FormKiQ Lambda Runtime Graalvm by Mike Friesen")
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
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri(Meta.RELEASE))
            snapshotRepositoryUrl.set(uri(Meta.SNAPSHOT))
            val ossrhUsername =
                providers
                    .environmentVariable("OSSRH_USERNAME")
                    .orElse(providers.gradleProperty("ossrh.username"))
            val ossrhPassword =
                providers
                    .environmentVariable("OSSRH_PASSWORD")
                    .orElse(providers.gradleProperty("ossrh.password"))
            if (ossrhUsername.isPresent && ossrhPassword.isPresent) {
                username.set(ossrhUsername.get())
                password.set(ossrhPassword.get())
            }
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

    named("check") {
        dependsOn("publishToMavenLocal")
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
