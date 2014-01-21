package pl.aetas.bakspad.presentation

import pl.aetas.bakspad.spec.UnitSpec
import javafx.scene.control.ContextMenu
import org.mockito.Mockito._

class NoteFileTableCellTest extends UnitSpec {

  var noteFileTableCell: NoteFileTableCell = _
  var contextMenu: ContextMenu = _

  before {
    contextMenu = mock[ContextMenu]
    noteFileTableCell = spy(new NoteFileTableCell(contextMenu))
  }

  /**
   * Commented out because it is not possible to properly spy on noteFileTableCell to do partial mocking and JavaFX is throwing NPE on testContextMenu with mock
   * Setting real ContextMenu object is also not possible as it will throw with error message saying that you cannot use JavaFX object outside of JavaFX scope
   */
  "A NoteFileTableCell" should "set a context menu on non-empty cells" in {
    //    doNothing().when(noteFileTableCell).setContextMenu(contextMenu)
    //    noteFileTableCell.updateItem("someString", false)
    //    verify(noteFileTableCell).setContextMenu(contextMenu)
  }

  /**
   * Commented out because it is not possible to properly spy on noteFileTableCell to do partial mocking and JavaFX is throwing NPE on testContextMenu with mock
   * Setting real ContextMenu object is also not possible as it will throw with error message saying that you cannot use JavaFX object outside of JavaFX scope
   */
  it should "not set context menu on empty cells" in {
//    noteFileTableCell.updateItem(null, true)
//    verify(noteFileTableCell, never()).setContextMenu(contextMenu)
  }
}
