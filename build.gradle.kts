buildscript {
    val kotlin_version by extra("2.0.21")
    val coroutine_version by extra("1.6.2")
    val hilt_version by extra("2.52")
    val agpVersion by extra("8.0.0")
    val agpVersion1 by extra("7.0.0")
    val agpVersion2 by extra("8.1.0")

    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven { setUrl("https://suitepad.mycloudrepo.io/public/repositories/public") }
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("de.suitepad:gradletools:1.2.0-SNAPSHOT")
        classpath("com.google.firebase:firebase-crashlytics-gradle:3.0.3")
        classpath("com.google.gms:google-services:4.4.2")
    }
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven { setUrl("https://linphone.org/maven_repository/") }
        maven { setUrl("https://suitepad.mycloudrepo.io/public/repositories/public") }
    }
}

