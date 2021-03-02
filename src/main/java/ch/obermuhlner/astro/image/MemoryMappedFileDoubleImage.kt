package ch.obermuhlner.astro.image

import java.io.File
import java.nio.ShortBuffer
import java.nio.channels.FileChannel
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class MemoryMappedFileDoubleImage(override val width: Int, override val height: Int, val file: File) : DoubleImage {

    private val channelsPerPixel = 3

    private val shortBuffer: ShortBuffer

    init {
        val n = width * height
        val bufferSize = n.toLong() * 2 * channelsPerPixel + 2 * 2
        val channel: FileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE) as FileChannel
        val buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize)
        shortBuffer = buffer.asShortBuffer()

        shortBuffer.put(width.toShort())
        shortBuffer.put(height.toShort())
    }

    override fun getNativePixel(x: Int, y: Int, color: DoubleArray): DoubleArray {
        val index = (x + y * width) * channelsPerPixel + 2
        shortBuffer.position(index)
        color[0] = toDouble(shortBuffer.get())
        color[1] = toDouble(shortBuffer.get())
        color[2] = toDouble(shortBuffer.get())
        return color
    }

    override fun setNativePixel(x: Int, y: Int, color: DoubleArray) {
        val index = (x + y * width) * channelsPerPixel + 2
        shortBuffer.position(index)
        shortBuffer.put(toShort(color[0]))
        shortBuffer.put(toShort(color[1]))
        shortBuffer.put(toShort(color[2]))
    }

    private fun toDouble(value: Short): Double {
        return value / Short.MAX_VALUE.toDouble()
    }
    
    private fun toShort(value: Double): Short {
        return when {
            value <= 0 -> {
                0
            }
            value >= 1.0 -> {
                Short.MAX_VALUE
            }
            else -> {
                (value * Short.MAX_VALUE).toInt().toShort()
            }
        }
    }

    companion object {
        fun fromFile(file: File): MemoryMappedFileDoubleImage {
            val channel: FileChannel = Files.newByteChannel(file.toPath(), StandardOpenOption.CREATE, StandardOpenOption.READ) as FileChannel
            val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, 2 * 2)
            val shortBuffer = buffer.asShortBuffer()
            val width = shortBuffer.get().toInt()
            val height = shortBuffer.get().toInt()

            return MemoryMappedFileDoubleImage(width, height, file)
        }

        fun fromImage(image: DoubleImage, file: File): MemoryMappedFileDoubleImage {
            val mmiImage = MemoryMappedFileDoubleImage(image.width, image.height, file)
            mmiImage.setPixels(image)
            return mmiImage
        }
    }
}