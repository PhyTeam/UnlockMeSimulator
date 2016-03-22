/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unlockme;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bbphuc
 */
public class UnlockMe {

    private static final long MEGABYTE = 1024L * 1024L;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws CloneNotSupportedException, IOException {
         long startTime = System.currentTimeMillis();


        InputStream is;
        if  (args.length < 1)
            // pass data by redirect system input 
            is = System.in;
        else {
            // Pass data by file
            File fin = new File(args[0]);
            is = new FileInputStream(fin);
        }
        State s = State.loadFromFile(is);
        DFS al = new DFS(s);
        al.Seacher(al.t);
        //long memory = Runtime.getRuntime().
        al.print();
        al.close();
        
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println(elapsedTime);
        
        // Get the Java runtime
        Runtime runtime = Runtime.getRuntime();
        // Run the garbage collector
        runtime.gc();
        // Calculate the used memory
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("Used memory is bytes: " + memory);
        System.out.println("Used memory is megabytes: "
            + bytesToMegabytes(memory));
    }
    
}
