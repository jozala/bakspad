package pl.aetas.bakspad.presentation;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

public class LocaleAwareStringComparator implements Comparator<String> {

    private final Collator localeCollator;

    public LocaleAwareStringComparator(Locale locale) {
        localeCollator = Collator.getInstance(locale);
    }

    @Override
    public int compare(String o1, String o2) {
        return localeCollator.compare(o1, o2);
    }
}
