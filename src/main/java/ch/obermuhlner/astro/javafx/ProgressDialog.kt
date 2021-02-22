package ch.obermuhlner.astro.javafx

import javafx.concurrent.Task
import javafx.concurrent.WorkerStateEvent
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.BorderPane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle

object ProgressDialog {
    fun show(title: String?, message: String?, runnable: Runnable) {
        runnable.run()
    }

    fun showREALLY(title: String?, message: String?, runnable: Runnable) {
        val dialogStage = Stage()
        dialogStage.initStyle(StageStyle.UTILITY)
        dialogStage.isResizable = false
        dialogStage.initModality(Modality.APPLICATION_MODAL)
        dialogStage.title = title
        val borderPane = BorderPane()
        val label = Label(message)
        label.isWrapText = true
        label.prefWidth = 360.0
        borderPane.top = label
        val progress = ProgressIndicator(-1.0)
        borderPane.center = progress
        val scene = Scene(borderPane)
        dialogStage.scene = scene
        dialogStage.show()
        val task: Task<Void?> = object : Task<Void?>() {
            @Throws(Exception::class)
            override fun call(): Void? {
                runnable.run()
                return null
            }
        }
        task.onSucceeded = EventHandler { event: WorkerStateEvent? -> dialogStage.close() }
        Thread(task).start()
    }
}