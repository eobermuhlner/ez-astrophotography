package ch.obermuhlner.astro.javafx

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.shape.Circle
import javafx.scene.shape.Rectangle

fun <T: Node> node(node: T, initializer: T.() -> Unit)
        = node.apply(initializer)


fun hbox(initializer: HBox.() -> Unit)
        = HBox().apply(initializer)


fun hbox(spacing: Double, initializer: HBox.() -> Unit)
        = HBox(spacing).apply(initializer)


fun vbox(initializer: VBox.() -> Unit)
        = VBox().apply(initializer)

fun vbox(spacing: Double, initializer: VBox.() -> Unit)
        = VBox(spacing).apply(initializer)


fun label(initializer: Label.() -> Unit)
        = Label().apply(initializer)

fun label(text: String)
        = Label(text)

fun label(text: String, initializer: Label.() -> Unit)
        = Label(text).apply(initializer)


fun button(initializer: Button.() -> Unit)
        = Button().apply(initializer)

fun button(text: String, initializer: Button.() -> Unit)
        = Button(text).apply(initializer)


fun spinner(min: Double, max: Double, initialValue: Double, initializer: Spinner<Number>.() -> Unit)
        = Spinner<Number>(min, max, initialValue).apply(initializer)

fun spinner(min: Int, max: Int, initialValue: Int, initializer: Spinner<Number>.() -> Unit)
        = Spinner<Number>(min, max, initialValue).apply(initializer)


fun <T> combobox(items: Array<T>, initializer: ComboBox<T>.() -> Unit)
        = combobox(FXCollections.observableArrayList(*items), initializer)

fun <T> combobox(items: List<T>, initializer: ComboBox<T>.() -> Unit)
        = combobox(FXCollections.observableArrayList(items), initializer)

fun <T> combobox(items: ObservableList<T>, initializer: ComboBox<T>.() -> Unit)
        = ComboBox(items).apply(initializer)

fun textfield(initializer: TextField.() -> Unit)
        = TextField().apply(initializer)

fun rectangle(width: Double, height: Double, initializer: Rectangle.() -> Unit)
        = Rectangle(width, height).apply(initializer)

fun circle(radius: Double, initializer: Circle.() -> Unit)
        = Circle(radius).apply(initializer)

//fun <S> tableview(initializer: TableView<S>.() -> Unit)
//    = TableViewContext<S>().apply(initializer)

fun <S> tableview(items: ObservableList<S>, initializer: TableView<S>.() -> Unit)
        = TableViewContext<S>(items).apply(initializer)

class TableViewContext<S>(items: ObservableList<S>) : TableView<S>(items) {
    fun <V> tablecolumn(header: String, initializer: TableColumn<S, V>.() -> Unit): TableColumn<S, V> {
        val tableColumn = TableColumn<S, V>(header).apply(initializer)
        this.columns.add(tableColumn)
        return tableColumn
    }
}

fun tabpane(initializer: TabPane.() -> Unit)
        = TabPane().apply(initializer)

fun tab(name: String, initializer: Tab.() -> Unit)
        = Tab(name).apply(initializer)


fun gridpane(initializer: GridPaneContext.() -> Unit): GridPane
        = GridPaneContext().apply(initializer)

class GridPaneContext : GridPane() {
    private var rowIndex = 0

    fun row(initializer: RowContext.() -> Unit): RowContext {
        val context = RowContext(this, rowIndex++).apply(initializer)
        return context
    }
}

class RowContext(private val gridPane: GridPane, private val rowIndex: Int) {
    private var colIndex = 0

    fun <T: Node> cell(creator: () -> T) {
        gridPane.add(creator.invoke(), colIndex++, rowIndex)
    }

    fun <T: Node> cell(colspan: Int, rowspan: Int, creator: () -> T) {
        gridPane.add(creator.invoke(), colIndex++, rowIndex, colspan, rowspan)
    }

}
