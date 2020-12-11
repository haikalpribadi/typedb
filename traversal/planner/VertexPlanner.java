package grakn.core.traversal.planner;

import grakn.core.traversal.procedure.VertexProcedure;
import grakn.core.traversal.structure.Structure;
import grakn.core.traversal.structure.StructureVertex;

public class VertexPlanner implements Planner {

    private final VertexProcedure procedure;

    private VertexPlanner(StructureVertex<?> vertex) {
        procedure = VertexProcedure.create(vertex);
    }

    static VertexPlanner create(Structure structure) {
        assert structure.vertices().size() == 1;
        return new VertexPlanner(structure.vertices().iterator().next());
    }

    @Override
    public VertexProcedure procedure() {
        return procedure;
    }

    @Override
    public boolean isVertex() { return true; }

    @Override
    public VertexPlanner asVertex() { return this; }
}