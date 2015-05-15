package pacman_serv;
import java.io.*;
import java.net.*;
import java.util.*;
public class Pacman_Server
{
    public static String []mas = new String[2];
    public static InputStream inStream[] = new InputStream[2];
    public static OutputStream outStream[] = new OutputStream[2];
    public static Scanner in[] = new Scanner[2];
    public static PrintWriter out[] = new PrintWriter[2];
    public static void main(String[] args)
    {
        try
        {
            int count = 0;
            ServerSocket s = new ServerSocket(1993);
            Pacman_Read r[] = new Pacman_Read[2];
            Pacman_Write w[] = new Pacman_Write[2];
            while(count<2)
            {
                Socket incomingRead = s.accept();
                Socket incomingWrite = s.accept();
                System.out.println("Spawing " + (count+1));
                inStream[count] = incomingRead.getInputStream();
                outStream[count] = incomingWrite.getOutputStream();
                in[count] = new Scanner(inStream[count]);
                out[count] = new PrintWriter(outStream[count], true);
                r[count] = new Pacman_Read(incomingRead, count);
                w[count] = new Pacman_Write(incomingWrite, count);
                count++;
            }
            if(count == 2)
            {
                r[0].start();
                w[0].start();
                r[1].start();
                w[1].start();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

}
