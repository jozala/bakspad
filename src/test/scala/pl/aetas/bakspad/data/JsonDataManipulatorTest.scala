package pl.aetas.bakspad.data

import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file._
import pl.aetas.bakspad.model.Note
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import pl.aetas.bakspad.spec.IntegrationSpec
import scala.io.Source
import java.nio.file.attribute.BasicFileAttributes
import java.util
import scalax.io.{Output, Resource}
import scala.collection
import org.junit.Assert.assertThat
import org.hamcrest.CoreMatchers
import java.io.IOException

@RunWith(classOf[JUnitRunner])
class JsonDataManipulatorTest extends IntegrationSpec {

  val filename: String = "some-filename"
  var jsonDataManipulator: JsonDataManipulator = null
  val testDataDirectory = Paths.get("test-data")
  val sampleNote: Note = new Note("some-name", "test description", "test content")
  val noteFile: NoteFile = NoteFile.loadedNoteFile(filename, sampleNote, jsonDataManipulator)
  val objectMapper: ObjectMapper = new ObjectMapper()


  override def beforeAll() {
    removeTestDataDirectory()
  }

  before {
    jsonDataManipulator = new JsonDataManipulator(objectMapper, testDataDirectory)
  }

  after {
    removeTestDataDirectory()
  }

  "A JsonDataManipulator save" should "save Note file in data directory with a given filename" in {
    jsonDataManipulator.save(noteFile)
    assert(Files.exists(testDataDirectory.resolve(filename)) === true)
  }

  it should "create a data directory when saving and data directory does not exist" in {
    assert(Files.notExists(testDataDirectory) === true)
    jsonDataManipulator.save(noteFile)
    assert(Files.exists(testDataDirectory) === true)
  }

  it should "throw NullPointerException when null NoteFile given to save" in {
    intercept[NullPointerException] {
      jsonDataManipulator.save(null)
    }
  }

  it should "save Note in JSON format in the file" in {
    jsonDataManipulator.save(noteFile)
    val stringFromFile: String = Source.fromFile(testDataDirectory.resolve(filename).toFile).mkString
    assert(stringFromFile == "{\"name\":\"some-name\",\"description\":\"test description\",\"content\":\"test content\"}")
  }

  "A JsonDataManipulator load" should "load all files from data directory and return them" in {
    val note1: Note = new Note("note1name", "note1description", "note1content")
    val note2: Note = new Note("note2name", "note2description", "note2content")
    val note3: Note = new Note("note3name", "note3description", "note3content")
    createFileWithNote("testfile1", note1)
    createFileWithNote("testfile2", note2)
    createFileWithNote("testfile3", note3)
    val noteFiles: util.Set[NoteFile] = jsonDataManipulator.load
    noteFiles should have size 3
    noteFiles should contain (NoteFile.loadedNoteFile("testfile1", note1, jsonDataManipulator))
    noteFiles should contain (NoteFile.loadedNoteFile("testfile2", note2, jsonDataManipulator))
    noteFiles should contain (NoteFile.loadedNoteFile("testfile3", note3, jsonDataManipulator))
  }
  
  it should "return empty collection when data directory does not exists" in {
    val noteFiles: util.Set[NoteFile] = jsonDataManipulator.load()
    noteFiles should have size 0
  }

  "A JsonDataManipulator delete" should "delete file with given note from disk" in {
    jsonDataManipulator.save(noteFile)
    jsonDataManipulator.delete(noteFile)
    Files.notExists(testDataDirectory.resolve(noteFile.getFilename)) shouldBe true
  }

  it should "throw IOException when file to delete does not exists" in {
    intercept[IOException] {
      jsonDataManipulator.delete(noteFile)
    }
  }

  it should "throw Exception when given noteFile is null" in {
    intercept[NullPointerException] {
      jsonDataManipulator.delete(null)
    }
  }

  "A JsonDataManipulator createProperFilename" should "return name with added number when file with suggested name already exists" in {
    createFileWithNote("testfile", sampleNote)
    val properFilename: String = jsonDataManipulator.createProperFilename("testfile")
    properFilename should equal ("testfile-1")
  }

  it should "return name with consecutive number when file with suggested name with -1 already exists" in {
    createFileWithNote("testfile", sampleNote)
    createFileWithNote("testfile-1", sampleNote)
    val properFilename: String = jsonDataManipulator.createProperFilename("testfile")
    properFilename should equal ("testfile-2")
  }

  it should "return same filename as suggested when file with given suggested name does not exists" in {
    val properFilename: String = jsonDataManipulator.createProperFilename("testfile")
    properFilename should equal ("testfile")
  }


  def createFileWithNote(filename: String, entry: Note): Unit = {
    val output: Output = Resource.fromFile(testDataDirectory.resolve(filename).toFile)
    output.write(s"""{"name":"${entry.getName}","description":"${entry.getDescription}","content":"${entry.getContent}"}""")
  }


  def removeTestDataDirectory() {
    if (Files.exists(testDataDirectory)) {
      Files.walkFileTree(testDataDirectory, new SimpleFileVisitor[Path]() {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }
      })
      Files.delete(testDataDirectory)
    }
  }
}
