package data_access;

import entity.Major;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts actual program titles, never guesses a successful scrape on malformed HTML. */
public final class UoftMajorPageParser {
    public Major parse(String html, UoftProgramSources.Program program) throws IOException {
        Element heading = Jsoup.parse(html, program.getUri().toString()).selectFirst("h1.page-title");
        if (heading == null) { throw new IOException("Official program heading not found."); }
        String title = heading.text().replace('\u00a0', ' ').trim();
        Pattern pattern = Pattern.compile("^(.+?) Major \\((?:Arts|Science) Program\\) - "
                + program.getCode() + "$");
        Matcher match = pattern.matcher(title);
        if (!match.matches() || title.length() > 240) {
            throw new IOException("Unexpected major title or program code.");
        }
        Major local = program.getFallback();
        return new Major(local.getId(), match.group(1), local.getDescription(), local.getCourses(),
                local.getSourceUrl(), Major.Source.WEB);
    }
}
