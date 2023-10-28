package dev.isnow.masscanlinker;

public class MasscanLinker {

    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("START WITH: java -jar Client.jar masterIP vpsName");
            return;
        }
        new MasscanClient(args);
    }
}
