package cognitionmodel.models.upright;


import com.aparapi.Kernel;
import com.aparapi.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.List;

/**
 * An example Aparapi application which demonstrates Conways 'Game Of Life'.
 *
 * Original code from Witold Bolt's site https://github.com/houp/aparapi/tree/master/samples/gameoflife.
 *
 * Converted to use int buffer and some performance tweaks by Gary Frost
 *
 * @author Wiltold Bolt
 * @author Gary Frost
 * @version $Id: $Id
 */
public class GPUTest{

    /**
     * LifeKernel represents the data parallel algorithm describing by Conway's game of life.
     *
     * http://en.wikipedia.org/wiki/Conway's_Game_of_Life
     *
     * We examine the state of each pixel and its 8 neighbors and apply the following rules.
     *
     * if pixel is dead (off) and number of neighbors == 3 {
     *       pixel is turned on
     * } else if pixel is alive (on) and number of neighbors is neither 2 or 3
     *       pixel is turned off
     * }
     *
     * We use an image buffer which is 2*width*height the size of screen and we use fromBase and toBase to track which half of the buffer is being mutated for each pass. We basically
     * copy from getGlobalId()+fromBase to getGlobalId()+toBase;
     *
     *
     * Prior to each pass the values of fromBase and toBase are swapped.
     *
     */

    public static class LifeKernel extends Kernel{

        private static final int ALIVE = 0xffffff;

        private static final int DEAD = 0;

        private final int[] imageData;

        private final int width;

        private final int height;

        private final Range range;

        private int fromBase;

        private int toBase;

        public LifeKernel(int _width, int _height, BufferedImage _image) {
            imageData = ((DataBufferInt) _image.getRaster().getDataBuffer()).getData();
            width = _width;
            height = _height;

            final String executionMode = System.getProperty("com.aparapi.executionMode");
            if ((executionMode != null) && executionMode.equals("JTP")) {
                range = Range.create(width * height, 4);
            } else {
                range = Range.create(width * height);
            }

            System.out.println("range = " + range);
            fromBase = height * width;
            toBase = 0;
            setExplicit(true); // This gives us a performance boost

            /** draw a line across the image **/
/*            for (int i = *//*(width * (height / 2)) + *//*(width / 2); i < ((width * ((height ))) - (width / 10)); i += width) {
                imageData[i] = LifeKernel.ALIVE;
            }*/
            for (int i = 0; i < 500; i++)
                for (int j = 0; j < 20; j++) {
                imageData[(width / 2) + (width * (height / 2)) + i] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2)) - i] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2))] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2 + j)) + i] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2 + j)) - i] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2 + j))] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2 - j)) + i] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2 - j)) - i] = LifeKernel.ALIVE;
                imageData[(width / 2) + (width * (height / 2 - j))] = LifeKernel.ALIVE;
            }

            put(imageData); // Because we are using explicit buffer management we must put the imageData array
        }

        public void processPixel(int gid) {
            final int to = gid + toBase;
            final int from = gid + fromBase;
            final int x = gid % width;
            final int y = gid / width;

            if (((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1)))) {
                // This pixel is on the border of the view, just keep existing value
                imageData[to] = imageData[from];
            } else {
                // Count the number of neighbors.  We use (value&1x) to turn pixel value into either 0 or 1
                final int neighbors = (imageData[from - 1] & 1) + // EAST
                        (imageData[from + 1] & 1) + // WEST
                        (imageData[from - width - 1] & 1) + // NORTHEAST
                        (imageData[from - width] & 1) + // NORTH
                        (imageData[(from - width) + 1] & 1) + // NORTHWEST
                        (imageData[(from + width) - 1] & 1) + // SOUTHEAST
                        (imageData[from + width] & 1) + // SOUTH
                        (imageData[from + width + 1] & 1); // SOUTHWEST

                // The game of life logic
                if ((neighbors == 3) || ((neighbors == 2) && (imageData[from] == ALIVE))) {
                    imageData[to] = ALIVE;
                } else {
                    imageData[to] = DEAD;
                }

            }
        }

        @Override public void run() {
            final int gid = getGlobalId();
            processPixel(gid);
        }

        boolean sequential = Boolean.getBoolean("sequential");

        public void nextGeneration() {
            // swap fromBase and toBase
            final int swap = fromBase;
            fromBase = toBase;
            toBase = swap;
            if (sequential) {
                for (int gid = 0; gid < (width * height); gid++) {
                    processPixel(gid);
                }

            } else {
                execute(range);
            }

        }

    }

    static boolean running = false;

    /**
     * <p>main.</p>
     *
     * @param _args an array of {@link java.lang.String} objects.
     */
    public static void main(String[] _args) {


        Kernel kernel = new Kernel(){
            @Override public void run(){
            }
        };

        kernel.execute(1024);
        System.out.println("Execution mode = " +kernel.getExecutionMode());
        kernel.setExecutionModeWithoutFallback(Kernel.EXECUTION_MODE.GPU);


        final JFrame frame = new JFrame("Game of Life");
        final int width = Integer.getInteger("width", 1024 + 512 + 256 + 128);

        final int height = Integer.getInteger("height", 768 + 256);

        // Buffer is twice the size as the screen.  We will alternate between mutating data from top to bottom
        // and bottom to top in alternate generation passses. The LifeKernel will track which pass is which
        final BufferedImage image = new BufferedImage(width, height * 2, BufferedImage.TYPE_INT_RGB);

        final LifeKernel lifeKernel = new LifeKernel(width, height, image);

        // Create a component for viewing the offsecreen image
        @SuppressWarnings("serial") final JComponent viewer = new JComponent(){
            @Override public void paintComponent(Graphics g) {
                if (lifeKernel.isExplicit()) {
                    lifeKernel.get(lifeKernel.imageData); // We only pull the imageData when we intend to use it.
                    final List<ProfileInfo> profileInfo = lifeKernel.getProfileInfo();
                    if (profileInfo != null) {
                        for (final ProfileInfo p : profileInfo) {
                            System.out.print(" " + p.getType() + " " + p.getLabel() + " " + (p.getStart() / 1000) + " .. "
                                    + (p.getEnd() / 1000) + " " + ((p.getEnd() - p.getStart()) / 1000) + "us");
                        }
                    }
                }
                // We copy one half of the offscreen buffer to the viewer, we copy the half that we just mutated.
                if (lifeKernel.fromBase == 0) {
                    g.drawImage(image, 0, 0, width, height, 0, 0, width, height, this);
                } else {
                    g.drawImage(image, 0, 0, width, height, 0, height, width, 2 * height, this);
                }
            }
        };

        final JPanel controlPanel = new JPanel(new FlowLayout());
        frame.getContentPane().add(controlPanel, BorderLayout.SOUTH);

        final JButton startButton = new JButton("Start");

        startButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                running = true;
                startButton.setEnabled(false);
            }
        });
        controlPanel.add(startButton);
        controlPanel.add(new JLabel(lifeKernel.getTargetDevice().getShortDescription()));

        controlPanel.add(new JLabel("  Generations/Second="));
        final JLabel generationsPerSecond = new JLabel("0.00");
        controlPanel.add(generationsPerSecond);

        // Set the default size and add to the frames content pane
        viewer.setPreferredSize(new Dimension(width, height));
        frame.getContentPane().add(viewer);

        // Swing housekeeping
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        long start = System.currentTimeMillis();
        long generations = 0;
        while (!running) {
            try {
                Thread.sleep(10);
                viewer.repaint();
            } catch (final InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        while (true) {

            lifeKernel.nextGeneration(); // Work is performed here
            viewer.repaint(); // Request a repaint of the viewer (causes paintComponent(Graphics) to be called later not synchronous
            generations++;
            final long now = System.currentTimeMillis();
            if ((now - start) > 1000) {
                generationsPerSecond.setText(String.format("%5.2f", (generations * 1000.0) / (now - start)));
                start = now;
                generations = 0;
            }
        }

    }
}

