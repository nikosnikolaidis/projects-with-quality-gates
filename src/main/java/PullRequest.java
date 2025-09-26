public class PullRequest {
    String key;
    String qualityGateStatus;
    int bugs;
    int vulnerabilities;
    int codeSmells;

    public PullRequest(String key, String qualityGateStatus, int bugs, int vulnerabilities, int codeSmells) {
        this.key = key;
        this.qualityGateStatus = qualityGateStatus;
        this.bugs = bugs;
        this.vulnerabilities = vulnerabilities;
        this.codeSmells = codeSmells;
    }

    @Override
    public String toString() {
        return "PullRequest{" +
                "key='" + key + '\'' +
                ", qualityGateStatus='" + qualityGateStatus + '\'' +
                ", bugs=" + bugs +
                ", vulnerabilities=" + vulnerabilities +
                ", codeSmells=" + codeSmells +
                '}';
    }
}
