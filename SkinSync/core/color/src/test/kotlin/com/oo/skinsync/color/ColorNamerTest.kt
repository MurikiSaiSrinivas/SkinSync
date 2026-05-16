package com.oo.skinsync.color

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorNamerTest {

    @Test fun red() = assertEquals("red", ColorNamer.name(0xFFD32F2F.toInt()))
    @Test fun green() = assertEquals("green", ColorNamer.name(0xFF2E7D32.toInt()))
    @Test fun blue() = assertEquals("blue", ColorNamer.name(0xFF1565C0.toInt()))
    @Test fun white() = assertEquals("white", ColorNamer.name(0xFFFFFFFF.toInt()))
    @Test fun black() = assertEquals("black", ColorNamer.name(0xFF000000.toInt()))
    @Test fun grey() = assertEquals("grey", ColorNamer.name(0xFF808080.toInt()))
}
