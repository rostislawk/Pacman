package pacman;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
public class CommunicationRead implements Runnable
{
    public CommunicationRead()
    {
    }
    public void run()
    {
        try
        {
           String line;
                while(Board.in.hasNextLine())
                {
                    line = Board.in.nextLine();
                    System.out.print(line);
                    for(int i =0; i< line.length(); i++)
                    {
                        if(line.charAt(i)=='1')
                        {
                            Board.ereqdx = -1;
                            Board.ereqdy = 0;
                        }
                        else
                            if(line.charAt(i)=='2')
                            {
                                Board.ereqdx = 1;
                                Board.ereqdy = 0;
                            }
                            else
                                if(line.charAt(i)=='3')
                                {
                                    Board.ereqdx = 0;
                                    Board.ereqdy = -1;
                                }
                                else
                                    if(line.charAt(i)=='4')
                                    {
                                        Board.ereqdx = 0;
                                        Board.ereqdy = 1;
                                    }
                                    else
                                        if(line.charAt(i)=='s')
                                        {
                                            Board.isMultiPlayer = true;
                                        }
                                        else
                                        {
                                            Board.ereqdx = 0;
                                            Board.ereqdy = 0;
                                        }
                }
           }
        }
        finally
        {
            try
            {
                Board.socketRead.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(CommunicationRead.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
