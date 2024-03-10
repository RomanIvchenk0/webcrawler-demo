plugins {
    java
    id("org.springframework.boot") version "3.2.3" apply false
    id("io.spring.dependency-management") version "1.1.4"
}

allprojects {
    group = "com.github.roman"
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply {
        plugin("org.springframework.boot")
        plugin("io.spring.dependency-management")
        plugin("java")
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}
