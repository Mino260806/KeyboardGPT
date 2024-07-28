package tn.amin.keyboard_gpt.instruction.command;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Commands {

    public static String encodeCommands(List<GenerativeAICommand> commands) {
        JSONArray rootJson = new JSONArray();
        for (GenerativeAICommand command: commands) {
            try {
                rootJson.put(new JSONObject()
                        .accumulate("prefix", command.getCommandPrefix())
                        .accumulate("message", command.getTweakMessage()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        return rootJson.toString();
    }

    public static ArrayList<GenerativeAICommand> decodeCommands(String rawCommands) {
        ArrayList<GenerativeAICommand> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(rawCommands);
            for (int i=0; i<array.length(); i++) {
                JSONObject commandJson = (JSONObject) array.get(i);
                String prefix = commandJson.getString("prefix");
                String message = commandJson.getString("message");
                result.add(new SimpleGenerativeAICommand(prefix, message));
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
