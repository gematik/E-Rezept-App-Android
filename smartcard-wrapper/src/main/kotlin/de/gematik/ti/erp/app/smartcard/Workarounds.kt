package de.gematik.ti.erp.app.smartcard

internal object Workarounds {
    // workaround for https://bugs.openjdk.java.net/browse/JDK-8255877
    fun `workaround for MacOSX Big Sur - PCSC not found bug`() {
        val javaVersion = System.getProperty("java.version")
        val majorJavaVersion = javaVersion.substring(0, javaVersion.indexOf('.')).toInt()
        val osVersion = System.getProperty("os.version")
        val osName = System.getProperty("os.name")
        val majorOsVersion = osVersion.substring(0, osVersion.indexOf('.')).toInt()
        if (osName == "Mac OS X" && majorJavaVersion <= 16 && majorOsVersion == 11) {
            System.setProperty(
                "sun.security.smartcardio.library",
                "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC"
            )
        }
    }
}
