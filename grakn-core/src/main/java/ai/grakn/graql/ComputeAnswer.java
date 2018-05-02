package ai.grakn.graql;

import ai.grakn.concept.Concept;

import java.util.List;
import java.util.Optional;

public interface ComputeAnswer{

    Optional<List<List<Concept>>> paths();

    ComputeAnswer paths(List<List<Concept>> paths);
}
