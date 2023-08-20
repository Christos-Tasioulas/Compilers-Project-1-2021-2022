import java_cup.runtime.*;
import java.io.*;
import java.lang.*;

class Parsexe {
    public static void main(String[] argv) throws Exception{

        FileInputStream fis = new FileInputStream(argv[0]);
        Parser p = new Parser(new Scanner(new InputStreamReader(fis)));
        p.parse();  

    }
}