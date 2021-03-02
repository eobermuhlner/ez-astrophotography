package ch.obermuhlner.astro.javafx

import ch.obermuhlner.astro.javafx.glow.GlowRemovalApp
import ch.obermuhlner.astro.javafx.stack.ImageStackerApp
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.stage.Stage

class AstrophotographyApp : Application() {
    override fun start(primaryStage: Stage) {
        val root = Group()
        val scene = Scene(root)

        root.children += vbox {
            children += button("Stack Images") {
                onAction = EventHandler {
                    ImageStackerApp().show()
                }
            }
            children += button("Glow Removal") {
                onAction = EventHandler {
                    GlowRemovalApp().show()
                }
            }
        }

        primaryStage.scene = scene
        primaryStage.show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(AstrophotographyApp::class.java)
        }
    }
}