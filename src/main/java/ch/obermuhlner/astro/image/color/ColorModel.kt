package ch.obermuhlner.astro.image.color

interface ColorModel {
    object RGB : ColorModel {
        const val R = 0
        const val G = 1
        const val B = 2
    }

    object HSV : ColorModel {
        const val H = 0
        const val S = 1
        const val V = 2
    }
}