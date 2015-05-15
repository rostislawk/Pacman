package pacman_serv;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Pacman_Write extends Thread implements Runnable
{
    Socket incoming;
    int counter;
    public Pacman_Write(Socket inc, int count)
    {
        incoming = inc;
        counter = count;
    }
    public void run()
    {
        String t = "";
        System.out.println("Write est");
        while(true)
        {
            synchronized(Pacman_Server.mas)
            {
                if((Pacman_Server.mas[1-counter]!=null)&&(Pacman_Server.mas[1-counter]!=""))
                {
                    t  = new String(Pacman_Server.mas[1-counter]);
                    Pacman_Server.mas[1-counter] = "";
                }
            }
            if(!t.equals(null)&&!t.equals(""))
            {
                System.out.println("Write " + counter + " " + t);
                Pacman_Server.out[counter].println(t);
                t = "";
            }
            try
            {
                sleep(1);
            }
            catch (InterruptedException ex)
            {
                Logger.getLogger(Pacman_Write.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
