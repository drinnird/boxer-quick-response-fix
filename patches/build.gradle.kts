group = "app.boxerpatches"

patches {
    about {
        name = "Boxer Quick Response Fixes"
        description = "Fixes Quick Response blank-line formatting in Workspace ONE Boxer."
        source = "local"
        author = "Community patch"
        contact = "na"
        website = "na"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}
