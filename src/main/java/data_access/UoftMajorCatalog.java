package data_access;

import entity.Major;
import use_case.choose_major.MajorCatalogDataAccessInterface;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Cached external catalogue with independent per-program local fallback. */
public final class UoftMajorCatalog implements MajorCatalogDataAccessInterface {
    private final ProgramPageClient client;
    private final Clock clock;
    private final UoftMajorPageParser parser = new UoftMajorPageParser();
    private final List<UoftProgramSources.Program> programs = UoftProgramSources.all();
    private volatile List<Major> snapshot;
    private Instant lastRefresh;

    public UoftMajorCatalog(ProgramPageClient client, Clock clock) {
        this.client = java.util.Objects.requireNonNull(client);
        this.clock = java.util.Objects.requireNonNull(clock);
        snapshot = programs.stream().map(UoftProgramSources.Program::getFallback).collect(Collectors.toUnmodifiableList());
    }
    @Override
    public List<Major> getMajors() { return snapshot; }
    @Override
    public Optional<Major> findById(String id) {
        return snapshot.stream().filter(major -> major.getId().equals(id)).findFirst();
    }

    /** Call only from an outer-layer worker. At most one bounded pass per ten minutes. */
    public synchronized void refresh() {
        Instant now = clock.instant();
        if (lastRefresh != null && now.isBefore(lastRefresh.plus(Duration.ofMinutes(10)))) { return; }
        List<Major> updated = new ArrayList<>();
        boolean interrupted = false;
        for (UoftProgramSources.Program program : programs) {
            Major major = program.getFallback();
            if (!interrupted) {
                try { major = parser.parse(client.fetch(program.getUri()), program); }
                catch (InterruptedException exception) { Thread.currentThread().interrupt(); interrupted = true; }
                catch (IOException exception) { /* This program alone uses its labelled local fallback. */ }
            }
            updated.add(major);
        }
        snapshot = List.copyOf(updated);
        lastRefresh = now;
    }
}
