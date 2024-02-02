import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
	val kotlinVersion="1.9.22"
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion
	kotlin("plugin.allopen") version kotlinVersion
}


configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

allprojects {
	group = "io.philo"
	version = "0.0.1-SNAPSHOT"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "java")
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.springframework.boot")
	apply(plugin = "io.spring.dependency-management")
	apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	apply(plugin = "org.jetbrains.kotlin.plugin.allopen")

	java.sourceCompatibility = JavaVersion.VERSION_17
	extra["springCloudVersion"] = "2023.0.0"

	dependencies {
		//  Kotlin
		implementation("org.springframework.boot:spring-boot-starter-amqp")
		implementation("org.springframework.boot:spring-boot-starter-web")
		implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		implementation("io.github.microutils:kotlin-logging:3.0.5")

		annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

		runtimeOnly("com.h2database:h2")

		implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.1.0")

		implementation("org.springframework.boot:spring-boot-starter-actuator:3.2.2")


		implementation("org.springframework.boot:spring-boot-starter-amqp")
		implementation("org.springframework.cloud:spring-cloud-starter-bus-amqp:4.1.0")
//		implementation("org.springframework.cloud:spring-cloud-starter-config")

		//  Test
		testImplementation("org.springframework.boot:spring-boot-starter-test")
		testImplementation("org.springframework.amqp:spring-rabbit-test")
	}

	dependencyManagement {
		imports {
			mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		}
	}

	tasks.withType<KotlinCompile> {
		kotlinOptions {
			freeCompilerArgs += "-Xjsr305=strict"
			jvmTarget = "17"
		}
	}

	tasks.withType<Test> {
		useJUnitPlatform()
	}
}

project("eureka-client") {
	dependencies {
		implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
		implementation(project(":common"))
	}
}


project("eureka") {
	dependencies {
		implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server")
		implementation(project(":common"))
	}
}

project("gateway") {
	dependencies {
		implementation("org.springframework.cloud:spring-cloud-starter-gateway")
		implementation(project(":eureka-client"))
		implementation(project(":common"))
	}
}

project("ms-1") {
	dependencies {
		implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
		implementation(project(":eureka-client"))
		implementation(project(":common"))
	}
}

project("ms-2") {
	dependencies {
		implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
		implementation(project(":eureka-client"))
		implementation(project(":common"))
	}
}

