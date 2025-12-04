package mv.sdd.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Lecture du fichier d'actions
public class ActionFileReader {
    public static List<Action> readActions(String filePath) throws IOException {
        List<Action> actions = new ArrayList<>();

        // TODO : Ajouter le code qui permet de lire et parser un fichier d'actions

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            String line;

            while ((line = reader.readLine()) != null){

                if(!line.isEmpty() && line.trim().startsWith("#"))
                    actions.add(ActionParser.parseLigne(line));
            }

        }catch (IOException e){

            e.printStackTrace();
        }

        return actions;
    }
}
