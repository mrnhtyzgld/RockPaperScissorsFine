import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;



/*
  ------------------------ Things to update in Rock Paper Scissors 2.0 ------------------------
        Nihat Emre Yüzügüldü

        Items don't have an algorithm for screen edges.
            -When an item goes from a side it can come from another side
            -Items can have a claustrophobic algorithm for staying away from edges of screen

        The solution to the movement algorithm of items is converted to int.
            -it needs to be float

        The programs' "move sgn from hunter & move sgn to prey" algorithm at line 141 can be better.

        The Program finds weighted aproximity of all items, but there should be an option to only focus on closest n items.
*/



// I extended JPanel because we need to override JPanels' PaintComponent method to avoid flickering.
public class RockPaperScissorsGUI extends JPanel {
    //these are item symbols, not to get confused with item objects.
    public final BufferedImage rock, paper, scissor;
    // every item type has STARTING_COUNT many items and item symbols are IMAGE_SIZE pixel each.
    public final int STARTING_COUNT = 30, IMAGE_SIZE = 30;
    // all the item objects
    private Item[] items = new Item[STARTING_COUNT*3];
    // those are fetched from swing
    public final int SCREEN_WIDTH, SCREEN_HEIGHT;

    /*
        Every item in the screen is a thread.
        Its algorithm finds weighted average of coordinates of every prey to that item.
        Weights are calculated with (1 / distance to this item).
        And we use the best coordinate to hunt a prey and excape from a hunter.
        In our case, Hunter, Prey etc. are only Rock Paper Scissors.
    */
    class Item extends Thread {
        int x, y;
        String type;

        public Item(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }

        // this is the algorithm
        public int[] findWeightedProximity(String type, int x, int y) {
            double bestX = 0, bestY = 0;
            double sumOfWeights = 0;
            ArrayList<Item> allItems = new ArrayList<>();
            for (int a = 0; a < items.length; a++) {
                if (items[a].type.equals(type)) {
                    allItems.add(items[a]);
                }
            }
            for (int a = 0; a < allItems.size(); a++) {

                // there is a bit of randomness
                double currWeight = 1 / Math.pow(Math.sqrt(Math.pow(allItems.get(a).x - x, 2) + Math.pow(allItems.get(a).y - y, 2)), 2) * new Random().nextFloat(1);
                sumOfWeights += currWeight;

                bestX += allItems.get(a).x * currWeight;
                bestY += allItems.get(a).y * currWeight;
            }
            if (sumOfWeights != 0) {
                bestX *= 1 / sumOfWeights;
                bestY *= 1 / sumOfWeights;
            }
            if (bestX == 0) bestX = -1;
            if (bestY == 0) bestY = -1;

            return new int[]{(int) bestX, (int) bestY};
        }


        public void run() {
            // I wanted there to be a delay when the program stars: 6 sec
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }


            while (true) {


                //when two objects collide if they are different then each other one needs to convert to other.
                for (int a = 0; a < STARTING_COUNT * 3; a++) {
                    if (items[a] != this && Math.abs(items[a].x - x) <= IMAGE_SIZE && Math.abs(items[a].y - y) <= IMAGE_SIZE) {
                        if (this.type.equals("Paper") && items[a].type.equals("Scissor")) {
                            this.type = "Scissor";
                        }
                        if (this.type.equals("Scissor") && items[a].type.equals("Rock")) {
                            this.type = "Rock";
                        }
                        if (this.type.equals("Rock") && items[a].type.equals("Paper")) {
                            this.type = "Paper";
                        }
                    }
                }


                // find the best location to hunt a prey
                int[] results = new int[2];
                if (type.equals("Paper"))
                    results = findWeightedProximity("Rock", this.x, this.y);
                if (type.equals("Rock"))
                    results = findWeightedProximity("Scissor", this.x, this.y);
                if (type.equals("Scissor"))
                    results = findWeightedProximity("Paper", this.x, this.y);

                // Find the best location to escape from a hunter
                int[] negResults = new int[2];
                if (type.equals("Paper"))
                    negResults = findWeightedProximity("Scissor", this.x, this.y);
                if (type.equals("Rock"))
                    negResults = findWeightedProximity("Paper", this.x, this.y);
                if (type.equals("Scissor"))
                    negResults = findWeightedProximity("Rock", this.x, this.y);

                //There is a bit of randomness
                /*
                    This part is not very cleverly designed.
                    We made movement 1-3 pixels because otherwise things look like teleporting.
                    We restricted items to move only integer size which does not allow better movement.
                    For example if item should go +1 x for escaping and -1 x for hunting, it stays in place.
                */
                if (results[0] != -1)
                    x += Math.signum(results[0] - x) * (new Random().nextFloat(4) - 0.5f);
                if (results[1] != -1)
                    y += Math.signum(results[1] - y) * (new Random().nextFloat(4) - 0.5f);

                if (negResults[0] != -1)
                    x -= Math.signum(negResults[0] - x) * (new Random().nextFloat(4) - 0.5f);
                if (negResults[1] != -1)
                    y -= Math.signum(negResults[1] - y) * (new Random().nextFloat(4) - 0.5f);


                // cannot go off-screen
                if (x < 1) x = 1;
                if (y < 1) y = 1;
                if (x > SCREEN_WIDTH - IMAGE_SIZE) x = SCREEN_WIDTH - IMAGE_SIZE;
                if (y > SCREEN_HEIGHT - IMAGE_SIZE) y = SCREEN_HEIGHT - IMAGE_SIZE;


                // otherwise game is too fast
                try {
                    sleep(16);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // This method is from stackoverflow to solve a problem I had.
    public BufferedImage toBufferedImage(Image img)
    {
        if (img instanceof BufferedImage)
        {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
    public RockPaperScissorsGUI() {


        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_WIDTH = (int)screenSize.getWidth();
        SCREEN_HEIGHT = (int)screenSize.getHeight()-70;


        JFrame myFrame = new JFrame();
        myFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        myFrame.setBackground(Color.LIGHT_GRAY);
        setBackground(Color.LIGHT_GRAY);
        myFrame.setSize(400,400);
        myFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        myFrame.add(this);

        try {
            URL rRes = getClass().getResource("rock.png");
            URL pRes = getClass().getResource("paper.png");
            URL sRes = getClass().getResource("scissors.png");
            rock =  this.toBufferedImage(ImageIO.read(rRes).getScaledInstance(IMAGE_SIZE,IMAGE_SIZE, BufferedImage.SCALE_FAST));
            paper =  this.toBufferedImage(ImageIO.read(pRes).getScaledInstance(IMAGE_SIZE,IMAGE_SIZE, BufferedImage.SCALE_FAST));
            scissor =  this.toBufferedImage(ImageIO.read(sRes).getScaledInstance(IMAGE_SIZE,IMAGE_SIZE, BufferedImage.SCALE_FAST));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        myFrame.setVisible(true); // start of the application

        Random r = new Random();
        for (int a = 0; a < STARTING_COUNT; a++)
            items[a] = new Item(r.nextInt(SCREEN_WIDTH), r.nextInt(SCREEN_HEIGHT), "Rock");

        for (int a = 0; a < STARTING_COUNT; a++)
            items[30 + a] = new Item(r.nextInt(SCREEN_WIDTH), r.nextInt(SCREEN_HEIGHT), "Paper");

        for (int a = 0; a < STARTING_COUNT; a++)
            items[60 + a] = new Item(r.nextInt(SCREEN_WIDTH), r.nextInt(SCREEN_HEIGHT), "Scissor");


        for (int a = 0; a < STARTING_COUNT*3; a++)
            items[a].start();

        //Is this efficient? No.
        while (true)
            repaint();

    }

    @Override
    protected void paintComponent(Graphics g) {
        // clear the screen
        super.paintComponent(g);

        //paint all items
        for (int a = 0; a < STARTING_COUNT*3; a++) {
            Item currItem = items[a];
            if (currItem.type.equals("Rock")) {
                g.drawImage(rock, currItem.x, currItem.y, this);
            }
            if (currItem.type.equals("Paper")) {
                g.drawImage(paper, currItem.x, currItem.y, this);
            }
            if (currItem.type.equals("Scissor")) {
                g.drawImage(scissor, currItem.x, currItem.y, this);
            }
        }
    }

    public static void main(String[] args) {
        new RockPaperScissorsGUI();
    }
}
