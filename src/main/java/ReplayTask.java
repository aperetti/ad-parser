import com.mongodb.BasicDBObject;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class ReplayTask implements Runnable {

    private final String replayUrl;
    private final Long matchId;
    private final boolean verbose ;

    public ReplayTask(Long matchId, String replayUrl) {
        this.matchId = matchId;
        this.replayUrl = replayUrl;
        this.verbose = false;

    }

    public ReplayTask(Long matchId, String replayUrl, boolean verbose) {
        this.matchId = matchId;
        this.replayUrl = replayUrl;
        this.verbose = verbose;
    }

    public void run() {
        MongoDota db = new MongoDota();
        try {
            if (verbose) {
                System.out.println(String.format("Starting download of match: %d", matchId));
            }
            BasicDBObject abilityDraftData = new ReplayParser().run(replayUrl);
            db.updateMatchWithDraft(matchId, abilityDraftData);
            if (verbose) {
                System.out.println(String.format("Finished Parse of match: %d", matchId));
            }
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Failed to download, \n\t match: %s \n\treplay: %s", matchId, replayUrl));
            db.updateFailedDraftParse(matchId, "FileNotFound");
        } catch (CompressorException e) {
            System.out.println(String.format("Failed to decompress, \n\t match: %s \n\treplay: %s", matchId, replayUrl));
            db.updateFailedDraftParse(matchId, "CompressorException");
        } catch (Exception e) {
            System.out.println(String.format("Failed to parse, \n\t match: %s \n\treplay: %s", matchId, replayUrl));
            System.out.println("\t" + e.toString());
        }
    }

}
