package pl.aetas.bakspad.spec

import org.scalatest._
import org.scalatest.mock.MockitoSugar

abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inside with Inspectors with BeforeAndAfter with MockitoSugar {

}
