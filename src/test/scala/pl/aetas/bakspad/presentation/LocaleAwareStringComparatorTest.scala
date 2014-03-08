package pl.aetas.bakspad.presentation

import pl.aetas.bakspad.spec.UnitSpec
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.util.Locale

@RunWith(classOf[JUnitRunner])
class LocaleAwareStringComparatorTest extends UnitSpec {

  val polishLocaleComparator = new LocaleAwareStringComparator(new Locale("pl_PL"))

  "LocaleAwareStringComparator" should "use locale specific order when returning comparable result" in {
    val first = "ą"
    val second = "z"
    val compare = polishLocaleComparator.compare(first, second)
    compare should be < 0
  }
}
