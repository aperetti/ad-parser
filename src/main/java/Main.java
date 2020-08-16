import com.mongodb.client.FindIterable;
import org.bson.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        MongoDota db = new MongoDota();
        FindIterable<Document> matches = db.getMatchesWithoutDraft();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (Document match: matches) {
            Long id = match.getLong("_id");
            String replayUrl = match.getString("replay_url");
            executorService.submit(new ReplayTask(id, replayUrl, true));
        }
    }
}
