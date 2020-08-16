import com.mongodb.BasicDBObject;
import org.bson.BSONObject;
import org.bson.BasicBSONObject;
import org.bson.BsonArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.OnEntityCreated;
import skadistats.clarity.processor.entities.OnEntityUpdated;
import skadistats.clarity.processor.entities.UsesEntities;
import skadistats.clarity.processor.runner.SimpleRunner;
import skadistats.clarity.source.InputStreamSource;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UsesEntities
public class ReplayParser {

    private final Logger log = LoggerFactory.getLogger(ReplayParser.class.getPackage().getClass());
    private final List<FieldPath> skillPaths = new ArrayList<>();
    private final List<Integer> skills = new ArrayList<>();
    private final List<FieldPath> playerPaths = new ArrayList<>();
    private final List<Integer> skillDraft = new ArrayList<>();
    private final List<String> skillPathStrings = IntStream.range(0, 48)
            .mapToObj(i -> String.format("m_pGameRules.m_AbilityDraftAbilities.%04d.m_nAbilityID", i))
            .collect(Collectors.toList());
    private final List<String> playerPathStrings = IntStream.range(0, 48)
            .mapToObj(i -> String.format("m_pGameRules.m_AbilityDraftAbilities.%04d.m_unPlayerID", i))
            .collect(Collectors.toList());

    private boolean isGameRules(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTAGamerulesProxy");
    }

    private void ensureFieldPaths(Entity e) {
        if (skillPaths.size() == 0) {
            for (String skillPath: skillPathStrings) {
                skillPaths.add(e.getDtClass().getFieldPathForName(skillPath));
            }
            for (String playerPath: playerPathStrings) {
                playerPaths.add(e.getDtClass().getFieldPathForName(playerPath));
            }
        }
    }

    @OnEntityCreated
    public void onCreated(Entity e) {
        if (!isGameRules(e)) {
            return;
        }
        ensureFieldPaths(e);
        for (FieldPath skillPath : skillPaths) {
            skills.add(e.getPropertyForFieldPath(skillPath));
        }

    }

    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
        if (!isGameRules(e)) {
            return;
        }
        ensureFieldPaths(e);
        boolean update = false;
        for (int i = 0; i < updateCount; i++) {
            if (playerPaths.contains(updatedPaths[i])) {
                int idx = playerPaths.indexOf(updatedPaths[i]);
                skillDraft.add(skills.get(idx));
            }
        }
    }

    public BasicDBObject run(String url) throws Exception {
        InputStreamSource input = new InputStreamSource(new ReplayDownloader(url).run());
        new SimpleRunner(input).runWith(this);

        Set<Integer> undrafted = new HashSet<Integer>(skills);
        Set<Integer> bd = new HashSet<Integer>(skillDraft);
        undrafted.removeAll(bd);

        BasicDBObject response = new BasicDBObject()
                .append("drafts", skillDraft)
                .append("skills", skills)
                .append("undrafted", undrafted);

        return response;
    }
}

