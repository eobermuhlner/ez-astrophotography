package ch.obermuhlner.astro.gradient.filter

import ch.obermuhlner.astro.image.ArrayDoubleImage
import ch.obermuhlner.astro.image.DoubleImage
import ch.obermuhlner.astro.image.SubDoubleImage

open interface Filter {
    fun filter(source: DoubleImage, target: DoubleImage, width: Int, height: Int): DoubleImage

    fun filter(source: DoubleImage, target: DoubleImage): DoubleImage {
        val width: Int = Math.min(source.width, target.width)
        val height: Int = Math.min(source.height, target.height)
        return filter(source, target, width, height)
    }

    fun filter(source: DoubleImage): DoubleImage {
        val target = ArrayDoubleImage(source.width, source.height, source.colorModel)
        return filter(source, target)
    }

    fun filter(source: DoubleImage, sourceX: Int, sourceY: Int, target: DoubleImage, targetX: Int, targetY: Int, width: Int, height: Int): DoubleImage {
        val subSource = SubDoubleImage(source, sourceX, sourceY, width, height)
        val subTarget = SubDoubleImage(target, targetX, targetY, width, height)
        return filter(subSource, subTarget)
    }
}