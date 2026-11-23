package data_access;

import entity.Major;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/** Eight allowlisted St. George major pages; never follows arbitrary discovered links. */
public final class UoftProgramSources {
    public static final class Program {
        private final String id;
        private final String code;
        private final Major fallback;
        private Program(String id, String code, Major fallback) {
            this.id = id; this.code = code; this.fallback = fallback;
        }
        public String getId() { return id; }
        public String getCode() { return code; }
        public Major getFallback() { return fallback; }
        public URI getUri() { return URI.create(fallback.getSourceUrl()); }
    }
    private UoftProgramSources() { }
    public static List<Program> all() {
        List<Major> playable = InMemoryMajorCatalog.predefined().getMajors();
        List<Program> result = new ArrayList<>();
        add(result, playable.get(0), "ASMAJ1689");
        add(result, playable.get(1), "ASMAJ2364");
        add(result, playable.get(2), "ASMAJ0652");
        add(result, displayOnly("mathematics", "Mathematics"), "ASMAJ1165");
        add(result, displayOnly("economics", "Economics"), "ASMAJ1478");
        add(result, displayOnly("psychology", "Psychology"), "ASMAJ1160");
        add(result, displayOnly("sociology", "Sociology"), "ASMAJ1013");
        add(result, displayOnly("philosophy", "Philosophy"), "ASMAJ0231");
        return List.copyOf(result);
    }
    private static Major displayOnly(String id, String name) {
        return new Major(id, name, "Browse this U of T major. Gameplay is not implemented for this major.", List.of());
    }
    private static void add(List<Program> result, Major local, String code) {
        String url = "https://artsci.calendar.utoronto.ca/program/" + code.toLowerCase(java.util.Locale.ROOT);
        Major fallback = new Major(local.getId(), local.getName(), local.getDescription(),
                local.getCourses(), url, Major.Source.LOCAL_FALLBACK);
        result.add(new Program(local.getId(), code, fallback));
    }
}
