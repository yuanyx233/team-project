package data_access;

import entity.CampusMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads the campus map tile file bundled on the classpath.
 */
public final class CampusMapResourceLoader {
    public static final String DEFAULT_MAP = "/maps/campus_map.txt";

    private CampusMapResourceLoader() {
    }

    /**
     * @param resourcePath classpath path of the map file
     * @return the parsed map
     * @throws IllegalStateException if the resource does not exist
     * @throws IllegalArgumentException if the file is not a valid map
     */
    public static CampusMap load(String resourcePath) {
        InputStream in = CampusMapResourceLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("Map resource not found: " + resourcePath);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> rows = reader.lines().filter(line -> !line.isEmpty()).collect(Collectors.toList());
            return CampusMap.parse(rows);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read map resource " + resourcePath, e);
        }
    }
}
