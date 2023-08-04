tasks {
    shadowJar {
        relocate("com.google.protobuf", "eu.kennytv.maintenance.lib.protobuf")
        relocate("org.mariadb", "eu.kennytv.maintenance.lib.mariadb")
    }
}

dependencies {
    implementation(projects.maintenanceCoreProxy)
    implementation(libs.mariadbConnector)
    implementation(libs.bstatsVelocity)
    compileOnly(libs.velocity)
    annotationProcessor(libs.velocity)
}
