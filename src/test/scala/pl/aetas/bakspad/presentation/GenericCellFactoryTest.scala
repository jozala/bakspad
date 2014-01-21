package pl.aetas.bakspad.presentation

import pl.aetas.bakspad.spec.UnitSpec
import javafx.scene.control.{TableCell, TableColumn, ContextMenu}
import pl.aetas.bakspad.data.NoteFile

class GenericCellFactoryTest extends UnitSpec {

  var genericCellFactory : GenericCellFactory = _
  var contextMenu : ContextMenu = _

  before {
    contextMenu = mock[ContextMenu]
    genericCellFactory = new GenericCellFactory(contextMenu)
  }

  "A GenericCellFactory" should "create a NoteFileTableCell" in {
    val cell: TableCell[NoteFile, String] = genericCellFactory.call(new TableColumn[NoteFile, String]())
    cell shouldBe a [NoteFileTableCell]
  }
}
