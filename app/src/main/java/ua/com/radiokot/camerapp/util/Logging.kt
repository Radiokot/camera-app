package ua.com.radiokot.camerapp.util

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

fun Any.lazyLogger(name: String): Lazy<KLogger> = lazy {
    KotlinLogging.logger("$name@${Integer.toHexString(hashCode())}")
}
