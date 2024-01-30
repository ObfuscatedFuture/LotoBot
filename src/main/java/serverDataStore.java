import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

public class serverDataStore
{
    JSONObject obj = new JSONObject();
    JSONParser parser = new JSONParser();

    //Server file will store, ticket price, draw date, max tickets, jackpot, and tickets sold
    public Optional<String> newServer(String serverID, int tixPrice, Date drawDate, int maxTix, int jackpot, int tixSold)
    {
        String ErrorOutput = "No info";
        try (FileReader reader = new FileReader(serverID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            //Used to trigger error if not found
            String fileValue = (String) obj.get("tixPrice:");
            ErrorOutput = "Account already exists";
        }
        catch (FileNotFoundException e)
        {
            try (FileWriter serverAccount = new FileWriter(serverID+".json")) {

                obj.put("tixPrice:", tixPrice);
                obj.put("drawDate", drawDate);
                obj.put("maxTix:", maxTix);
                obj.put("jackpot:", jackpot);
                obj.put("tixSold:", tixSold);

                serverAccount.write(obj.toJSONString());

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
    //TODO Add caching of vars in main method to reduce file reads
    public int getTixPrice(String serverID) throws IOException {
        int value = 0;
        try (FileReader reader = new FileReader(serverID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            value = (int) obj.get("tixPrice:");
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Server does not have a file");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            throw new IOException ("Unspecified Error, Contact Awesome_Wow");
        }
        return value;
    }
    public String getDrawDate(String serverID)
    {
        String fileValue = "";
        try (FileReader reader = new FileReader(serverID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (String) obj.get("drawDate:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //TODO output verbose error message
            fileValue = "Server does not have a file";
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        return fileValue;
    }
    public int getMaxTix(String serverID) throws IOException {
        int value = 0;
        try (FileReader reader = new FileReader(serverID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            value = (int) obj.get("maxTix:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //TODO output verbose error message
            throw new FileNotFoundException("Server does not have a file");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            throw new IOException("Unspecified Error, Contact Awesome_Wow");
        }
        return value;
    }
    public String getJackpot(String serverID)
    {
        String fileValue = "";
        try (FileReader reader = new FileReader(serverID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (String) obj.get("jackpot:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //TODO output verbose error message
            fileValue = "Server does not have a file";
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        return fileValue;
    }
    public String getTixSold(int serverID)
    {
        String fileValue = "";
        try (FileReader reader = new FileReader(serverID+".json"))
        {
            JSONObject obj = (JSONObject) parser.parse(reader);
            fileValue = (String) obj.get("tixSold:");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //TODO output verbose error message
            fileValue = "Server does not have a file";
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            fileValue = "Unspecified Error";
        }
        return fileValue;
    }

}
