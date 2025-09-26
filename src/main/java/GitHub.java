import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GitHub {
    String token;
    public GitHub(String arg) {
        token= arg;
    }

    public void run(){
        File fileQG = new File("github-projects_qg.csv");
        if (!fileQG.exists()) {
            getProjectsQGAndSave(fileQG);
        }

        File fileAnalysis = new File("github-projects_analysis.csv");
        if (!fileAnalysis.exists()) {
            getProjectsAnalysisAndSave(fileAnalysis);
        }
    }

    private void getProjectsAnalysisAndSave(File fileAnalysis) {
        List<Project> listProjects = new ArrayList<>();
        Unirest.setTimeouts(0, 0);

        //SonarQube
        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "sonarsource%2Fsonarqube-scan-action%20AND%20pull_request%20path%3A.github%2Fworkflows");
//        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "sonar-scanner%20AND%20sonar.host.url%20AND%20pull_request%20path%3A.github%2Fworkflows");
//        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "mvn%20AND%20org.sonarsource.scanner%20AND%20pull_request%20path%3A.github%2Fworkflows");
//        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "%22gradle%20sonar%22%20AND%20pull_request%20path%3A.github%2Fworkflows");
//        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "%22gradle%20sonarqube%22%20AND%20pull_request%20path%3A.github%2Fworkflows");
//        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "%22gradlew%20sonar%22%20AND%20pull_request%20path%3A.github%2Fworkflows");
//        makeRequestAndSave(fileAnalysis, listProjects, "sonar", false, "%22gradlew%20sonarqube%22%20AND%20pull_request%20path%3A.github%2Fworkflows");


        //Qodana
        makeRequestAndSave(fileAnalysis, listProjects, "qodana", false, "JetBrains/qodana-action%20AND%20pull_request%20path%3A.github%2Fworkflows");
    }

    private void getProjectsQGAndSave(File fileQG) {
        List<Project> listProjects = new ArrayList<>();
        Unirest.setTimeouts(0, 0);

        //SonarQube
        makeRequestAndSave(fileQG, listProjects, "sonar", true, "sonarsource%2Fsonarqube-quality-gate-action%20AND%20pull_request%20path%3A.github%2Fworkflows");

        //Qodana
        makeRequestAndSave(fileQG, listProjects, "qodana", true, "JetBrains/qodana-action%20AND%20fail-threshold%20AND%20pull_request%20language:YAML%20fork:true");
    }

    private void makeRequestAndSave(File file, List<Project> listProjects, String tool, Boolean qg, String q) {
        int page=0;
        while (true) {
            page = page + 1;
            System.out.println("page: " + page);
            try {
                //make request to GitHub
                HttpResponse<String> response = Unirest.get("https://api.github.com/search/code?q="+q+"&per_page=100&page="+ page)
                        .header("Authorization", "Bearer "+token)
                        .asString();
                if (response.getStatus() == 422)
                    break;
                if (response.getStatus() != 200) {
                    throw new RuntimeException("HttpResponseCode: " + response.getStatus() +" " +response.getBody());
                }

                //get result
                Scanner sc = new Scanner(response.getRawBody());
                String inline = "";
                while (sc.hasNext()) {
                    inline += sc.nextLine();
                }
                sc.close();

                //parse and keep ids
                try {
                    JSONParser parse = new JSONParser();
                    JSONObject jobj = (JSONObject) parse.parse(inline);
                    JSONArray jsonarr = (JSONArray) jobj.get("items");
                    if (jsonarr.size() == 0) {
                        break;
                    }
                    for(int i=0; i<jsonarr.size(); i++) {
                        JSONObject jsonobj_1 = (JSONObject) jsonarr.get(i);
                        String path = jsonobj_1.get("path").toString();
                        JSONObject jobj_1 = (JSONObject) jsonobj_1.get("repository");
                        String name = jobj_1.get("name").toString();
                        String key = jobj_1.get("full_name").toString();
                        JSONObject jobj_2 = (JSONObject) jobj_1.get("owner");
                        String organization = jobj_2.get("login").toString();
                        Project project = new Project(organization, name, key, path, tool, qg);
                        listProjects.add(project);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(10000); //5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //write all ids to file
        try {
            FileWriter writer = new FileWriter(file);
            if(!file.exists())
                writer.write("organization;name;key;path;tool;qg" + System.lineSeparator());
            for(Project pr: listProjects){
                writer.append(pr.organization +";"+ pr.name +";"+ pr.key +";"+ pr.path +";"+ pr.qualityTool +";"+ pr.QG + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
