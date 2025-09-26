import java.util.ArrayList;
import java.util.List;

public class Project {
    String organization;
    String name;
    String key;
    String path;
    String qualityTool;
    Boolean QG;
    List<PullRequest> pullRequests;
    List<SonarRule> sonarRulesList;
    SonarRules sonarRules = new SonarRules();


    public Project(String organization, String name, String key) {
        this.organization = organization;
        this.name = name;
        this.key = key;
    }

    public Project(String fileLine, Boolean withQG) {
        if(!withQG) {
            String[] split = fileLine.split(";");
            this.organization = split[0];
            this.name = split[1];
            this.key = split[2];
        }
        else {
            String[] split = fileLine.split(";");
            this.organization = split[0];
            this.name = split[1];
            this.key = split[2];
            this.QG = Boolean.parseBoolean(split[3]);
            this.pullRequests = new ArrayList<>();
            this.sonarRulesList = new ArrayList<>();
        }
    }

    public Project(String organization, String name, String key, Boolean QG) {
        this.organization = organization;
        this.name = name;
        this.key = key;
        this.QG = QG;
    }

    public Project(String organization, String name, String key, String path, String qualityTool, Boolean QG) {
        this.organization = organization;
        this.name = name;
        this.key = key;
        this.path = path;
        this.qualityTool = qualityTool;
        this.QG = QG;
    }

    public void addPullRequests(PullRequest pullRequest){
        this.pullRequests.add(pullRequest);
    }


    @Override
    public String toString() {
        return "Project{" +
                "organization='" + organization + '\'' +
                ", name='" + name + '\'' +
                ", key='" + key + '\'' +
                ", path='" + path + '\'' +
                ", qualityTool='" + qualityTool + '\'' +
                ", QG=" + QG +
                '}';
    }
}
