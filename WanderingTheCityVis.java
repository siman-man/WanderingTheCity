import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.security.*;

import javax.swing.*;

public class WanderingTheCityVis {
    static final int minSz = 50, maxSz = 500;
    static final double minProb = 0.05, maxProb = 0.5;
    static final double minChangeProb = 0.05, maxChangeProb = 0.2;

    int S;
    char[][] cityMapOld;
    char[][] cityMap;
    int[] startPos;
    // costs for 1 unit of walk and 1 call of look/guess
    int W, L, G;

    volatile int nLook, nGuess, nCorGuess, totalWalked;
    volatile boolean correct;

    // limits for # of calls for each function
    int maxLook, maxGuess, maxTotalWalk;
    
    volatile int[] curPos;
    boolean ok;                     // indicates that all actions were valid
    String errmes;                  // error message from actions
    // -----------------------------------------
    void generate(long seed) {
      try {
        // generate test case
        SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG"); 
        r1.setSeed(seed);
        S = r1.nextInt(maxSz - minSz + 1) + minSz;
        double blackProb = r1.nextDouble() * (maxProb - minProb) + minProb;
        double changeProb = r1.nextDouble() * (maxChangeProb - minChangeProb) + minChangeProb;
        double mixProb = 0.01;

        if (seed <= 3) {
            S = (int)seed * 20;
            blackProb = maxProb - minProb * 2 * seed;
        }
        if (seed == 4) {
            S = maxSz;
            blackProb = minProb;
        }

        // generate the map of the city
        cityMapOld = new char[S][S];
        cityMap = new char[S][S];
        int[] reps = new int[S];
        int repCnt = 0;
        for (int i = 2; i <= S; i++) {
            if ((S%i)==0) {
                reps[repCnt++] = i;
            }
        }

        int nB = 0;
        do {
            // select a repeatable dimension
            int repeatI = reps[r1.nextInt(repCnt)];
            int repeatJ = reps[r1.nextInt(repCnt)];

            // generate base city pattern
            for (int i = 0; i < repeatI; i++)
            for (int j = 0; j < repeatJ; j++) {
                cityMap[i][j] = r1.nextDouble() < blackProb ? 'X' : '.';
            }
            // repeat the pattern
            for (int i = 0; i < S; i++)
            for (int j = 0; j < S; j++) {
                cityMap[i][j] = cityMap[i%repeatI][j%repeatJ];
            }
            // randomly update parts of the city
            for (int i = 0; i < S; i++)
            for (int j = 0; j < S; j++) {
                if (r1.nextDouble() < mixProb) {
                    cityMap[i][j] = r1.nextDouble() < blackProb ? 'X' : '.';
                }
                if (cityMap[i][j] == 'X')
                    nB ++;
            }

            // make sure the map has at least 1 black and 1 white building
        } while (nB == 0 && nB != S * S);

        // copy the "old" map of the city to pass it to solution
        for (int i = 0; i < S; i++)
        for (int j = 0; j < S; j++) {
            cityMapOld[i][j] = cityMap[i][j];
        }

        // change the city map to "new"
        int nChange = 0;
        for (int i = 0; i < S; i++)
        for (int j = 0; j < S; j++) {
            if (r1.nextDouble() < changeProb) {
                cityMap[i][j] = (cityMap[i][j] == '.' ? 'X' : '.');
                ++nChange;
            }
        }

        // generate starting position (position = coordinates of the block to lower right of where you're standing)
        startPos = new int[2];
        for (int i = 0; i < 2; ++i)
            startPos[i] = r1.nextInt(S);

        // generate costs
        W = r1.nextInt(10) + 1;
        L = r1.nextInt(S / 2) + S / 2;
        G = r1.nextInt(S * S / 2) + S * S / 2;

        // initialize limits
        maxGuess = maxLook = S * S;
        maxTotalWalk = 16 * S * S;

        if (debug) {
            System.out.println("S = " + S);
            System.out.println("Probability of black building = " + blackProb);
            System.out.println("Starting position: (" + startPos[0] + "," + startPos[1] + ")");
            System.out.println("Old map:");
            for (int i = 0; i < S; i++)
                System.out.println(new String(cityMapOld[i]));
            System.out.println("Changed cells = " + nChange);
            System.out.println("Cost of walking W = " + W);
            System.out.println("Cost of look() L = " + L);
            System.out.println("Cost of guess() G = " + G);
            System.out.println();
        }
      }
      catch (Exception e) {
        System.err.println("An exception occurred while generating test case.");
        e.printStackTrace(); 
      }
    }
    // -----------------------------------------
    public String[] look() {
        // no input, so no validation
        nLook ++;
        if (nLook > maxLook) {
            errmes = "You can do at most " + maxLook + " look() calls.";
            ok = false;
            return new String[2];
        }
        char[][] seen = new char[2][2];
        for (int i = 0; i < 2; ++i)
        for (int j = 0; j < 2; ++j) {
            seen[i][j] = cityMap[(curPos[0] + S + i - 1) % S][(curPos[1] + S + j - 1) % S];
            if (vis)
                seenVis[(curPos[0] + S + i - 1) % S][(curPos[1] + S + j - 1) % S] = true;
        }
        String[] seenStr = new String[2];
        seenStr[0] = new String(seen[0]);
        seenStr[1] = new String(seen[1]);
        draw();
        return seenStr;
    }
    // -----------------------------------------
    boolean validShift(int shift) {
        if (shift <= -S || shift >= S) {
            errmes = "Value of shift (" + shift + ") must be between " + (-S+1) + " and " + (S-1) + ", inclusive.";
            ok = false;
            return false;
        }
        return true;
    }
    // -----------------------------------------
    int applyShift(int cur, int shift) {
        return (cur + S + shift) % S;
    }
    // -----------------------------------------
    public int walk(int[] shift) {
        if (shift == null || shift.length != 2) {
            errmes = "Shift must have exactly two elements";
            ok = false;
            return -1;
        }
        // restrict shifts to size of the city in each direction to avoid distance overflows
        for (int i = 0; i < 2; ++i)
            if (!validShift(shift[i]))
                return -1;
        for (int i = 0; i < 2; ++i)
            curPos[i] = applyShift(curPos[i], shift[i]);
        totalWalked += Math.abs(shift[0]) + Math.abs(shift[1]);
        if (totalWalked > maxTotalWalk) {
            errmes = "You can walk at most " + maxTotalWalk + " distance.";
            ok = false;
            return -1;
        }
        return 0;
    }
    // -----------------------------------------
    boolean validCoord(int coord) {
        if (coord < 0 || coord >= S) {
            errmes = "Value of coordinate (" + coord + ") must be between 0 and " + (S-1) + ", inclusive.";
            ok = false;
            return false;
        }
        return true;
    }
    // -----------------------------------------
    public int guess(int[] coord) {
        if (coord == null || coord.length != 2) {
            errmes = "Coord must have exactly two elements";
            ok = false;
            return -1;
        }
        for (int i = 0; i < 2; ++i)
            if (!validCoord(coord[i]))
                return -1;
        nGuess ++;
        if (nGuess > maxGuess) {
            errmes = "You can do at most " + maxGuess + " guess() calls.";
            ok = false;
            return -1;
        }
        
        boolean res = (startPos[0] == coord[0] && startPos[1] == coord[1]);
        if (res)
            nCorGuess++;
        correct |= res;
        if (vis)
            guessedVis[coord[0]][coord[1]] = true;
        return res ? 1 : 0;
    }
    // -----------------------------------------
    public double runTest(String seed) {
      try {
        generate(Long.parseLong(seed));
        
        curPos = new int[2];
        for (int i = 0; i < 2; ++i)
            curPos[i] = startPos[i];
        totalWalked = 0;
        nLook = 0;
        nGuess = 0;
        nCorGuess = 0;
        correct = false;
        ok = true;

        if (vis) {
            Wv = (twomaps ? S*2+2 : S)*SZ+20;
            Hv = S*SZ+40;
            seenVis = new boolean[S][S];
            guessedVis = new boolean[S][S];
            jf.setSize(Wv, Hv);
            jf.setVisible(true);
            draw();
        }
        
        String[] cityMapStr = new String[S];
        for (int i = 0; i < S; ++i)
            cityMapStr[i] = new String(cityMapOld[i]);

        // call the solution
        int ret = whereAmI(cityMapStr, W, L, G);

        if (!ok) {
            // something went wrong during library calls
            addFatalError(errmes);
            return -1;
        }

        if (!correct) {
            // solution failed to guess correctly before returning
            addFatalError("Failed to guess the starting position correctly.");
            return -1;
        }

        // ignore the return value
        // calculate the score based on nLook, nGuess and totalWalked
        if (debug) {
            addFatalError("Total distance walked = " + totalWalked);
            addFatalError("Number of look() calls = " + nLook);
            addFatalError("Number of incorrect guess() calls = " + (nGuess - nCorGuess));
        }
        double score = 0;
        score += (double)(W) * totalWalked;
        score += (double)(L) * nLook;
        score += (double)(G) * (nGuess - nCorGuess);
        return score;
      }
      catch (Exception e) { 
        System.err.println("An exception occurred while trying to get your program's results.");
        e.printStackTrace(); 
        return 0;
      }
    }
// ------------- visualization part ------------
    JFrame jf;
    Vis v;
    static String exec;
    static boolean debug;
    static boolean vis;
    static boolean twomaps;
    static Process proc;
    static int del;
    InputStream is;
    OutputStream os;
    BufferedReader br;
    static int SZ;
    volatile boolean ready;
    volatile boolean[][] seenVis;
    volatile boolean[][] guessedVis;
    // -----------------------------------------
    int whereAmI(String[] map, int W, int L, int G) throws IOException, NumberFormatException {
        int ret = 0;
        if (proc != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(map.length).append("\n");
            for (int i = 0; i < map.length; ++i)
                sb.append(map[i]).append("\n");
            sb.append(W).append("\n");
            sb.append(L).append("\n");
            sb.append(G).append("\n");
            os.write(sb.toString().getBytes());
            os.flush();

            // simulate function calls
            String request;
            while ((request = br.readLine()).startsWith("?")) {
                // get name of function invoked and read appropriate params
                if (request.equals("?look")) {
                    // no params
                    String[] looked = look();
                    if (!ok)
                        return 0;
                    os.write((looked[0] + "\n" + looked[1] + "\n").getBytes());
                    os.flush();
                } else if (request.equals("?walk")) {
                    // two integers
                    int[] shift = new int[2];
                    for (int i = 0; i < 2; ++i)
                        shift[i] = Integer.parseInt(br.readLine());
                    int walked = walk(shift);
                    if (!ok)
                        return 0;
                    os.write((walked + "\n").getBytes());
                    os.flush();
                } else if (request.equals("?guess")) {
                    // two integers
                    int[] coord = new int[2];
                    for (int i = 0; i < 2; ++i)
                        coord[i] = Integer.parseInt(br.readLine());
                    int guessed = guess(coord);
                    if (!ok)
                        return 0;
                    os.write((guessed + "\n").getBytes());
                    os.flush();
                } else {
                    ok = false;
                    errmes = "Unknown command: " + request;
                    return 0;
                }
                draw();
            }
        }
        return ret;
    }
    // -----------------------------------------
    void draw() {
        if (!vis) return;
        v.repaint();
        try { Thread.sleep(del); }
        catch (Exception e) { };
    }
    // -----------------------------------------
    int Wv, Hv;
    BufferedImage oldMap;
    void DrawOldMap() {
        oldMap = new BufferedImage(S * SZ + 1, S * SZ + 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D)oldMap.getGraphics();
        // buildings on the map
        for (int i = 0; i < S; ++i)
        for (int j = 0; j < S; ++j) {
            g2.setColor(new Color(cityMapOld[i][j] == 'X' ? 0x444444 : 0xDDDDDD));
            g2.fillRect(j * SZ, i * SZ, SZ, SZ);
        }
        // lines between buildings for streets
        g2.setColor(new Color(0xAAAAAA));
        for (int i = 0; i <= S; i++)
            g2.drawLine(0,i*SZ,S*SZ,i*SZ);
        for (int i = 0; i <= S; i++)
            g2.drawLine(i*SZ,0,i*SZ,S*SZ);
    }
    static BufferedImage deepCopy(BufferedImage source) {
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics g = b.getGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }
    // -----------------------------------------
    public class Vis extends JPanel implements WindowListener {
        public void paint(Graphics g) {
            // do painting here
            // draw the given (old) map once (and cache it)
            if (oldMap == null)
                DrawOldMap();
            BufferedImage bi = deepCopy(oldMap);
            Graphics2D g2 = (Graphics2D)bi.getGraphics();
            if (!twomaps) {
                // overlay seen parts of the map, using real black and white and with a border:
                // red for cells which differ from map and green for cells which match
                for (int i = 0; i < S; ++i)
                for (int j = 0; j < S; ++j) 
                    if (seenVis[i][j]) {
                        // border
                        g2.setColor(cityMap[i][j] == cityMapOld[i][j] ? Color.GREEN : Color.RED);
                        g2.fillRect(j * SZ + 1, i * SZ + 1, SZ - 1, SZ - 1);
                        // actual color of the building
                        g2.setColor(cityMap[i][j] == 'X' ? Color.BLACK : Color.WHITE);
                        g2.fillRect(j * SZ + 2, i * SZ + 2, SZ - 3, SZ - 3);
                    }
            }
            
            // mark positions which have been guessed incorrectly
            g2.setStroke(new BasicStroke(2.0f));
            for (int i = 0; i < S; ++i)
            for (int j = 0; j < S; ++j) 
                if (guessedVis[i][j]) {
                    g2.setColor(i == startPos[0] && j == startPos[1] ? Color.GREEN : Color.RED);
                    g2.drawLine(j * SZ - 3, i * SZ - 3, j * SZ + 3, i * SZ + 3);
                    g2.drawLine(j * SZ - 3, i * SZ + 3, j * SZ + 3, i * SZ - 3);
                }
            g.drawImage(bi, 0, 0, S * SZ + 1, S * SZ + 1, null);
            
            if (twomaps) {
                // draw currently seen parts of the map using fog of war - only show the cells observed
                BufferedImage bi2 = new BufferedImage(S * SZ + 1, S * SZ + 1, BufferedImage.TYPE_INT_RGB);
                Graphics2D g22 = (Graphics2D)bi2.getGraphics();
                g22.setColor(new Color(0xAAAAAA));
                g22.fillRect(0, 0, S * SZ, S * SZ);
                // buildings on the map
                for (int i = 0; i < S; ++i)
                for (int j = 0; j < S; ++j) 
                    if (seenVis[i][j]) {
                        g22.setColor(cityMap[i][j] == 'X' ? Color.BLACK : Color.WHITE);
                        g22.fillRect(j * SZ, i * SZ, SZ, SZ);
                    }
                // lines between buildings for streets
                g22.setColor(new Color(0xAAAAAA));
                for (int i = 0; i <= S; i++)
                    g22.drawLine(0,i*SZ,S*SZ,i*SZ);
                for (int i = 0; i <= S; i++)
                    g22.drawLine(i*SZ,0,i*SZ,S*SZ);

                g.drawImage(bi2, (S + 2) * SZ, 0, S * SZ + 1, S * SZ + 1, null);
            }
        }
        // -------------------------------------
        public Vis() {
            jf.addWindowListener(this);
        }
        // -------------------------------------
        //WindowListener
        public void windowClosing(WindowEvent e){ 
            if(proc != null)
                try { proc.destroy(); } 
                catch (Exception ex) { ex.printStackTrace(); }
            System.exit(0); 
        }
        public void windowActivated(WindowEvent e) { }
        public void windowDeactivated(WindowEvent e) { }
        public void windowOpened(WindowEvent e) { }
        public void windowClosed(WindowEvent e) { }
        public void windowIconified(WindowEvent e) { }
        public void windowDeiconified(WindowEvent e) { }
    }
    // -----------------------------------------
    public WanderingTheCityVis(String seed) {
      try {
        //interface for runTest
        if (vis)
        {   jf = new JFrame();
            v = new Vis();
            jf.getContentPane().add(v);
        }
        if (exec != null) {
            try {
                Runtime rt = Runtime.getRuntime();
                proc = rt.exec(exec);
                os = proc.getOutputStream();
                is = proc.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                new ErrorReader(proc.getErrorStream()).start();
            } catch (Exception e) { e.printStackTrace(); }
        }
        System.out.println("Score = "+runTest(seed));
        if (proc != null)
            try { proc.destroy(); } 
            catch (Exception e) { e.printStackTrace(); }
      }
      catch (Exception e) { e.printStackTrace(); }
    }
    // -----------------------------------------
    public static void main(String[] args) {
        String seed = "1";
        vis = true;
        twomaps = false;
        del = 100;
        SZ = 10;
        for (int i = 0; i<args.length; i++)
        {   if (args[i].equals("-seed"))
                seed = args[++i];
            if (args[i].equals("-exec"))
                exec = args[++i];
            if (args[i].equals("-delay"))
                del = Integer.parseInt(args[++i]);
            if (args[i].equals("-novis"))
                vis = false;
            if (args[i].equals("-size"))
                SZ = Integer.parseInt(args[++i]);
            if (args[i].equals("-debug"))
                debug = true;
            if (args[i].equals("-twomaps"))
                twomaps = true;
        }
        if (twomaps)
            vis = true;
        WanderingTheCityVis f = new WanderingTheCityVis(seed);
    }
    // -----------------------------------------
    void addFatalError(String message) {
        System.out.println(message);
    }
}

class ErrorReader extends Thread{
    InputStream error;
    public ErrorReader(InputStream is) {
        error = is;
    }
    public void run() {
        try {
            byte[] ch = new byte[50000];
            int read;
            while ((read = error.read(ch)) > 0)
            {   String s = new String(ch,0,read);
                System.out.print(s);
                System.out.flush();
            }
        } catch(Exception e) { }
    }
}
