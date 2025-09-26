import java.io.File;

public class Main {
    public static void main(String[] args){
        SonarCloudIO sonarCloudIO = new SonarCloudIO();
        sonarCloudIO.run();

        //if(args.length==1) {
//            GitHub gitHub = new GitHub("...");//args[0]);
//            gitHub.run();
        //}
    }

}
