package app;

import data_access.HttpProgramPageClient;
import data_access.UoftMajorCatalog;
import entity.Major;
import java.time.Clock;

/** Optional manual live check. Normal JUnit tests never call the website. */
public final class VerifyUoftScrape {
    private VerifyUoftScrape() { }
    public static void main(String[] args) {
        UoftMajorCatalog catalog = new UoftMajorCatalog(new HttpProgramPageClient(), Clock.systemUTC());
        catalog.refresh();
        int live = 0;
        for (Major major : catalog.getMajors()) {
            System.out.println(major.getName() + " | " + major.getSource()
                    + " | " + (major.isPlayable() ? "Playable" : "Display only"));
            if (major.getSource() == Major.Source.WEB) { live++; }
        }
        System.out.println("Live pages: " + live + "/8");
        if (live != 8) { throw new IllegalStateException("Some pages used fallback. Check network or selectors."); }
    }
}
