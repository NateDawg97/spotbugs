package edu.umd.cs.findbugs.detect;

import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.test.SpotBugsRule;
import edu.umd.cs.findbugs.AbstractIntegrationTest;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcher;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcherBuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static edu.umd.cs.findbugs.test.CountMatcher.containsExactly;
import static org.hamcrest.MatcherAssert.assertThat;

public class Issue1293Test extends AbstractIntegrationTest {
    @Test
    public void test() {
        performAnalysis("ghIssues/Issue1293.class");

        BugInstanceMatcherBuilder builder = new BugInstanceMatcherBuilder()
                .bugType("ODR_OPEN_DATABASE_RESOURCE");

        // Test the straight connection and statements

        BugInstanceMatcher str8ConnMatcher = builder.atLine(8).build();
        BugInstanceMatcher str8StmtMatcher = builder.atLine(9).build();

        assertThat(getBugCollection(), containsExactly(1, str8ConnMatcher));
        assertThat(getBugCollection(), containsExactly(1, str8StmtMatcher));

        // Test the abstracted connection and statements

        BugInstanceMatcher abstractedConnMatcher = builder.atLine(21).build();
        BugInstanceMatcher abstractedStmtMatcher = builder.atLine(22).build();

        assertThat(getBugCollection(), containsExactly(1, abstractedConnMatcher));
        assertThat(getBugCollection(), containsExactly(0, abstractedStmtMatcher));
    }
}
