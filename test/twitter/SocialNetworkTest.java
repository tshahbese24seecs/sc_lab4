package twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Tests for SocialNetwork.
 *
 * Testing strategy
 *
 * guessFollowsGraph(tweets):
 *   - number of tweets: 0, 1, many
 *   - mentions per tweet: 0, 1, many
 *   - tweets by the same author: one, several
 * The postcondition is underdetermined (an implementation may infer extra follows), so tests
 * only check evidence that MUST appear (contains) or that nothing is inferred when there is
 * no evidence at all.
 *
 * influencers(graph):
 *   - graph: empty, one user without followers, one influencer, several influencers, ties
 *   - the result lists every user, sorted by descending follower count; the order of tied users
 *     is unspecified, so tie tests do not depend on it.
 */
public class SocialNetworkTest {

    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");

    private static Tweet tweet(long id, String author, String text) {
        return new Tweet(id, author, text, d1);
    }

    private static Set<String> set(String... names) {
        return new HashSet<String>(Arrays.asList(names));
    }

    // ---------------------------------------------------------------- guessFollowsGraph

    // 1. Empty list of tweets -> no evidence -> empty graph
    @Test
    public void testGuessFollowsGraphEmpty() {
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(new ArrayList<Tweet>());
        assertTrue("expected empty graph", graph.isEmpty());
    }

    // 2. Tweets without mentions -> nobody follows anybody
    @Test
    public void testGuessFollowsGraphNoMentions() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "good morning everyone"),
                tweet(2, "bob", "contact me at bob@mit.edu"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        for (Set<String> follows : graph.values()) {
            assertTrue("no evidence, so nobody should follow anybody", follows.isEmpty());
        }
    }

    // 3. Single mention -> author follows the mentioned user
    @Test
    public void testGuessFollowsGraphSingleMention() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "alice", "hello @bob"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue("alice should be in the graph", graph.containsKey("alice"));
        assertTrue("alice should follow bob", graph.get("alice").contains("bob"));
    }

    // 4. Multiple mentions in one tweet -> author follows every mentioned user
    @Test
    public void testGuessFollowsGraphMultipleMentions() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "alice", "@bob and @carol, meet @dave"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue(graph.get("alice").containsAll(set("bob", "carol", "dave")));
    }

    // 5. Several tweets by one user -> mentions from all of them are captured
    @Test
    public void testGuessFollowsGraphMultipleTweetsOneUser() {
        List<Tweet> tweets = Arrays.asList(
                tweet(1, "alice", "hi @bob"),
                tweet(2, "alice", "thanks @carol"),
                tweet(3, "alice", "again @bob"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue(graph.get("alice").containsAll(set("bob", "carol")));
    }

    // Extra: usernames are case-insensitive and keys are lower case
    @Test
    public void testGuessFollowsGraphCaseInsensitive() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "Alice", "hello @BOB"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertTrue(graph.containsKey("alice"));
        assertTrue(graph.get("alice").contains("bob"));
    }

    // Extra: a user cannot follow themselves
    @Test
    public void testGuessFollowsGraphNoSelfFollow() {
        List<Tweet> tweets = Arrays.asList(tweet(1, "alice", "note to self @alice"));
        Map<String, Set<String>> graph = SocialNetwork.guessFollowsGraph(tweets);
        assertFalse(graph.containsKey("alice") && graph.get("alice").contains("alice"));
    }

    // ---------------------------------------------------------------- influencers

    // 6. Empty graph -> empty list
    @Test
    public void testInfluencersEmpty() {
        Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
        assertTrue(SocialNetwork.influencers(graph).isEmpty());
    }

    // 7. Single user without followers -> nobody has any follower, so nobody is influential
    @Test
    public void testInfluencersSingleUserNoFollowers() {
        Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
        graph.put("alice", new HashSet<String>());
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals(Arrays.asList("alice"), result); // listed, but with zero followers
    }

    // 8. Single influencer -> the only followed user comes first
    @Test
    public void testInfluencersSingleInfluencer() {
        Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
        graph.put("alice", set("bob"));
        graph.put("carol", set("bob"));
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals("bob", result.get(0));
        assertEquals(3, result.size());
    }

    // 9. Multiple influencers -> descending order of follower count
    @Test
    public void testInfluencersOrdering() {
        Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
        graph.put("alice", set("bob", "carol"));
        graph.put("dave", set("bob", "carol"));
        graph.put("erin", set("bob"));
        // followers: bob = 3, carol = 2, alice = dave = erin = 0
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals("bob", result.get(0));
        assertEquals("carol", result.get(1));
    }

    // 10. Tied influence -> tied users appear together; their relative order is unspecified
    @Test
    public void testInfluencersTie() {
        Map<String, Set<String>> graph = new HashMap<String, Set<String>>();
        graph.put("alice", set("bob", "carol"));
        graph.put("dave", set("bob", "carol"));
        graph.put("erin", set("frank"));
        // followers: bob = 2, carol = 2, frank = 1
        List<String> result = SocialNetwork.influencers(graph);
        assertEquals(set("bob", "carol"), new HashSet<String>(result.subList(0, 2)));
        assertEquals("frank", result.get(2));
    }
}