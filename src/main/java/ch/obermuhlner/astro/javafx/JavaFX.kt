package ch.obermuhlner.astro.javafx

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle

fun hbox(initializer: HBox.() -> Unit): HBox
    = HBox().apply(initializer)


fun hbox(spacing: Double, initializer: HBox.() -> Unit): HBox
    = HBox(spacing).apply(initializer)


fun vbox(initializer: VBox.() -> Unit): VBox
        = VBox().apply(initializer)

fun vbox(spacing: Double, initializer: VBox.() -> Unit): VBox
        = VBox(spacing).apply(initializer)


fun button(initializer: Button.() -> Unit): Button
    = Button().apply(initializer)


fun button(text: String, initializer: Button.() -> Unit): Button
    = Button(text).apply(initializer)


fun rectangle(width: Double, height: Double, initializer: Rectangle.() -> Unit): Rectangle
    = Rectangle(width, height).apply(initializer)

fun circle(radius: Double, initializer: Circle.() -> Unit): Circle
    = Circle(radius).apply(initializer)


