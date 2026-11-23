package use_case.choose_major;

import entity.Major;
import java.util.List;
import java.util.Optional;

/** Implemented by the outer data-access layer. */
public interface MajorCatalogDataAccessInterface {
    List<Major> getMajors();
    Optional<Major> findById(String majorId);
}
