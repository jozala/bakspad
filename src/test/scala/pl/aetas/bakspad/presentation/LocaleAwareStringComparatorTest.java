package pl.aetas.bakspad.presentation;

import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

public class LocaleAwareStringComparatorTest {

    private LocaleAwareStringComparator polishLocaleComparator;

    @Before
    public void setUp() throws Exception {
        polishLocaleComparator = new LocaleAwareStringComparator(new Locale("pl_PL"));
    }

    @Test
    public void shouldTakeLocaleSpecificOrderWhenReturningComparableResult() throws Exception {
        String first = "ą";
        String second = "z";

        int compare = polishLocaleComparator.compare(first, second);
        assertThat(compare, lessThan(0));

    }
}
