package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;


public class Board extends JPanel implements ActionListener
{
    Dimension d;
    Font smallfont = new Font("Helvetica", Font.BOLD, 14);

    FontMetrics fmsmall, fmlarge;
    Image ii;
    Color dotcolor = new Color(192, 192, 0);
    Color mazecolor;

    boolean ingame = false;
    boolean dying = false;
    boolean eingame = false;
    boolean edying = false;
    public static boolean isgameover = false;
    public static boolean isconnecting = false;

    final int blocksize = 24;
    int nrofblocksx = 15;
    int nrofblocksy = 15;
    int scrsizex = nrofblocksx * blocksize;
    int scrsizey = nrofblocksy * blocksize;
    final int pacanimdelay = 2;
    final int pacmananimcount = 4;
    final int maxghosts = 4;
    final int pacmanspeed = 6;

    int SendingMessage;
    int pacanimcount = pacanimdelay;
    int pacanimdir = 1;
    int pacmananimpos = 0;
    int nrofghosts = 4;
    int epacsleft, escore;
    int pacsleft, score;
    int deathcounter;
    int edeathcounter;
    int[] dx, dy;
    int[] ghostx, ghosty, ghostdx, ghostdy, ghostspeed;

    Image ghost;
    Image epacman;
    Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    Image pacman3up, pacman3down, pacman3left, pacman3right;
    Image pacman4up, pacman4down, pacman4left, pacman4right;
    Image epacman1, epacman2up, epacman2left, epacman2right, epacman2down;
    Image epacman3up, epacman3down, epacman3left, epacman3right;
    Image epacman4up, epacman4down, epacman4left, epacman4right;

    int pacmanx, pacmany, pacmandx, pacmandy;
    int epacmanx, epacmany;
    int epacmandx,epacmandy;
    int reqdx, reqdy, viewdx, viewdy;
    int eviewdx, eviewdy;
    public static int ereqdx, ereqdy;

    final int validspeeds[] = { 1, 2, 3, 4, 6, 8 };
    final int maxspeed = 6;
    public static Socket socketRead;
    public static Socket socketWrite;
    public static InputStream inStream;
    public static Scanner in;
    public static OutputStream outStream;
    public static PrintWriter out;
    public static boolean isMultiPlayer = false;
    int currentspeed = 3;
    short[] screendata;
    Timer timer;


    public Board()  {



        GetImages();

        addKeyListener(new TAdapter());
        screendata = new short[nrofblocksx * nrofblocksy];
        mazecolor = new Color(5, 100, 5);
        setFocusable(true);

        d = new Dimension(400, 400);

        setBackground(Color.black);
        setDoubleBuffered(true);

        ghostx = new int[maxghosts];
        ghostdx = new int[maxghosts];
        ghosty = new int[maxghosts];
        ghostdy = new int[maxghosts];
        ghostspeed = new int[maxghosts];
        dx = new int[4];
        dy = new int[4];
        timer = new Timer(40, this);
        timer.start();
    }

    public void addNotify() {
        super.addNotify();
        try {
            GameInit();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void DoAnim() {
        pacanimcount--;
        if (pacanimcount <= 0) {
            pacanimcount = pacanimdelay;
            pacmananimpos = pacmananimpos + pacanimdir;
            if (pacmananimpos == (pacmananimcount - 1) || pacmananimpos == 0)
                pacanimdir = -pacanimdir;
        }
    }


    public void PlayGame(Graphics2D g2d) throws FileNotFoundException {
        if (dying)
        {
            Death();
        } 
        else
            if(edying)
            {
                E_Death();
            }
            else
            {
                MovePacMan();
                E_MovePacMan();
                DrawPacMan(g2d);
                E_DrawPacMan(g2d);
                moveGhosts(g2d);
                CheckMaze();
            }
    }


    public void ShowIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, scrsizex / 2 - 30, scrsizey - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, scrsizex / 2 - 30, scrsizey - 100, 50);

        String s = "Press O to start on this computer";
        String m = "Press M to multiplayer";
        String con = "Connecting...";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);

        if(!isconnecting && !isgameover)
        {
            g2d.drawString(s, (scrsizex - metr.stringWidth(s)) / 2, scrsizey / 2 + 20);
            g2d.drawString(m, (scrsizey - metr.stringWidth(m))/2, scrsizey/2 + 40 );
        }
        if(isconnecting)
        {
            g2d.drawString(con, (scrsizex - metr.stringWidth(con)) / 2, scrsizey / 2 + 20);   
        }
        if(isgameover)
        {
            g2d.setColor(new Color(0, 32, 48));
            g2d.fillRect(50, scrsizex / 2 - 30, scrsizey - 100, 50);
            g2d.setColor(Color.white);
            g2d.drawRect(50, scrsizex / 2 - 30, scrsizey - 100, 50);
            String w = "You Win";
            String l = "You Lose";
            String c = "Press C to continue...";
            Font small1 = new Font("Helvetica", Font.BOLD, 14);
            FontMetrics metr1 = this.getFontMetrics(small1);
            if(score>escore)
                g2d.drawString(w, (scrsizex - metr1.stringWidth(w)) / 2, scrsizey / 2 + 20);
            else
                 g2d.drawString(l, (scrsizex - metr1.stringWidth(l)) / 2, scrsizey / 2 + 20);
            g2d.drawString(c, (scrsizex - metr1.stringWidth(c)) / 2, scrsizey / 2 + 40);
        }
    }

    public void DrawScore(Graphics2D g) {
        int i;
        String s;

        g.setFont(smallfont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, scrsizex / 2 - 70, scrsizey + 16);
        if(!isMultiPlayer)
        {
            for (i = 0; i < pacsleft; i++)
            {
                g.drawImage(pacman3left, i * 28 + 8, scrsizey + 1, this);
            }
        }
    }
    public void E_DrawScore(Graphics2D g) {
        int i;
        String s;

        g.setFont(smallfont);
        g.setColor(new Color(255, 0, 0));
        s = "Score: " + escore;
        g.drawString(s, scrsizex / 2 + 16, scrsizey + 16);
        if(!isMultiPlayer)
        {
            for (i = 0; i < epacsleft; i++)
            {
                g.drawImage(epacman3left,scrsizex -  i * 28 - 28, scrsizey + 1, this);
            }
        }
    }


    public void CheckMaze() throws FileNotFoundException {
        short i = 0;
        boolean finished = true;

        while (i < nrofblocksx * nrofblocksy && finished) {
            if ((screendata[i] & 48) != 0)
                finished = false;
            i++;
        }

        if (finished)
        {
            //score += 50;

            //if (nrofghosts < maxghosts)
            //    nrofghosts++;
            //if (currentspeed < maxspeed)
            //    currentspeed++;
            if(!isMultiPlayer)
                LevelInit();
            if(isMultiPlayer)
            {
                ingame = false;
                eingame = false;
                isgameover = true;
            }
        }
    }

    public void Death() {

        pacsleft--;
        if (pacsleft == 0)
        {
            ingame = false;
            isgameover = true;
        }
        LevelContinue();
    }
    public void E_Death()
     {

        epacsleft--;
        if (epacsleft == 0)
        {
            eingame = false;
            isgameover = true;
        }
        E_LevelContinue();
    }


    public void moveGhosts(Graphics2D g2d) {
        short i;
        int pos;
        int count;

        for (i = 0; i < nrofghosts; i++) {
            if (ghostx[i] % blocksize == 0 && ghosty[i] % blocksize == 0)
            {
                pos =
                ghostx[i] / blocksize + nrofblocksx * (int)(ghosty[i] / blocksize);

                count = 0;
                if ((screendata[pos] & 1) == 0 && ghostdx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screendata[pos] & 2) == 0 && ghostdy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if ((screendata[pos] & 4) == 0 && ghostdx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if ((screendata[pos] & 8) == 0 && ghostdy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if (count == 0) {
                    if ((screendata[pos] & 15) == 15) {
                        ghostdx[i] = 0;
                        ghostdy[i] = 0;
                    } else {
                        ghostdx[i] = -ghostdx[i];
                        ghostdy[i] = -ghostdy[i];
                    }
                } else {
                    count = (int)(Math.random() * count);
                    if (count > 3)
                        count = 3;
                    ghostdx[i] = dx[count];
                    ghostdy[i] = dy[count];
                }

            }
            ghostx[i] = ghostx[i] + (ghostdx[i] * ghostspeed[i]);
            ghosty[i] = ghosty[i] + (ghostdy[i] * ghostspeed[i]);
            DrawGhost(g2d, ghostx[i] + 1, ghosty[i] + 1);

            if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12) &&
                pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12) &&
                ingame)
            {

                dying = true;
                deathcounter = 64;

            }
            if (epacmanx > (ghostx[i] - 12) && epacmanx < (ghostx[i] + 12) &&
                epacmany > (ghosty[i] - 12) && epacmany < (ghosty[i] + 12) &&
                eingame)
            {

                edying = true;
                deathcounter = 64;
            }
        }
    }


    public void DrawGhost(Graphics2D g2d, int x, int y) {
        g2d.drawImage(ghost, x, y, this);
    }


    public void MovePacMan() {
        int pos;
        short ch;

        if (reqdx == -pacmandx && reqdy == -pacmandy) {
            pacmandx = reqdx;
            pacmandy = reqdy;
            viewdx = pacmandx;
            viewdy = pacmandy;
        }
        if (pacmanx % blocksize == 0 && pacmany % blocksize == 0) {
            pos =
            pacmanx / blocksize + nrofblocksx * (int)(pacmany / blocksize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short)(ch & 15);
                score++;
            }

            if (reqdx != 0 || reqdy != 0) {
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0) ||
                      (reqdx == 1 && reqdy == 0 && (ch & 4) != 0) ||
                      (reqdx == 0 && reqdy == -1 && (ch & 2) != 0) ||
                      (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    pacmandx = reqdx;
                    pacmandy = reqdy;
                    viewdx = pacmandx;
                    viewdy = pacmandy;
                }
            }

            // Check for standstill
            if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0) ||
                (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0) ||
                (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0) ||
                (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
                pacmandx = 0;
                pacmandy = 0;
            }
        }
        pacmanx = pacmanx + pacmanspeed * pacmandx;
        pacmany = pacmany + pacmanspeed * pacmandy;
    }
    public void E_MovePacMan() {
        int pos;
        short ch;

        if (ereqdx == -epacmandx && ereqdy == -epacmandy)
        {
            epacmandx = ereqdx;
            epacmandy = ereqdy;
            eviewdx = epacmandx;
            eviewdy = epacmandy;
        }
        if (epacmanx % blocksize == 0 && epacmany % blocksize == 0) {
            pos =
            epacmanx / blocksize + nrofblocksx * (int)(epacmany / blocksize);
            ch = screendata[pos];

            if ((ch & 16) != 0) {
                screendata[pos] = (short)(ch & 15);
                escore++;
            }

            if (ereqdx != 0 || ereqdy != 0) {
                if (!((ereqdx == -1 && ereqdy == 0 && (ch & 1) != 0) ||
                      (ereqdx == 1 && ereqdy == 0 && (ch & 4) != 0) ||
                      (ereqdx == 0 && ereqdy == -1 && (ch & 2) != 0) ||
                      (ereqdx == 0 && ereqdy == 1 && (ch & 8) != 0))) {
                    epacmandx = ereqdx;
                    epacmandy = ereqdy;
                    eviewdx = epacmandx;
                    eviewdy = epacmandy;
                }
            }

            // Check for standstill
            if ((epacmandx == -1 && epacmandy == 0 && (ch & 1) != 0) ||
                (epacmandx == 1 && epacmandy == 0 && (ch & 4) != 0) ||
                (epacmandx == 0 && epacmandy == -1 && (ch & 2) != 0) ||
                (epacmandx == 0 && epacmandy == 1 && (ch & 8) != 0)) {
                epacmandx = 0;
                epacmandy = 0;
            }
        }
        epacmanx = epacmanx + pacmanspeed * epacmandx;
        epacmany = epacmany + pacmanspeed * epacmandy;
    }


    public void DrawPacMan(Graphics2D g2d) {
        if (viewdx == -1)
            DrawPacManLeft(g2d);
        else if (viewdx == 1)
            DrawPacManRight(g2d);
        else if (viewdy == -1)
            DrawPacManUp(g2d);
        else
            DrawPacManDown(g2d);
    }
    public void E_DrawPacMan(Graphics2D g2d) {
        if (eviewdx == -1)
            E_DrawPacManLeft(g2d);
        else if (eviewdx == 1)
            E_DrawPacManRight(g2d);
        else if (eviewdy == -1)
            E_DrawPacManUp(g2d);
        else
            E_DrawPacManDown(g2d);
    }

    public void DrawPacManUp(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3up, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4up, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman2up, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }
    public void E_DrawPacManUp(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(epacman2up, epacmanx + 1, epacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(epacman3up, epacmanx + 1, epacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(epacman4up, epacmanx + 1, epacmany + 1, this);
            break;
        default:
            g2d.drawImage(epacman2up, epacmanx + 1, epacmany + 1, this);
            break;
        }
    }
    public void DrawPacManDown(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3down, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4down, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman2down, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }
    public void E_DrawPacManDown(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(epacman2down, epacmanx + 1, epacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(epacman3down, epacmanx + 1, epacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(epacman4down, epacmanx + 1, epacmany + 1, this);
            break;
        default:
            g2d.drawImage(epacman2down, epacmanx + 1, epacmany + 1, this);
            break;
        }
    }
    public void DrawPacManLeft(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3left, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4left, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman2left, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }
    public void E_DrawPacManLeft(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(epacman2left, epacmanx + 1, epacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(epacman3left, epacmanx + 1, epacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(epacman4left, epacmanx + 1, epacmany + 1, this);
            break;
        default:
            g2d.drawImage(epacman2left, epacmanx + 1, epacmany + 1, this);
            break;
        }
    }
    public void DrawPacManRight(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(pacman3right, pacmanx + 1, pacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(pacman4right, pacmanx + 1, pacmany + 1, this);
            break;
        default:
            g2d.drawImage(pacman2right, pacmanx + 1, pacmany + 1, this);
            break;
        }
    }
    public void E_DrawPacManRight(Graphics2D g2d) {
        switch (pacmananimpos) {
        case 1:
            g2d.drawImage(epacman2right, epacmanx + 1, epacmany + 1, this);
            break;
        case 2:
            g2d.drawImage(epacman3right, epacmanx + 1, epacmany + 1, this);
            break;
        case 3:
            g2d.drawImage(epacman4right, epacmanx + 1, epacmany + 1, this);
            break;
        default:
            g2d.drawImage(epacman2right, epacmanx + 1, epacmany + 1, this);
            break;
        }
    }


    public void DrawMaze(Graphics2D g2d) {
        short i = 0;
        int x, y;

        for (y = 0; y < scrsizey; y += blocksize) {
            for (x = 0; x < scrsizex; x += blocksize) {
                g2d.setColor(mazecolor);
                g2d.setStroke(new BasicStroke(2));

                if ((screendata[i] & 1) != 0) // draws left
                {
                    g2d.drawLine(x, y, x, y + blocksize - 1);
                }
                if ((screendata[i] & 2) != 0) // draws top
                {
                    g2d.drawLine(x, y, x + blocksize - 1, y);
                }
                if ((screendata[i] & 4) != 0) // draws right
                {
                    g2d.drawLine(x + blocksize - 1, y, x + blocksize - 1,
                                 y + blocksize - 1);
                }
                if ((screendata[i] & 8) != 0) // draws bottom
                {
                    g2d.drawLine(x, y + blocksize - 1, x + blocksize - 1,
                                 y + blocksize - 1);
                }
                if ((screendata[i] & 16) != 0) // draws point
                {
                    g2d.setColor(dotcolor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }
                i++;
            }
        }
    }

    public void GameInit() throws FileNotFoundException
    {
        pacsleft = 3;
        epacsleft = 3;
        score = 0;
        escore = 0;
        LevelInit();
        if(isMultiPlayer)
            nrofghosts = 0;
        else
            nrofghosts = 4;
        currentspeed = 3;
    }

    public void LevelInit() throws FileNotFoundException
    {
        int i;
        Scanner scanner = new Scanner(getClass().getResourceAsStream("leveldata/leveldata2.txt"));
        nrofblocksx = scanner.nextInt();
        nrofblocksy = scanner.nextInt();
        scrsizex = nrofblocksx * blocksize;
        scrsizey = nrofblocksy * blocksize;
        screendata = new short[nrofblocksx * nrofblocksy];
        for (i = 0; i < nrofblocksx * nrofblocksy; i++)
            screendata[i] = scanner.nextShort();
        S_LevelContinue();
    }

    public void LevelContinue()
    {
        short i;
        int dx = 1;
        int random;

//        for (i = 0; i < nrofghosts; i++) {
//            ghosty[i] = 4 * blocksize;
//            ghostx[i] = 4 * blocksize;
//            ghostdy[i] = 0;
//            ghostdx[i] = dx;
//            dx = -dx;
//            random = (int)(Math.random() * (currentspeed + 1));
//            if (random > currentspeed)
//                random = currentspeed;
//            ghostspeed[i] = validspeeds[random];
//        }

        pacmanx = 21 * blocksize;
        pacmany = 18 * blocksize;
        pacmandx = 0;
        pacmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }
    public void S_LevelContinue() {
        short i;
        int dx = 1;
        int random;

        for (i = 0; i < nrofghosts; i++) {
            ghosty[i] = 11 * blocksize;
            ghostx[i] = 13 * blocksize;
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            dx = -dx;
            random = (int)(Math.random() * (currentspeed + 1));
            if (random > currentspeed)
                random = currentspeed;
            ghostspeed[i] = validspeeds[random];
        }

        pacmanx = 21 * blocksize;
        pacmany = 18 * blocksize;
        pacmandx = 0;
        pacmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
        epacmanx = 21 * blocksize;
        epacmany = 18 * blocksize;
        epacmandx = 0;
        epacmandy = 0;
        ereqdx = 0;
        ereqdy = 0;
        eviewdx = -1;
        eviewdy = 0;
        edying = false;
    }
    public void E_LevelContinue()
     {
        short i;
        int dx = 1;
        int random;

//        for (i = 0; i < nrofghosts; i++) {
//            ghosty[i] = 4 * blocksize;
//            ghostx[i] = 4 * blocksize;
//            ghostdy[i] = 0;
//            ghostdx[i] = dx;
//            dx = -dx;
//            random = (int)(Math.random() * (currentspeed + 1));
//            if (random > currentspeed)
//                random = currentspeed;
//            ghostspeed[i] = validspeeds[random];
//        }

        epacmanx = 21 * blocksize;
        epacmany = 18 * blocksize;
        epacmandx = 0;
        epacmandy = 0;
        ereqdx = 0;
        ereqdy = 0;
        eviewdx = -1;
        eviewdy = 0;
        edying = false;
    }

    public void GetImages()
    {

      ghost = new ImageIcon(Board.class.getResource("pacpix/ghost.png")).getImage();
      epacman = new ImageIcon(Board.class.getResource("pacpix/epacman.png")).getImage();
      pacman1 = new ImageIcon(Board.class.getResource("pacpix/pacman.png")).getImage();
      pacman2up = new ImageIcon(Board.class.getResource("pacpix/up1.png")).getImage();
      pacman3up = new ImageIcon(Board.class.getResource("pacpix/up2.png")).getImage();
      pacman4up = new ImageIcon(Board.class.getResource("pacpix/up3.png")).getImage();
      pacman2down = new ImageIcon(Board.class.getResource("pacpix/down1.png")).getImage();
      pacman3down = new ImageIcon(Board.class.getResource("pacpix/down2.png")).getImage();
      pacman4down = new ImageIcon(Board.class.getResource("pacpix/down3.png")).getImage();
      pacman2left = new ImageIcon(Board.class.getResource("pacpix/left1.png")).getImage();
      pacman3left = new ImageIcon(Board.class.getResource("pacpix/left2.png")).getImage();
      pacman4left = new ImageIcon(Board.class.getResource("pacpix/left3.png")).getImage();
      pacman2right = new ImageIcon(Board.class.getResource("pacpix/right1.png")).getImage();
      pacman3right = new ImageIcon(Board.class.getResource("pacpix/right2.png")).getImage();
      pacman4right = new ImageIcon(Board.class.getResource("pacpix/right3.png")).getImage();
      epacman1 = new ImageIcon(Board.class.getResource("pacpix/epacman.png")).getImage();
      epacman2up = new ImageIcon(Board.class.getResource("pacpix/eup1.png")).getImage();
      epacman3up = new ImageIcon(Board.class.getResource("pacpix/eup2.png")).getImage();
      epacman4up = new ImageIcon(Board.class.getResource("pacpix/eup3.png")).getImage();
      epacman2down = new ImageIcon(Board.class.getResource("pacpix/edown1.png")).getImage();
      epacman3down = new ImageIcon(Board.class.getResource("pacpix/edown2.png")).getImage();
      epacman4down = new ImageIcon(Board.class.getResource("pacpix/edown3.png")).getImage();
      epacman2left = new ImageIcon(Board.class.getResource("pacpix/eleft1.png")).getImage();
      epacman3left = new ImageIcon(Board.class.getResource("pacpix/eleft2.png")).getImage();
      epacman4left = new ImageIcon(Board.class.getResource("pacpix/eleft3.png")).getImage();
      epacman2right = new ImageIcon(Board.class.getResource("pacpix/eright1.png")).getImage();
      epacman3right = new ImageIcon(Board.class.getResource("pacpix/eright2.png")).getImage();
      epacman4right = new ImageIcon(Board.class.getResource("pacpix/eright3.png")).getImage();

    }

    public void paint(Graphics g)
    {
      super.paint(g);

      Graphics2D g2d = (Graphics2D) g;

      g2d.setColor(Color.black);
      g2d.fillRect(0, 0, d.width, d.height);

      DrawMaze(g2d);
      DrawScore(g2d);
      E_DrawScore(g2d);
      DoAnim();
      if (ingame&&eingame)
        try
        {
            PlayGame(g2d);
        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
        }
      else
          {
            ShowIntroScreen(g2d);
          }
      g.drawImage(ii, 5, 5, this);
      Toolkit.getDefaultToolkit().sync();
      g.dispose();
    }
    class TAdapter extends KeyAdapter {
        public void keyPressed(KeyEvent e)
        {
          int key = e.getKeyCode();

          if (ingame)
          {
            if (key == KeyEvent.VK_LEFT)
            {
                if(isMultiPlayer)
                {
                        out.println("1");
                }
                reqdx=-1;
                reqdy=0;
            }
            else if (key == KeyEvent.VK_RIGHT)
            {
                if(isMultiPlayer)
                {
                        out.println("2");
                }
                reqdx=1;
                reqdy=0;
            }
            else if (key == KeyEvent.VK_UP)
            {
                if(isMultiPlayer)
                {
                        out.println("3");
                }
                reqdx=0;
                reqdy=-1;
            }
            else if (key == KeyEvent.VK_DOWN)
            {
                if(isMultiPlayer)
                {
                        out.println("4");
                }
                reqdx=0;
                reqdy=1;
            }
            if (key == KeyEvent.VK_C && isgameover)
            {
                
                isgameover = !isgameover;
            }
            if (key == KeyEvent.VK_A && !isMultiPlayer)
            {
              ereqdx=-1;
              ereqdy=0;
            }
            else if (key == KeyEvent.VK_D && !isMultiPlayer)
            {
              ereqdx=1;
              ereqdy=0;
            }
            else if (key == KeyEvent.VK_W && !isMultiPlayer)
            {
              ereqdx=0;
              ereqdy=-1;
            }
            else if (key == KeyEvent.VK_S && !isMultiPlayer)
            {
              ereqdx=0;
              ereqdy=1;
            }
            else if (key == KeyEvent.VK_ESCAPE && timer.isRunning())
            {
              ingame=false;
              eingame = false;
            }
            else if (key == KeyEvent.VK_P && !isMultiPlayer)
            {
                if (timer.isRunning())
                    timer.stop();
                else timer.start();
            }
          }
          else
          {
            if (key == 'O' || key == 'o')
          {
              ingame = true;
              eingame = true;
              isMultiPlayer = false;
                    try
                    {
                        GameInit();
                    } 
                    catch (FileNotFoundException ex)
                    {
                        Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
            else if( key == 'M' || key == 'm')
            {
                ingame = true;
                eingame = true;
                
                try
                {
                    socketWrite = new Socket("127.0.0.1", 1993);
                    socketRead = new Socket("127.0.0.1", 1993);
                    try
                    {
                       inStream = socketRead.getInputStream();
                    }
                    catch (IOException ex)
                    {
                       Logger.getLogger(CommunicationRead.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try
                    {
                       outStream = socketWrite.getOutputStream();
                    }
                    catch (IOException ex)
                    {
                       Logger.getLogger(CommunicationRead.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    in = new Scanner(inStream);
                    out = new PrintWriter(outStream,true);
                    Runnable read = new CommunicationRead();
                    Runnable write = new CommunicationWrite();
                    Thread tr = new Thread(read);
                    Thread tw = new Thread(write);
                    tw.start();
                    tr.start();
                    out.println("s");
//                    while(!isMultiPlayer)
//                    {
//                        isconnecting = true;
//                    }

                    isMultiPlayer = true;
                    isconnecting = false;
                    GameInit();

                }
                catch (UnknownHostException ex)
                {
                    Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
          }
      }

          public void keyReleased(KeyEvent e) {
              int key = e.getKeyCode();

              if (key == Event.LEFT || key == Event.RIGHT ||
                 key == Event.UP ||  key == Event.DOWN)
              {
                reqdx=0;
                reqdy=0;
              }
//               if (key == KeyEvent.VK_A || key == KeyEvent.VK_S ||
//                 key == KeyEvent.VK_D ||  key == KeyEvent.VK_W)
//              {
//                ereqdx=0;
//                ereqdy=0;
//              }
          }
      }

    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}