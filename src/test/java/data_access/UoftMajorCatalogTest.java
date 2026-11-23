package data_access;

import entity.Major;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class UoftMajorCatalogTest {
    private static final Clock NOW = Clock.fixed(Instant.parse("2026-11-23T12:00:00Z"), ZoneOffset.UTC);
    private static String fixture(String id) throws IOException {
        try (java.io.InputStream stream = UoftMajorCatalogTest.class.getResourceAsStream("/uoft/" + id + ".html")) {
            if (stream == null) { throw new IOException("Missing fixture " + id); }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
    private static ProgramPageClient fixtures() {
        return uri -> {
            UoftProgramSources.Program program = UoftProgramSources.all().stream()
                    .filter(p -> p.getUri().equals(uri)).findFirst().orElseThrow();
            return fixture(program.getId());
        };
    }

    @Test
    void initialCatalogueContainsEightLabelledFallbacksWithoutHttp() {
        UoftMajorCatalog catalog = new UoftMajorCatalog(uri -> { fail("Unexpected HTTP call"); return ""; }, NOW);
        assertEquals(8, catalog.getMajors().size());
        assertTrue(catalog.getMajors().stream().allMatch(m -> m.getSource() == Major.Source.LOCAL_FALLBACK));
        assertEquals(Set.of("cs", "biology", "history"), catalog.getMajors().stream()
                .filter(Major::isPlayable).map(Major::getId).collect(Collectors.toSet()));
    }

    @Test
    void extractsAllEightRealHeadingFixturesAndKeepsGameCourses() {
        UoftMajorCatalog catalog = new UoftMajorCatalog(fixtures(), NOW);
        catalog.refresh();
        assertEquals(8, catalog.getMajors().size());
        assertTrue(catalog.getMajors().stream().allMatch(m -> m.getSource() == Major.Source.WEB));
        assertEquals("Computer Science", catalog.findById("cs").orElseThrow().getName());
        assertEquals("Philosophy", catalog.findById("philosophy").orElseThrow().getName());
        assertEquals("high_school_computer_science", catalog.findById("cs").orElseThrow()
                .getCourses().get(0).getQuestionCategory());
        assertTrue(catalog.getMajors().stream().filter(m -> !m.isPlayable()).allMatch(m -> m.getCourses().isEmpty()));
    }

    @Test
    void onePageFailureDoesNotDiscardOtherScrapedMajors() {
        ProgramPageClient pages = fixtures();
        UoftMajorCatalog catalog = new UoftMajorCatalog(uri -> {
            if (uri.getPath().endsWith("asmaj2364")) { throw new IOException("Timeout"); }
            return pages.fetch(uri);
        }, NOW);
        catalog.refresh();
        assertEquals(Major.Source.LOCAL_FALLBACK, catalog.findById("biology").orElseThrow().getSource());
        assertEquals(7, catalog.getMajors().stream().filter(m -> m.getSource() == Major.Source.WEB).count());
        assertEquals(2, catalog.findById("biology").orElseThrow().getCourses().size());
    }

    @Test
    void completeNetworkFailureStillLeavesPlayableAndDisplayOnlyEntries() {
        UoftMajorCatalog catalog = new UoftMajorCatalog(uri -> { throw new IOException("Offline"); }, NOW);
        catalog.refresh();
        assertEquals(8, catalog.getMajors().size());
        assertEquals(3, catalog.getMajors().stream().filter(Major::isPlayable).count());
        assertTrue(catalog.getMajors().stream().allMatch(m -> m.getSource() == Major.Source.LOCAL_FALLBACK));
    }

    @Test
    void layoutChangeOrWrongProgramCannotMasqueradeAsScrapedData() {
        UoftMajorCatalog catalog = new UoftMajorCatalog(uri -> "<h1>Access denied</h1>", NOW);
        catalog.refresh();
        assertTrue(catalog.getMajors().stream().allMatch(m -> m.getSource() == Major.Source.LOCAL_FALLBACK));
        UoftMajorPageParser parser = new UoftMajorPageParser();
        assertThrows(IOException.class, () -> parser.parse(fixture("history"), UoftProgramSources.all().get(0)));
    }

    @Test
    void cachePreventsRepeatedFetchesWhenPreviewingAndReopening() throws IOException {
        AtomicInteger count = new AtomicInteger();
        ProgramPageClient pages = fixtures();
        UoftMajorCatalog catalog = new UoftMajorCatalog(uri -> { count.incrementAndGet(); return pages.fetch(uri); }, NOW);
        catalog.refresh(); catalog.getMajors(); catalog.findById("cs"); catalog.refresh();
        assertEquals(8, count.get());
    }

    @Test
    void httpClientRejectsNonProgramAndNonOfficialUrlsBeforeRequesting() {
        HttpProgramPageClient client = new HttpProgramPageClient();
        assertThrows(IOException.class, () -> client.fetch(java.net.URI.create("https://example.com/program/asmaj1689")));
        assertThrows(IOException.class, () -> client.fetch(java.net.URI.create("https://artsci.calendar.utoronto.ca/user/login")));
    }
}
