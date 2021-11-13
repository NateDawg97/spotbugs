package edu.umd.cs.findbugs.detect;

import edu.umd.cs.findbugs.AbstractIntegrationTest;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcher;
import edu.umd.cs.findbugs.test.matcher.BugInstanceMatcherBuilder;
import org.junit.Test;

import static edu.umd.cs.findbugs.test.CountMatcher.containsExactly;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @see <a href="https://github.com/spotbugs/spotbugs/issues/1472">GitHub issue #1472</a>
 */
public class Issue1501Test extends AbstractIntegrationTest {
    @Test
    public void test() {
        performAnalysis("ghIssues/Issue1501.class");
        BugInstanceMatcherBuilder builder = new BugInstanceMatcherBuilder()
                .bugType("VA_FORMAT_STRING_USES_NEWLINE");

        BugInstanceMatcher withoutVariable = builder.atLine(12).build();
        BugInstanceMatcher withVariable = builder.atLine(8).build();

        assertThat(getBugCollection(), containsExactly(1, withoutVariable));
        assertThat(getBugCollection(), containsExactly(1, withVariable));
    }
}
