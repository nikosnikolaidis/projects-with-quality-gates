public class SonarRule {
    String name;
    String op;
    String value;

    public SonarRule(String name, String op, String value) {
        this.name = name;
        this.op = op;
        this.value = value;
    }

    @Override
    public String toString() {
        return "SonarRule{" +
                "name='" + name + '\'' +
                ", op='" + op + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
