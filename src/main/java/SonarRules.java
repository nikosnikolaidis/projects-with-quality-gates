import org.json.simple.JSONObject;

public class SonarRules {

    String new_security_rating = "";
    String new_reliability_rating = "";
    String new_maintainability_rating = "";
    String new_security_review_rating = "";
    String new_coverage = "";
    String new_line_coverage = "";
    String new_duplicated_lines_density = "";
    String new_security_hotspots_reviewed = "";
    String new_blocker_violations = "";
    String new_critical_violations = "";
    String new_major_violations = "";
    String new_violations = "";
    String new_bugs = "";
    String new_code_smells = "";
    String new_duplicated_blocks = "";
    String new_duplicated_lines = "";
    String new_vulnerabilities = "";
    String new_technical_debt = "";
    String new_sqale_debt_ratio = "";

    String sqale_rating = "";
    String security_rating = "";
    String reliability_rating = "";
    String maintainability_rating = "";
    String security_review_rating = "";
    String coverage = "";
    String duplicated_lines_density = "";
    String security_hotspots_reviewed = "";
    String blocker_violations = "";
    String critical_violations = "";
    String violations = "";
    String bugs = "";
    String code_smells = "";
    String vulnerabilities = "";
    String duplicated_blocks = "";
    String test_errors = "";
    String test_failures = "";

    public SonarRules() {
    }

    public void saveMetric(JSONObject jsonObjectRule) {
        String metric = jsonObjectRule.get("metric").toString();
        String error = jsonObjectRule.get("error").toString();
        //to add op
        switch (metric) {
            case "new_security_rating" -> new_security_rating = saveRatingMetric(error);
            case "new_reliability_rating" -> new_reliability_rating = saveRatingMetric(error);
            case "new_maintainability_rating" -> new_maintainability_rating = saveRatingMetric(error);
            case "new_security_review_rating" -> new_security_review_rating = saveRatingMetric(error);
            case "new_coverage" -> new_coverage = error;
            case "new_line_coverage" -> new_line_coverage = error;
            case "new_duplicated_lines_density" -> new_duplicated_lines_density = error;
            case "new_security_hotspots_reviewed" -> new_security_hotspots_reviewed = error;
            case "new_blocker_violations" -> new_blocker_violations = error;
            case "new_critical_violations" -> new_critical_violations = error;
            case "new_major_violations" -> new_major_violations = error;
            case "new_violations" -> new_violations = error;
            case "new_bugs" -> new_bugs = error;
            case "new_code_smells" -> new_code_smells = error;
            case "new_duplicated_blocks" -> new_duplicated_blocks = error;
            case "new_duplicated_lines" -> new_duplicated_lines = error;
            case "new_vulnerabilities" -> new_vulnerabilities = error;
            case "new_technical_debt" -> new_technical_debt = error;
            case "new_sqale_debt_ratio" -> new_sqale_debt_ratio = error;

            case "sqale_rating" -> sqale_rating = saveRatingMetric(error);
            case "security_rating" -> security_rating = saveRatingMetric(error);
            case "reliability_rating" -> reliability_rating = saveRatingMetric(error);
            case "maintainability_rating" -> maintainability_rating = saveRatingMetric(error);
            case "security_review_rating" -> security_review_rating = saveRatingMetric(error);
            case "coverage" -> coverage = error;
            case "duplicated_lines_density" -> duplicated_lines_density = error;
            case "security_hotspots_reviewed" -> security_hotspots_reviewed = error;
            case "blocker_violations" -> blocker_violations = error;
            case "critical_violations" -> critical_violations = error;
            case "violations" -> violations = error;
            case "bugs" -> bugs = error;
            case "code_smells" -> code_smells = error;
            case "vulnerabilities" -> vulnerabilities = error;
            case "duplicated_blocks" -> duplicated_blocks = error;
            case "test_errors" -> test_errors = error;
            case "test_failures" -> test_failures = error;
        }
    }



    private String saveRatingMetric(String value) {
        return switch (value) {
            case "1" -> "A";
            case "2" -> "B";
            case "3" -> "C";
            default -> "D";
        };
    }
}
