package pacman_serv;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Pacman_Read extends Thread implements Runnable
{
    Socket incoming;
    int counter;
    public Pacman_Read(Socket inc, int c)
    {
        incoming = inc;
        counter = c;
    }
    public void run()
    {
        System.out.println("Read est");
        try
        {
            try
            {
                boolean done = false;
                while(true)
                {
                    if(Pacman_Server.in[counter].hasNextLine())
                    {
                        String t = Pacman_Server.in[counter].nextLine();
                        synchronized(Pacman_Server.mas)
                        {
                            Pacman_Server.mas[counter] += t;
                        }
                        System.out.println("Read " + counter + " " + t);
                    }
                    try
                    {
                        sleep(1);
                    }
                    catch (InterruptedException ex)
                    {
                        Logger.getLogger(Pacman_Read.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            finally
            {
                incoming.close();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
