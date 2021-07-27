@file:JvmName("Base64")

package android.util

fun decode(input: ByteArray, flag: Int): ByteArray {
    return java.util.Base64.getDecoder().decode(input)
}

fun decode(input: String, flag: Int): ByteArray {
    return when (flag) {
        0 -> java.util.Base64.getDecoder().decode(input)
        8 -> java.util.Base64.getUrlDecoder().decode(input)
        else -> java.util.Base64.getUrlDecoder().decode(input)
    }
}
