import java.io.*;
import java.util.ArrayList;
import java.util.Optional;

import org.json.simple.parser.JSONParser;

import org.json.simple.parser.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class userDataStore
{
    JSONObject obj = new JSONObject();
    JSONArray array = new JSONArray();
    JSONParser parser = new JSONParser();

    public Optional<String> newAccount(String userID, String credits) {
        String ErrorOutput = "No info";
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            //Used to trigger error if not found
            String fileValue = (String) obj.get("Credits:");
            ErrorOutput = "Account already exists";
        }
        catch (FileNotFoundException e)
        {
            try (FileWriter userAccount = new FileWriter(userID+".json")) {

                obj.put("ID:", userID);
                obj.put("Credits:", credits);

                array.clear();

                obj.put("Tickets:", array);
                userAccount.write(obj.toJSONString());

            } catch (IOException a) {
                a.printStackTrace();
            }
            ErrorOutput = null;

        }catch (IOException e)
        {
            ErrorOutput = "Failed IOException";
            e.printStackTrace();
        } catch (ParseException e)
        {
            e.printStackTrace();
            ErrorOutput = "Failed ParseException";
        }
        return Optional.ofNullable(ErrorOutput);
    }
    public String getID(String userID) {
        String fileValue = "";
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (String) obj.get("ID:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileValue = "User account created";
            this.newAccount(userID, "0");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        return fileValue;
    }
    public String getCredits(String userID) {
        String fileValue = "";
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (String) obj.get("Credits:");
        } catch (FileNotFoundException e) {
            fileValue = "0";
            this.newAccount(userID, "0");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        return fileValue;
    }
    public ArrayList<Integer> getTickets(String userID) {
        ArrayList<Integer> tickets = new ArrayList<>();
        JSONArray fileValue;
        try (FileReader reader = new FileReader(userID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (JSONArray) obj.get("Tickets:");
            tickets = fileValue;
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            this.newAccount(userID, "0");
            tickets = new ArrayList<>();

        } catch (IOException | ParseException e) {
            e.printStackTrace();
            //TODO process null option in main
            tickets = null;
        }
        return tickets;
    }
    public void editAccount(String userID, String credits) {
        ArrayList<Integer> currTickets = this.getTickets(userID);
        try (FileWriter userAccount = new FileWriter(userID+".json")) {

            obj.put("ID:", userID);
            obj.put("Credits:", credits);

            array.clear();
            array.addAll(currTickets);

            obj.put("Tickets:", array);
            userAccount.write(obj.toJSONString());

        } catch (IOException a) {
            a.printStackTrace();
        }
    }
    public void editTickets(String userID, ArrayList<Integer> tickets) {
        String currCredits = this.getCredits(userID);
        try (FileWriter userAccount = new FileWriter(userID+".json")) {

            obj.put("ID:", userID);
            obj.put("Credits:", currCredits);

            array.clear();
            array.addAll(tickets);
            //Inputted tickets arrayList should include new ticket numbers

            obj.put("Tickets:", array);
            userAccount.write(obj.toJSONString());

        } catch (IOException a) {
            a.printStackTrace();
        }
    }
    public void clearTickets() {
        File fileStore = new File("src/main/java/FileStore");
        File[] files = fileStore.listFiles();
        if(files!= null)
        {
            for(File file: files)
            {
                //Read JSON file and clear array
                try (FileReader reader = new FileReader(file))
                {
                    String userID = file.getName().replace(".json", "");
                    obj.put("ID:", userID);
                    obj.put("Credits:", this.getCredits(userID));
                    JSONObject obj = (JSONObject) parser.parse(reader);
                    array.clear();
                    obj.put("Tickets:", array);
                    try (FileWriter userAccount = new FileWriter(file)) {
                        userAccount.write(obj.toJSONString());
                    } catch (IOException a) {
                        a.printStackTrace();
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
            }
        }
        }
    }
    public String formatArrayList(ArrayList<Integer> tickets) {
        String output = "";
        for(int i = 0; i < tickets.size(); i++)
        {
            output += tickets.get(i) + "\n";
        }
        return output;
    }

}
