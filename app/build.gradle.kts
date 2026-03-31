import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.google.firebase.crashlytics")
    id("dagger.hilt.android.plugin")
    id("suitepad-gradletools-repos")
    // id("suitepad-gradletools-signing") // Uncomment if needed
}

apply(from = "../release.gradle")

fun getDestinationEmailAddress(): String {
    val properties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }
    return properties.getProperty("sendgrid.mailto") ?: ""
}

fun getSendgridApiKey(): String {
    val properties = Properties().apply {
        load(project.rootProject.file("local.properties").inputStream())
    }
    return properties.getProperty("sendgrid.apikey") ?: ""
}

android {
    namespace = "de.suitepad.linbridge"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = getPropOrEnv("sign.release.keystore")?.let { file(it) }
            storePassword = getPropOrEnv("sign.release.keystorepass")
            keyAlias = getPropOrEnv("sign.release.keyalias")
            keyPassword = getPropOrEnv("sign.release.keypass")
        }
    }

    defaultConfig {
        applicationId = "de.suitepad.linbridge"
        minSdk = 23
        targetSdk = 33

        versionCode = 2010040
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SENDGRID_MAIL_TO", "\"${getDestinationEmailAddress()}\"")
        buildConfigField("String", "SENDGRID_API_KEY", "\"${getSendgridApiKey()}\"")
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            isTestCoverageEnabled = false
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
            isTestCoverageEnabled = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

repositories {
    maven {
        setUrl("suitepad") // Assuming this replaces `mavenRemote { load "suitepad" }`
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.0.21")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
    implementation("de.suitepad:linbridge-api:1.1.4")
    implementation("dnsjava:dnsjava:3.6.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.github.danysantiago:sendgrid-android:1") {
        exclude(group = "org.apache.httpcomponents.httpclient-android")
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }

    // Dagger dependencies
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:27.1.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx:17.1.0")
    //implementation("org.linphone:linphone-sdk-android:5.5+")
    implementation("org.linphone.no-video:linphone-sdk-android:5.4.1")
}

apply(plugin = "com.google.gms.google-services")

fun getPropOrEnv(propName: String) =
    gradleLocalProperties(rootDir).getProperty(propName)
        ?: System.getenv(propName.toUpperCase().replace('.', '_'))
