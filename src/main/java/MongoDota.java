import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.set;

import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;

public class MongoDota {
    private final MongoClient client;
    private final MongoDatabase db;
    private final MongoCollection<Document> matchDetails;

    public MongoDota() {
        client = new MongoClient();
        db = client.getDatabase("dota");
        matchDetails = db.getCollection("match_details");
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDb() {
        return db;
    }

    public FindIterable<Document> getMatchesWithoutDraft() {
        return matchDetails
                .find(
                        and(
                                eq("ability_draft", null),
                                ne("replay_url", null),
                                eq("replay_error", null)
                        )
                )
                .projection(fields(include("_id", "replay_url")));
    }

    public boolean updateMatchWithDraft(Long matchId, BasicDBObject draftResults) {
        UpdateResult updateResults = matchDetails
                .updateOne(eq("_id", matchId), set("ability_draft", draftResults));

        return updateResults.wasAcknowledged();
    }

    public boolean updateFailedDraftParse(Long matchId, String reason) {
        UpdateResult updateResults = matchDetails
                .updateOne(eq("_id", matchId), set("replay_error", reason));

        return updateResults.wasAcknowledged();
    }


}
