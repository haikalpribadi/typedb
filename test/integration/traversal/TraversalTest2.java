package grakn.core.traversal;

import grakn.core.Grakn;
import grakn.core.common.parameters.Arguments;
import grakn.core.rocks.RocksGrakn;
import grakn.core.test.integration.util.Util;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlMatch;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TraversalTest2 {
    private static Path directory = Paths.get(System.getProperty("user.dir")).resolve("traversal-test-2");
    private static String database = "traversal-test-2";

    @Test
    public void match_query_stalls() throws IOException {
        Util.resetDirectory(directory);

        try (Grakn grakn = RocksGrakn.open(directory)) {
            grakn.databases().create(database);

            try (Grakn.Session session = grakn.session(database, Arguments.Session.Type.SCHEMA)) {
                try (Grakn.Transaction transaction = session.transaction(Arguments.Transaction.Type.WRITE)) {
                    transaction.query().define(Graql.parseQuery("define lion sub entity;").asDefine());
                    transaction.commit();
                }
            }

            try (Grakn.Session session = grakn.session(database, Arguments.Session.Type.DATA)) {
                try (Grakn.Transaction transaction = session.transaction(Arguments.Transaction.Type.WRITE)) {
                    final String queryString = "insert $x isa lion;";
                    final GraqlInsert query = Graql.parseQuery(queryString);
                    transaction.query().insert(query).toList();
                    final GraqlMatch secondQuery = Graql.parseQuery("match $x isa lion;");
                    transaction.query().match(secondQuery).toList();
                    transaction.commit();
                }
            }
        }
    }
}
