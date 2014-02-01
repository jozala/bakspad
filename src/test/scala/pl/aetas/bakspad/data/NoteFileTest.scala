package pl.aetas.bakspad.data

import pl.aetas.bakspad.spec.UnitSpec
import pl.aetas.bakspad.model.Note
import org.mockito.Mockito._
import pl.aetas.bakspad.exception.{DeleteFailedException, SaveFailedException}
import java.io.IOException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NoteFileTest extends UnitSpec {

  val filename: String = "someFilename"

  var notDirtyNoteFile : NoteFile = _
  var dirtyNoteFile : NoteFile = _
  var dataManipulator: JsonDataManipulator = _

  before {
    dataManipulator = mock[JsonDataManipulator]
    val noteSample = new Note("some-name", "some description", "content")
    notDirtyNoteFile = NoteFile.loadedNoteFile(filename, noteSample, dataManipulator)
    dirtyNoteFile = NoteFile.loadedNoteFile(filename, noteSample, dataManipulator)
    dirtyNoteFile.setContent("changed content")
  }

  "A dirty NoteFile save" should "save file using given data manipulator" in {
    dirtyNoteFile.save()
    verify(dataManipulator).save(dirtyNoteFile)
  }

  it should "throw SaveFailedException when IOException thrown from DataManipulator" in {
    when(dataManipulator.save(dirtyNoteFile)).thenThrow(classOf[IOException])
    intercept[SaveFailedException] {
      dirtyNoteFile.save()
    }
  }

  it should "set itself as not dirty after save completed successfully" in {
    dirtyNoteFile.save()
    dirtyNoteFile.isDirty shouldBe false
  }

  it should "notify registered isDirtyListeners of changing dirty to false" in {
    var hasBeenNotifiedCorrectly = false
    dirtyNoteFile.registerIsDirtyListener(new NoteFileIsDirtyListener(){
      def change(isDirty: Boolean): Unit = { if (!isDirty) hasBeenNotifiedCorrectly = true }
    })
    dirtyNoteFile.save()
    hasBeenNotifiedCorrectly shouldBe true
  }

  "Not dirty NoteFile" should "change state to dirty when content is changed" in {
    notDirtyNoteFile.setContent("new Content")
    notDirtyNoteFile.isDirty shouldBe true
  }

  it should "change state to dirty when name is changed" in {
    notDirtyNoteFile.setName("new name")
    notDirtyNoteFile.isDirty shouldBe true
  }

  it should "change state to dirty when description is changed" in {
    notDirtyNoteFile.setDescription("new description")
    notDirtyNoteFile.isDirty shouldBe true
  }

  it should "not try to save note entry as it is not dirty" in {
    notDirtyNoteFile.save()
    verifyZeroInteractions(dataManipulator)
  }

  it should "notify registered isDirtyListeners of changing dirty to true when changing name" in {
    var hasBeenNotifiedCorrectly = false
    notDirtyNoteFile.registerIsDirtyListener(new NoteFileIsDirtyListener(){
      def change(isDirty: Boolean): Unit = { if (isDirty) hasBeenNotifiedCorrectly = true }
    })
    notDirtyNoteFile.setName("newName")
    hasBeenNotifiedCorrectly shouldBe true
  }

  it should "notify registered isDirtyListeners of changing dirty to true when changing description" in {
    var hasBeenNotifiedCorrectly = false
    notDirtyNoteFile.registerIsDirtyListener(new NoteFileIsDirtyListener(){
      def change(isDirty: Boolean): Unit = { if (isDirty) hasBeenNotifiedCorrectly = true }
    })
    notDirtyNoteFile.setDescription("new description")
    hasBeenNotifiedCorrectly shouldBe true
  }

  it should "notify registered isDirtyListeners of changing dirty to true when changing content" in {
    var hasBeenNotifiedCorrectly = false
    notDirtyNoteFile.registerIsDirtyListener(new NoteFileIsDirtyListener(){
      def change(isDirty: Boolean): Unit = { if (isDirty) hasBeenNotifiedCorrectly = true }
    })
    notDirtyNoteFile.setContent("new content")
    hasBeenNotifiedCorrectly shouldBe true
  }

  "A NoteFile delete" should "delete note file using given data manipulator" in {
    notDirtyNoteFile.delete()
    verify(dataManipulator).delete(notDirtyNoteFile)
  }

  it should "throw exception when dataManipulator throw IO" in {
    when(dataManipulator.delete(notDirtyNoteFile)).thenThrow(classOf[IOException])
    intercept[DeleteFailedException] {
      notDirtyNoteFile.delete()
    }
  }

}
