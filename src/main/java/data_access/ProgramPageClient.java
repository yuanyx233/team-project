package data_access;

import java.io.IOException;
import java.net.URI;

/** Outer-layer seam for offline HTML fixtures; not exposed to use cases. */
@FunctionalInterface
public interface ProgramPageClient {
    String fetch(URI uri) throws IOException, InterruptedException;
}
