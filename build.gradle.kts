plugins {
    java
    id("org.springframework.boot") version "3.3.0-SNAPSHOT"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.nechost"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://m2.dv8tion.net/releases") }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.projectlombok:lombok")
    compileOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
    testImplementation("org.springframework.security:spring-security-test")
    implementation("io.jsonwebtoken:jjwt:0.12.5")
    runtimeOnly("com.mysql:mysql-connector-j")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("net.dv8tion:JDA:5.0.0-beta.3")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
