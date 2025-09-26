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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SonarCloudIO {
    public SonarCloudIO() {
    }

    public void run(){
        File fileIds = new File("io-projects_id.csv");
        File fileProjects = new File("io-projects.csv");

        if(!fileProjects.exists()) {
            if (!fileIds.exists()) {
                getProjectIdsAndSave(fileIds);
            }

            List<Project> projects = getProjectsFromFile(fileIds,false);
            getQGInfoAndSave(projects);
        }

        if(fileProjects.exists() && fileHas4Columns(fileProjects)){
            List<Project> projects = getProjectsFromFile(fileProjects,true);
            getQGRulesOfProjectsAndSave(projects);
        }

        if(!(new File("io-projects-qg.csv").exists())){
            List<Project> projects = getQGProjectsFromFile(fileProjects);
            getQGExtraProjectInfoAndSave(projects);
        }
    }

    private void getQGRulesOfProjectsAndSave(List<Project> projects) {
        Unirest.setTimeouts(0, 0);
        for(Project project: projects) {
            System.out.println("project: " + project.key + "  index:" + projects.indexOf(project) + "/" + projects.size());
            if(!project.QG)
                continue;
            try {
                //make request to sonarcloud.io
                HttpResponse<String> response = Unirest.get("https://sonarcloud.io/api/qualitygates/get_by_project?organization="+project.organization+"&project="+project.key)
                        .asString();
                if (response.getStatus() == 429){
                    System.out.println("error: 429, waiting...");
                    //if too many requests, wait 1 hour and try one more time
                    try {
                        Thread.sleep(3600 * 1000); //1 hour
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HttpResponse<String> response1 = Unirest.get("https://sonarcloud.io/api/qualitygates/get_by_project?organization="+project.organization+"&project="+project.key)
                            .asString();
                    if(response.getStatus()!=200){
                        throw new RuntimeException("HttpResponseCode: "+response.getStatus());
                    }
                    response = response1;
                }
                else if (response.getStatus() == 404 || response.getStatus() == 403) {
                    continue;
                }
                else if (response.getStatus() != 200) {
                    throw new RuntimeException("HttpResponseCode: " + response.getStatus());
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
                    JSONObject jsonObject = (JSONObject) jobj.get("qualityGate");
                    String qualityGateName = jsonObject.get("name").toString();

                    getSonarRules(project, qualityGateName);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            System.out.println(project);
            try {
                Thread.sleep(5000); //5 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //write all projects and Rules info
        try {
            FileWriter writer = new FileWriter(new File("io-projects-rules.csv"));
            writer.write("organization;name;key;qg;rules" + System.lineSeparator());
            for(Project project: projects){
                writer.append(project.organization +";"+ project.name +";"+ project.key +";"+ project.QG +";"+ project.sonarRulesList + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //write all projects and Rules info
        try {
            FileWriter writer = new FileWriter(new File("io-projects-QG-rules.csv"));
            writer.write("organization;name;key;qg;new_security_rating;new_reliability_rating;new_maintainability_rating;"+
                            "new_security_review_rating;new_coverage;new_line_coverage;"+
                            "new_duplicated_lines_density;new_security_hotspots_reviewed;new_blocker_violations;"+
                            "new_critical_violations;new_major_violations;new_violations;"+
                            "new_bugs;new_code_smells;new_duplicated_blocks;"+
                            "new_duplicated_lines;new_vulnerabilities;new_technical_debt;"+
                            "new_sqale_debt_ratio;"+
                            "sqale_rating;security_rating;reliability_rating;"+
                            "maintainability_rating;security_review_rating;coverage;"+
                            "duplicated_lines_density;security_hotspots_reviewed;blocker_violations;"+
                            "critical_violations;violations;bugs;"+
                            "code_smells;vulnerabilities;duplicated_blocks;"+
                            "test_errors;test_failures" +
                            System.lineSeparator());
            for(Project project: projects) {
                writer.append(project.organization + ";" + project.name + ";" + project.key + ";" + project.QG +";"+
                            project.sonarRules.new_security_rating +";"+ project.sonarRules.new_reliability_rating +";"+ project.sonarRules.new_maintainability_rating +";"+
                            project.sonarRules.new_security_review_rating +";"+ project.sonarRules.new_coverage +";"+ project.sonarRules.new_line_coverage +";"+
                            project.sonarRules.new_duplicated_lines_density +";"+ project.sonarRules.new_security_hotspots_reviewed +";"+ project.sonarRules.new_blocker_violations +";"+
                            project.sonarRules.new_critical_violations +";"+ project.sonarRules.new_major_violations +";"+ project.sonarRules.new_violations +";"+
                            project.sonarRules.new_bugs +";"+ project.sonarRules.new_code_smells +";"+ project.sonarRules.new_duplicated_blocks +";"+
                            project.sonarRules.new_duplicated_lines +";"+ project.sonarRules.new_vulnerabilities +";"+ project.sonarRules.new_technical_debt +";"+
                            project.sonarRules.new_sqale_debt_ratio+";"+
                            project.sonarRules.sqale_rating +";"+ project.sonarRules.security_rating +";"+ project.sonarRules.reliability_rating +";"+
                            project.sonarRules.maintainability_rating +";"+ project.sonarRules.security_review_rating +";"+ project.sonarRules.coverage +";"+
                            project.sonarRules.duplicated_lines_density +";"+ project.sonarRules.security_hotspots_reviewed +";"+ project.sonarRules.blocker_violations +";"+
                            project.sonarRules.critical_violations +";"+ project.sonarRules.violations +";"+ project.sonarRules.bugs +";"+
                            project.sonarRules.code_smells +";"+ project.sonarRules.vulnerabilities +";"+ project.sonarRules.duplicated_blocks +";"+
                            project.sonarRules.test_errors +";"+ project.sonarRules.test_failures
                            + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSonarRules(Project project, String qualityGateName) {
        Unirest.setTimeouts(0, 0);
        List<SonarRule> sonarRulesList = new ArrayList<>();
        try {
            //make request to sonarcloud.io
            HttpResponse<String> response = Unirest.get("https://sonarcloud.io/api/qualitygates/list?organization="+project.organization)
                    .asString();
            if (response.getStatus() != 200) {
                throw new RuntimeException("HttpResponseCode: " + response.getStatus());
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
                JSONArray jsonArray = (JSONArray) jobj.get("qualitygates");
                for (Object o : jsonArray) {
                    JSONObject jsonObjectQG = (JSONObject) o;
                    if (jsonObjectQG.get("name").toString().equals(qualityGateName)) {
                        JSONArray jsonArrayRules = (JSONArray) jsonObjectQG.get("conditions");
                        for (Object o2 : jsonArrayRules) {
                            JSONObject jsonObjectRule = (JSONObject) o2;
                            //save in list of rules
                            sonarRulesList.add(new SonarRule(jsonObjectRule.get("metric").toString(), jsonObjectRule.get("op").toString(),
                                    jsonObjectRule.get("error").toString()));
                            //save in project
                            project.sonarRules.saveMetric(jsonObjectRule);
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }

        project.sonarRulesList = sonarRulesList;
    }

    private boolean fileHas4Columns(File fileProjects) {
        try {
            List<String> contents = Files.readAllLines(fileProjects.toPath(), StandardCharsets.ISO_8859_1);
            return contents.get(0).split(";").length==4;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getQGExtraProjectInfoAndSave(List<Project> projects) {
        Unirest.setTimeouts(0, 0);
        for(Project project: projects) {
            System.out.println("project: " + project.key + "  index:" + projects.indexOf(project) + "/" + projects.size());
            try {
                //make request to sonarcloud.io
                HttpResponse<String> response = Unirest.get("https://sonarcloud.io/api/project_pull_requests/list?project="+project.key)
                        .asString();
                if (response.getStatus() == 429){
                    System.out.println("error: 429, waiting...");
                    //if too many requests, wait 1 hour and try one more time
                    try {
                        Thread.sleep(3600 * 1000); //1 hour
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HttpResponse<String> response1 = Unirest.get("https://sonarcloud.io/api/project_pull_requests/list?project="+project.key)
                            .asString();
                    if(response.getStatus()!=200){
                        throw new RuntimeException("HttpResponseCode: "+response.getStatus());
                    }
                    response = response1;
                }
                else if (response.getStatus() == 404) {
                    continue;
                }
                else if (response.getStatus() != 200) {
                    throw new RuntimeException("HttpResponseCode: " + response.getStatus());
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
                    JSONArray jsonarr = (JSONArray) jobj.get("pullRequests");
                    for(int i=0; i<jsonarr.size(); i++){
                        JSONObject jsonobj_1 = (JSONObject) jsonarr.get(i);
                        String key = jsonobj_1.get("key").toString();
                        if(!jsonobj_1.containsKey("status"))
                            continue;
                        JSONObject jsonObjectStatus = (JSONObject) jsonobj_1.get("status");
                        String qualityGateStatus = jsonObjectStatus.get("qualityGateStatus").toString();
                        int bugs = Integer.parseInt(jsonObjectStatus.get("bugs").toString());
                        int vulnerabilities = Integer.parseInt(jsonObjectStatus.get("vulnerabilities").toString());
                        int codeSmells = Integer.parseInt(jsonObjectStatus.get("codeSmells").toString());

                        PullRequest pullRequest = new PullRequest(key,qualityGateStatus,bugs,vulnerabilities,codeSmells);
                        project.addPullRequests(pullRequest);
                    }
                    System.out.println("Project:"+project.key+"  prs:"+project.pullRequests.size());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            System.out.println(project);
            try {
                Thread.sleep(10000); //10 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //write all projects and QG info
        try {
            FileWriter writer = new FileWriter(new File("io-projects-qg.csv"));
            writer.write("projectKey;prKey;qualityGateStatus;bugs;vulnerabilities;codeSmells" + System.lineSeparator());
            for(Project project: projects){
                for(PullRequest pullRequest: project.pullRequests) {
                    writer.append(project.key +";"+ pullRequest.key +";"+ pullRequest.qualityGateStatus +";"+
                            pullRequest.bugs +";"+ pullRequest.vulnerabilities +";"+ pullRequest.codeSmells + System.lineSeparator());
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getQGInfoAndSave(List<Project> projects) {
        Unirest.setTimeouts(0, 0);
        for(Project project: projects){
            System.out.println("project: "+project.name +"  index:"+ projects.indexOf(project)+"/"+ projects.size());
            try {
                //make request to sonarcloud.io
                HttpResponse<String> response = Unirest.get("https://sonarcloud.io/api/project_pull_requests/list?project="+project.key)
                        .asString();
                if (response.getStatus() == 429){
                    System.out.println("error: 429, waiting...");
                    //if too many requests, wait 1 hour and try one more time
                    try {
                        Thread.sleep(3600 * 1000); //1 hour
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    HttpResponse<String> response1 = Unirest.get("https://sonarcloud.io/api/project_pull_requests/list?project="+project.key)
                            .asString();
                    if(response.getStatus()!=200){
                        throw new RuntimeException("HttpResponseCode: "+response.getStatus());
                    }
                    response = response1;
                }
                else if (response.getStatus() != 200) {
                    throw new RuntimeException("HttpResponseCode: " + response.getStatus());
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
                    JSONArray jsonarr = (JSONArray) jobj.get("pullRequests");
                    if (jsonarr.size() == 0) {
                        project.QG = false;
                    }
                    else {
                        project.QG = true;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            System.out.println(project);
            try {
                Thread.sleep(10000); //10 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //write all projects and QG info
        try {
            FileWriter writer = new FileWriter(new File("io-projects.csv"));
            writer.write("organization;name;key;qg" + System.lineSeparator());
            for(Project project: projects){
                writer.append(project.organization +";"+ project.name +";"+ project.key +";"+ project.QG + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Project> getQGProjectsFromFile(File fileProjects) {
        try {
            List<String> contents = Files.readAllLines(fileProjects.toPath(), StandardCharsets.ISO_8859_1);
            List<Project> projects = new ArrayList<>();
            for(int i=1; i<contents.size(); i++){
                Project p=new Project(contents.get(i), true);
                if(p.QG) {
                    projects.add(p);
                    System.out.println(p);
                }
            }
            return projects;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Project> getProjectsFromFile(File fileIds, Boolean hasQGInFile) {
        try {
            List<String> contents = Files.readAllLines(fileIds.toPath(), StandardCharsets.ISO_8859_1);
            List<Project> projects = new ArrayList<>();
            for(int i=1; i<contents.size(); i++){
                Project p=new Project(contents.get(i), hasQGInFile);
                projects.add(p);
                System.out.println(p);
            }
            return projects;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getProjectIdsAndSave(File fileIds) {
        Unirest.setTimeouts(0, 0);

        int page=0;
        List<Project> listIds = new ArrayList<>();
        while (true) {
            page= page + 1;
            System.out.println("page: "+page);
            try {
                //make request to sonarcloud.io
                HttpResponse<String> response = Unirest.get("https://sonarcloud.io/api/components/search_projects?&facets=ncloc&f=analysisDate&s=analysisDate&asc=false&filter=ncloc%3E100000&p=" + page + "&ps=500")
                        .asString();
                if (response.getStatus() != 200) {
                    throw new RuntimeException("HttpResponseCode: " + response.getStatus());
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
                    JSONArray jsonarr = (JSONArray) jobj.get("components");
                    if (jsonarr.size() == 0) {
                        break;
                    }
                    for(int i=0; i<jsonarr.size(); i++){
                        JSONObject jsonobj_1 = (JSONObject)jsonarr.get(i);
                        String organization = jsonobj_1.get("organization").toString();
                        String name = jsonobj_1.get("name").toString();
                        String key = jsonobj_1.get("key").toString();
                        Project project = new Project(organization, name, key);
                        listIds.add(project);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (UnirestException e) {
                e.printStackTrace();
            }
        }

        //write all ids to file
        try {
            FileWriter writer = new FileWriter(fileIds);
            writer.write("organization;name;key" + System.lineSeparator());
            for(Project pr: listIds){
                writer.append(pr.organization +";"+ pr.name +";"+ pr.key + System.lineSeparator());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
