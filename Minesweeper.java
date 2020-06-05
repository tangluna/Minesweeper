package minesweeper;

import javax.swing.*;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Class Minesweeper
 * @author Tarang Lunawat
 * Opens a JFrame in which a game of Minesweeper can be played
 */
public class Minesweeper
{
    // Game specification constants
    private final int boardDimensions = 10;
    private final int mineNum = 10;

    // Variables to keep track of how the game progresses
    private Cell[][] board;
    private Cell[] mines = new Cell[mineNum];
    private int flags = mineNum;
    private ArrayList<Cell> wronglyFlagged = new ArrayList<>();
    private boolean gameWon = false;
    private boolean gameLost = false;
    private boolean firstClick = true;
    private String playerName;

    // JFrame & associated graphics objects
    private CardLayout shuffler;
    private JFrame frame;
    private JPanel cards;
    private JPanel welcomeS;
    private JPanel gameS;
    private JPanel endS;
    private JLabel winLose;
    private JButton goToEndScreen;
    private JLabel gameStats;
    private JTextField playerNameInput;

    // Timing variables
    private int seconds = 0;
    private Timer t;

    /**
     * Main method, initializes and opens the game
     */
    public static void main (String[] args)
    {
        Minesweeper m = new Minesweeper();
        m.openGame();
    }
    
    /**
     * Constructor
     * Loads images for game, initializes JFrame and layouts
     */
    public Minesweeper() {
        // Loads all image icons displayed by game cells
        Cell.loadImages();
        
        // Game window
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(800, 800));
        frame.setTitle("Minesweeper");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // CardLayout to switch through different screens of the game
        shuffler = new CardLayout();
        cards = new JPanel(shuffler);
        setUpCards();
        Container pane = frame.getContentPane();
        pane.add(cards, BorderLayout.CENTER);
        
        frame.pack();
    }
    
    /**
     * Sets up all screens of the game (welcomeS, gameS, endS) by creating and adding respective elements
     */
    public void setUpCards() {
        // Welcome screen
        welcomeS = new JPanel();
        welcomeS.setLayout(null);

        JPanel wSDrawings = new NameJPanel();
        wSDrawings.setBounds(0, 0, 800, 100);
        welcomeS.add(wSDrawings);

        JLabel enterName = new JLabel("Enter name: ");
        enterName.setBounds(285, 120, 100, 30);
        welcomeS.add(enterName);

        playerNameInput = new JTextField("Anonymous");
        playerNameInput.setBounds(390, 120, 100, 30);
        welcomeS.add(playerNameInput);

        JLabel toPlay = new JLabel("To play:");
        toPlay.setBounds(285, 150, 100, 50);
        JLabel rightClickInstructions = new JLabel("Right click or ctrl+click to flag");
        rightClickInstructions.setBounds(300, 170, 200, 50);
        JLabel revealInstructions = new JLabel("Click to open");
        revealInstructions.setBounds(300, 190, 100, 50);
        welcomeS.add(toPlay);
        welcomeS.add(rightClickInstructions);
        welcomeS.add(revealInstructions);
        
        JLabel gameDescription = new JLabel("The objective of the game is to open all squares");
        JLabel gD2 = new JLabel("which do not contain a mine. Numbers are the ");
        JLabel gD3 = new JLabel("amount of surrounding squares that contain a");
        JLabel gD4 = new JLabel("mine. The game is won by opening all non-mine");
        JLabel gD5 = new JLabel("squares and lost if a mine is opened.");
        gameDescription.setBounds(285, 250, 350, 50);
        gD2.setBounds(285, 270, 350, 50);
        gD3.setBounds(285, 290, 350, 50);
        gD4.setBounds(285, 310, 350, 50);
        gD5.setBounds(285, 330, 350, 50);
        welcomeS.add(gameDescription);
        welcomeS.add(gD2);
        welcomeS.add(gD3);
        welcomeS.add(gD4);
        welcomeS.add(gD5);
        
        JButton go = new JButton("Go!");
        go.setBounds(370, 390, 60, 30);
        ActionListener goListener = new GoListener();
        go.addActionListener(goListener);
        welcomeS.add(go);

        cards.add(welcomeS, "Welcome Screen");
        
        // Game screen
        gameS = new JPanel();
        gameS.setLayout(null);

        JPanel gSDrawings = new NameJPanel();
        gSDrawings.setBounds(0, 0, 800, 100);
        gameS.add(gSDrawings);

        winLose = new JLabel();
        winLose.setBounds(350, 350, 500, 500);
        winLose.setFont(new Font(winLose.getFont().getName(), Font.PLAIN, 14));
        gameS.add(winLose);

        gameStats = new JLabel(getGameStatsString());
        gameStats.setBounds(250, 120, 400, 40);
        gameS.add(gameStats);

        goToEndScreen = new JButton("Next");
        goToEndScreen.setVisible(false);
        goToEndScreen.setBounds(370, 650, 60, 30);
        ActionListener nextListener = new NextListener();
        goToEndScreen.addActionListener(nextListener);
        gameS.add(goToEndScreen);

        cards.add(gameS, "Game Screen");

        // End screen
        endS = new JPanel();
        endS.setLayout(null);
        JPanel eSDrawings = new NameJPanel();
        eSDrawings.setBounds(0, 0, 800, 100);
        endS.add(eSDrawings);
        cards.add(endS, "End Screen");
    }

    /**
     * Inner class NameJPanel
     * Displays "Minesweeper" at top of each screen
     */
    class NameJPanel extends JPanel {
        public void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(new Font(g2.getFont().getFontName(), Font.PLAIN, 30));
            g2.drawString("Minesweeper", 307, 40);
        }
    }

    /**
     * Interface method, shows the welcome screen
     */
    public void openGame() {
        shuffler.show(cards, "Welcome Screen");
        frame.setVisible(true);
    }

    /**
     * Inner class GoListener
     * Records player name and advances game
     */
    class GoListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            playerName = playerNameInput.getText();
            playGame();
        }
    }

    /**
     * Interface method
     * @return true, signals game has began
     * Generates and draws game board, initializes timer to keep track of gameplay time
     */
    public boolean playGame() {
        generateBoard(false);
        TimeListener listener = new TimeListener();
        t = new Timer(1000, listener);
        t.start();
        shuffler.show(cards, "Game Screen");
        return true;
    }

    /**
     * Randomly selects positions for mines and increments surrounds to build board
     * Calls method to draw board on JFrame
     * @param alreadyDrawn true if board has already been added to the JFrame
     */
    public void generateBoard(boolean alreadyDrawn) {
        if (alreadyDrawn)
        {
            for (Cell[] cRow : board)
            {
                for (Cell c : cRow)
                {
                    c.setVisible(false);
                }
            }
        }
        board = new Cell[boardDimensions][boardDimensions];
        
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[1].length; j++) {
                board[i][j] = new Cell(j, i, 0, this);
            }
        }
        
        // Randomly selects mines and increments surroundings
        for (int i = 0; i < mineNum; i++) {
            int mX = (int) (Math.random() * boardDimensions);
            int mY = (int) (Math.random() * boardDimensions);
            if (board[mY][mX].getValue() != -1) {
                board[mY][mX].setValue(-1);
                mines[i] = board[mY][mX];
                incrementSurroundings(mY, mX);
            } else {
                // If selected coordinates is already a mine, selects another set of random coordinates
                i--;
            }
        }
        // Adds cells to JFrame if not already done
        drawBoard();
    }

    /**
     * Adds all cells to game JFrame
     */
    private void drawBoard() {
        for (Cell[] row : board) {
            for (Cell c : row) {
                c.setVisible(true);
                gameS.add(c);
            }
        }
    }

    /**
     * Increments all surrounding cell values of mine by 1
     * @param mY y position on board of mine
     * @param mX x position on board of mine
     */
    private void incrementSurroundings(int mY, int mX) {
        if (mX != 0 && mX != boardDimensions - 1) {
            // x is somewhere normal
            if (mY != 0 && mY != boardDimensions - 1) {
                // y is somewhere normal
                board[mY][mX - 1].incrementValueIfNotMine();
                board[mY][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX - 1].incrementValueIfNotMine();
                board[mY - 1][mX + 1].incrementValueIfNotMine();
                board[mY + 1][mX - 1].incrementValueIfNotMine();
                board[mY + 1][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX].incrementValueIfNotMine();
                board[mY + 1][mX].incrementValueIfNotMine();
            } else if (mY == 0) {
                // top row
                board[mY][mX - 1].incrementValueIfNotMine();
                board[mY][mX + 1].incrementValueIfNotMine();
                board[mY + 1][mX - 1].incrementValueIfNotMine();
                board[mY + 1][mX + 1].incrementValueIfNotMine();
                board[mY + 1][mX].incrementValueIfNotMine();
            } else {
                // bottom row
                board[mY][mX - 1].incrementValueIfNotMine();
                board[mY][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX - 1].incrementValueIfNotMine();
                board[mY - 1][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX].incrementValueIfNotMine();
            }
        } else if (mX == 0) {
            // left column
            if (mY != 0 && mY != boardDimensions - 1) {
                // y is somewhere normal
                board[mY][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX + 1].incrementValueIfNotMine();
                board[mY + 1][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX].incrementValueIfNotMine();
                board[mY + 1][mX].incrementValueIfNotMine();
            } else if (mY == 0) {
                // top row
                board[mY][mX + 1].incrementValueIfNotMine();
                board[mY + 1][mX + 1].incrementValueIfNotMine();
                board[mY + 1][mX].incrementValueIfNotMine();
            } else {
                // bottom row
                board[mY][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX + 1].incrementValueIfNotMine();
                board[mY - 1][mX].incrementValueIfNotMine();
            }
        } else {
            // right column
            if (mY != 0 && mY != boardDimensions - 1) {
                // y is somewhere normal
                board[mY][mX - 1].incrementValueIfNotMine();
                board[mY - 1][mX - 1].incrementValueIfNotMine();
                board[mY + 1][mX - 1].incrementValueIfNotMine();
                board[mY - 1][mX].incrementValueIfNotMine();
                board[mY + 1][mX].incrementValueIfNotMine();
            } else if (mY == 0) {
                // top row
                board[mY][mX - 1].incrementValueIfNotMine();
                board[mY + 1][mX - 1].incrementValueIfNotMine();
                board[mY + 1][mX].incrementValueIfNotMine();
            } else {
                // bottom row
                board[mY][mX - 1].incrementValueIfNotMine();
                board[mY - 1][mX - 1].incrementValueIfNotMine();
                board[mY - 1][mX].incrementValueIfNotMine();
            }
        }

    }

    /**
     * Inner class NextListener
     * Advances game to ending screen
     */
    class NextListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            endGame();
        }
    }

    /**
     * Inner class TimeListener
     * Increments seconds counter & updates corresponding JLabel
     */
    class TimeListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            seconds++;
            gameStats.setText(getGameStatsString());
        }
    }

    /**
     * Interface method
     * Updates stats JLabels and displays ending screen
     */
    public void endGame() {
        JLabel eSMessage = new JLabel();
        eSMessage.setBounds(300, 120, 400, 40);
        if (gameLost) {
            eSMessage.setText("Better luck next time, " + playerName);
        } else {
            eSMessage.setText("Congrats " + playerName + ", you won!");
        }
        endS.add(eSMessage);

        JLabel stats = new JLabel("Your stats: ");
        stats.setBounds(300, 150, 400, 40);
        endS.add(stats);

        JLabel wrongFlags = new JLabel("Wrong flags: " + wronglyFlagged.size() + " flag(s)");
        wrongFlags.setBounds(310, 170, 400, 40);
        endS.add(wrongFlags);

        JLabel time = new JLabel("Time taken: " + seconds + " seconds");
        time.setBounds(310, 190, 400, 40);
        endS.add(time);

        JLabel score = new JLabel("Score: " + getScore().substring(playerName.length()));
        score.setBounds(310, 210, 400, 40);
        endS.add(score);

        shuffler.show(cards, "End Screen");
    }

    /**
     * Interface method
     * @return String containing playerName and score, 0 if game lost
     */
    public String getScore() {
        if (!isWon()) {
            return playerName + " 0";
        } else {
            return playerName + " " + (seconds + 10 * wronglyFlagged.size());
        }
    }

    /**
     * Gets full updated string to be shown for game stats
     * @return full displayed game stats string
     */
    public String getGameStatsString() {
        return "Flags remaining: " + flags + "     Seconds elasped: " + seconds;
    }

    /**
     * Returns player name
     * @return player name
     */
    public String returnUsername() {
        return playerName;
    }

    /**
     * Returns boolean signifying if this is player's first click
     * @return true if first click
     */
    public boolean isFirstClick()
    {
        return firstClick;
    }
    
    /**
     * Sets boolean firstClick to false
     */
    public void firstClickOccured()
    {
        firstClick = false;
    }
    
    /**
     * Returns boolean signifying if game is lost
     * @return true if game is lost
     */
    public boolean isLost() {
        return gameLost;
    }

    /**
     * Returns boolean signifying if game is won
     * @return true if game is won
     */
    public boolean isWon() {
        return gameWon;
    }

    /**
     * Adds Cell to wronglyFlagged ArrayList
     * @param c Cell to add
     */
    public void addToWronglyFlagged(Cell c) {
        wronglyFlagged.add(c);
    }

    /**
     * Removes Cell from wronglyFlagged ArrayList
     * @param c Cell to remove
     */
    public void removeFromWronglyFlagged(Cell c) {
        wronglyFlagged.remove(c);

    }

    /**
     * Increments flag count and updates respective JLabel
     */
    public void incrementFlagCount() {
        flags++;
        gameStats.setText(getGameStatsString());
    }

    /**
     * Decrements flag count and updates respective JLabel
     */
    public void decrementFlagCount() {
        flags--;
        gameStats.setText(getGameStatsString());
    }

    /**
     * Regenerates board so that the first click cannot be a mine, then reveals clicked Cell
     * @param bX x-coordinate of clicked Cell in board double array
     * @param bY y-ccordinate of clicked Cell in board double array
     */
    public void firstClickMine(int bX, int bY)
    {
        while(board[bY][bX].getValue() == -1)
        {
            generateBoard(true);
        }
        board[bY][bX].reveal(true);
    }
    
    /**
     * Called when game is lost
     * Stops timer, reveals unflagged mines and wrong flags
     * Sets JLabel text to "Game Lost" and displays "Next" JButton
     */
    public void mineClicked() {
        t.stop();
        gameLost = true;
        winLose.setText("Game Lost!");
        goToEndScreen.setVisible(true);
        for (Cell c : mines) {
            if (!c.isFlagged()) {
                c.revealMine();
            }
        }
        for (Cell c : wronglyFlagged) {
            c.showCross();
        }
    }
    
    /**
     * Checks to see if the game is won
     * If so, stops timer, sets JLabel text to "Game Won" and displays "Next" KButton
     */
    public void checkEndConditions() {
        // game ends when: everything that's not a mine is opened
        boolean conditionsMet = true;
        for (Cell[] row : board) {
            for (Cell c : row) {
                if (!c.isOpened() && c.getValue() != -1) {
                    conditionsMet = false;
                }
            }
        }
        if (conditionsMet) {
            t.stop();
            winLose.setText("Game Won!");
            gameWon = true;
            goToEndScreen.setVisible(true);
        }
    }

    /**
     * Opens all surrounding cells to the one passed in, recurses using a breadth first search until cells that are not empty are opened
     * @param x x-coordinate of Cell to cascade around
     * @param y y-coordinate of Cell to cascade around
     */
    public void cascade(int x, int y) {
        // Queue to keep track of all cells for which to check and open surroundings
        Queue<Cell> toCascade = new LinkedList<>();
        toCascade.add(board[y][x]);
        
        // Repeats until there are no more Cells to check
        while (toCascade.size() != 0) {
            Cell cascading = toCascade.remove();
            int cX = cascading.getMyX();
            int cY = cascading.getMyY();
            Cell[] surroundings;
            // Adds Cells to check to an Array depending on where the main Cell is
            if (cX != 0 && cX != boardDimensions - 1) {
                // x is somewhere normal
                if (cY != 0 && cY != boardDimensions - 1) {
                    // y is somewhere normal
                    surroundings = new Cell[8];
                    surroundings[0] = board[cY][cX - 1];
                    surroundings[1] = board[cY][cX + 1];
                    surroundings[2] = board[cY - 1][cX - 1];
                    surroundings[3] = board[cY - 1][cX + 1];
                    surroundings[4] = board[cY + 1][cX - 1];
                    surroundings[5] = board[cY + 1][cX + 1];
                    surroundings[6] = board[cY - 1][cX];
                    surroundings[7] = board[cY + 1][cX];
                } else if (cY == 0) {
                    // top row
                    surroundings = new Cell[5];
                    surroundings[0] = board[cY][cX - 1];
                    surroundings[1] = board[cY][cX + 1];
                    surroundings[2] = board[cY + 1][cX - 1];
                    surroundings[3] = board[cY + 1][cX + 1];
                    surroundings[4] = board[cY + 1][cX];
                } else {
                    // bottom row
                    surroundings = new Cell[5];
                    surroundings[0] = board[cY][cX - 1];
                    surroundings[1] = board[cY][cX + 1];
                    surroundings[2] = board[cY - 1][cX - 1];
                    surroundings[3] = board[cY - 1][cX + 1];
                    surroundings[4] = board[cY - 1][cX];
                }
            } else if (cX == 0) {
                // left column
                if (cY != 0 && cY != boardDimensions - 1) {
                    // y is somewhere normal
                    surroundings = new Cell[5];
                    surroundings[0] = board[cY][cX + 1];
                    surroundings[1] = board[cY - 1][cX + 1];
                    surroundings[2] = board[cY + 1][cX + 1];
                    surroundings[3] = board[cY - 1][cX];
                    surroundings[4] = board[cY + 1][cX];
                } else if (cY == 0) {
                    // top row
                    surroundings = new Cell[3];
                    surroundings[0] = board[cY][cX + 1];
                    surroundings[1] = board[cY + 1][cX + 1];
                    surroundings[2] = board[cY + 1][cX];
                } else {
                    // bottom row
                    surroundings = new Cell[3];
                    surroundings[0] = board[cY][cX + 1];
                    surroundings[1] = board[cY - 1][cX + 1];
                    surroundings[2] = board[cY - 1][cX];
                }
            } else {
                // right column
                if (cY != 0 && cY != boardDimensions - 1) {
                    // y is somewhere normal
                    surroundings = new Cell[5];
                    surroundings[0] = board[cY][cX - 1];
                    surroundings[1] = board[cY - 1][cX - 1];
                    surroundings[2] = board[cY + 1][cX - 1];
                    surroundings[3] = board[cY - 1][cX];
                    surroundings[4] = board[cY + 1][cX];
                } else if (cY == 0) {
                    // top row
                    surroundings = new Cell[3];
                    surroundings[0] = board[cY][cX - 1];
                    surroundings[1] = board[cY + 1][cX - 1];
                    surroundings[2] = board[cY + 1][cX];
                } else {
                    // bottom row
                    surroundings = new Cell[3];
                    surroundings[0] = board[cY][cX - 1];
                    surroundings[1] = board[cY - 1][cX - 1];
                    surroundings[2] = board[cY - 1][cX];
                }
            }
            // Checks all surrounding Cells and opens them if they are not opened or are wrongly flagged
            for (Cell c : surroundings) {
                if (!c.isOpened() || wronglyFlagged.contains(c)) {
                    removeFromWronglyFlagged(c);
                    c.reveal(false);
                    if (c.getValue() == 0) {
                        toCascade.add(c);
                    }
                }
            }
        }
    }
}

/**
 * Private class Cell
 * @author Tarang
 * Represents a single square of the Minesweeper board
 */
class Cell extends JButton {
    // Final variables representing graphics position and icons
    public static final int CELL_SIZE = 40;
    public static final int BOARD_X_OFFSET = 190;
    public static final int BOARD_Y_OFFSET = 175;
    private static ImageIcon[] images; // -1 to 8 corresponds to value, 9 is flag, 10 is unopened, 11 is wrongly flagged cross

    // x and y positions in the board double array
    private int boardX;
    private int boardY;
    
    // variables monitoring status of Cell
    private int value; // -1 = mine, 0 to 8 represents actual numerical value    
    private boolean isOpened;
    private boolean isFlagged;
    
    // Reference to Minesweeper instance to which the Cell belongs
    private Minesweeper gameRef;
    
    /**
     * Constructor, sets Icon to unopened, adds action listeners, and intializes variables
     * @param x x-coordinate of Cell in board double array
     * @param y y-coordinate of Cell in board double array
     * @param value represents what the Cell should be when revealed
     * @param ref reference to the instance of Minesweeper to which the Cell belongs
     */
    public Cell(int x, int y, int value, Minesweeper ref) {
        // sets icon to unopened
        super(images[10]);
        
        // intializes values
        this.value = value;
        this.boardX = x;
        this.boardY = y;
        gameRef = ref;
        isOpened = false;
        isFlagged = false;
        
        // sets size
        this.setBounds(BOARD_X_OFFSET + x * CELL_SIZE, BOARD_Y_OFFSET + y * CELL_SIZE, CELL_SIZE, CELL_SIZE);

        // adds action listeners
        this.addActionListener(new CellListener());
        this.addMouseListener(new RightClickCellListener());
    }

    /**
     * Static method, fetches icon images from Wikimedia Commons URLs and creates one for wrong flags
     */
    public static void loadImages() {
        images = new ImageIcon[12];
        try {
            images[0] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/80/Minesweeper_0.svg/800px-Minesweeper_0.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[1] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/c/ca/Minesweeper_1.svg/1200px-Minesweeper_1.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[2] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/4/44/Minesweeper_2.svg/1200px-Minesweeper_2.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[3] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Minesweeper_3.svg/1200px-Minesweeper_3.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[4] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/4/4f/Minesweeper_4.svg/1200px-Minesweeper_4.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[5] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/4/46/Minesweeper_5.svg/1200px-Minesweeper_5.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[6] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/c/cc/Minesweeper_6.svg/1200px-Minesweeper_6.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[7] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/5/56/Minesweeper_7.svg/1200px-Minesweeper_7.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[8] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0d/Minesweeper_8.svg/1200px-Minesweeper_8.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[9] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Minesweeper_flag.svg/1200px-Minesweeper_flag.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
            images[10] = new ImageIcon(new ImageIcon(new URL(
                    "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c7/Minesweeper_unopened_square.svg/800px-Minesweeper_unopened_square.svg.png"))
                            .getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_FAST));
        } catch (MalformedURLException e) {
            // This should not happen unless you don't have internet connection - then the images won't load
            e.printStackTrace();
        }
        BufferedImage wrongFlag = new BufferedImage(CELL_SIZE, CELL_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = wrongFlag.createGraphics();
        graphics.setPaint(new Color(0, 51, 61));
        graphics.fillRect(0, 0, CELL_SIZE, CELL_SIZE);
        graphics.setPaint(new Color(252, 3, 3));
        graphics.drawLine(0, 0, CELL_SIZE, CELL_SIZE);
        graphics.drawLine(0, CELL_SIZE, CELL_SIZE, 0);
        images[11] = new ImageIcon(wrongFlag);
    }

    /**
     * Reveals icon of the Cell
     * Checks if game is lost or won
     * @param toCascade true if first call and should cascade around, false if called from a recursive cascading function
     */
    public void reveal(boolean toCascade) {
        // Does nothing if the game is already lost or won
        if (!gameRef.isLost() && !gameRef.isWon()) {
            if (this.value != -1) {
                // If not a mine
                // If not already false, flip first click boolean
                if (gameRef.isFirstClick())
                {
                    gameRef.firstClickOccured();
                }
                if (isFlagged) {
                    // Unflag
                    flag();
                }
                isOpened = true;
                this.setIcon(images[value]);
                // Cascade if empty and not a recursive call
                if (this.value == 0 && toCascade) {
                    gameRef.cascade(this.boardX, this.boardY);
                }
                // Check if the game is won
                gameRef.checkEndConditions();
            } else {
                // Mine is clicked
                if (gameRef.isFirstClick())
                {
                    // If this is the first click, flip first click boolean and reshuffle board
                    gameRef.firstClickOccured();
                    gameRef.firstClickMine(boardX, boardY);
                } else {
                    // If not the first click, mine has been hit
                    gameRef.mineClicked();
                }
            }
        }
    }

    /**
     * Flags or unflags Cell depending on current state
     */
    public void flag() {
        // Does nothing if game is already lost or won
        if (!gameRef.isLost() && !gameRef.isWon()) {
            // If not already false, flip first click boolean
            if (gameRef.isFirstClick())
            {
                gameRef.firstClickOccured();
            }
            if (this.getIcon().equals(images[10])) {
                // If unopened, flag
                this.setIcon(images[9]);
                isFlagged = true;
                if (this.value != -1) {
                    // If not a mine, add to wronglyFlagged ArrayList
                    gameRef.addToWronglyFlagged(this);
                }
                // Update game stats
                gameRef.decrementFlagCount();
            } else if (this.getIcon().equals(images[9])) {
                // If flagged, unflag
                this.setIcon(images[10]);
                isFlagged = false;
                // Remove from wronglyFlagged ArrayList (if present)
                gameRef.removeFromWronglyFlagged(this);
                // Update game stats
                gameRef.incrementFlagCount();
            }
        }
    }
    
    /*
     * Reveals all remaining mines after the game is lost
     * Creates a new, randomly colored icon for each mine
     */
    public void revealMine() {
        // Create image
        BufferedImage openMine = new BufferedImage(CELL_SIZE, CELL_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = openMine.createGraphics();
        
        // Select color and draw square and circle
        Color random = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
        graphics.setPaint(random.brighter());
        graphics.fillRect(0, 0, openMine.getWidth(), openMine.getHeight());
        graphics.setPaint(random.darker());
        graphics.fillOval(0 + CELL_SIZE / 4, 0 + CELL_SIZE / 4, CELL_SIZE / 2, CELL_SIZE / 2);
        
        // Set as icon
        this.setIcon(new ImageIcon(openMine));
    }
    
    /**
     * Shows icon signifying that Cell was wrongly flagged
     */
    public void showCross() {
        this.setIcon(images[11]);
    }
    
    /**
     * Returns boolean signifying if the Cell is flagged
     * @return true if Cell is flagged
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Returns x-position of Cell in board double array
     * @return x-position of Cell in board double array
     */
    public int getMyX() {
        return boardX;
    }

    /**
     * Returns y-position of Cell in board double array
     * @returny-position of Cell in board double array
     */
    public int getMyY() {
        return boardY;
    }

    /**
     * Returns boolean signifying if Cell is opened
     * @return true if Cell is opened
     */
    public boolean isOpened() {
        return isOpened;
    }

    /**
     * Returns value of Cell
     * @return value of Cell, -1 if mine, 0 - 8 for respective numbers
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets value of the Cell
     * @param value value of the cell, -1 if mine, 0-8 for corresponding numerical icon
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Increments value of Cell if Cell is not a mine
     */
    public void incrementValueIfNotMine() {
        if (value != -1) {
            value++;
        }
    }
    
    /**
     * Inner class RightClickCellListener
     * Listens for a right click, calls flag if so
     */
    class RightClickCellListener implements MouseListener {
        /**
         * Checks if right-click-button is pressed, if so calls flag
         */
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                Cell s = (Cell) e.getSource();
                s.flag();
            }
        }

        // Unused methods
        
        public void mouseClicked(MouseEvent e) {
            // do nothing
        }

        public void mouseEntered(MouseEvent e) {
            // do nothing
        }

        public void mouseExited(MouseEvent e) {
            // do nothing
        }

        public void mouseReleased(MouseEvent e) {
            // do nothing
        }
    }

    /**
     * Inner class CellListener
     * Checks for control-click or regular click, calls flag or reveal
     */
    class CellListener implements ActionListener {
        /**
         * Checks whether the control key was held, calls Cells flag if so, calls reveal otherwise
         */
        public void actionPerformed(ActionEvent event) {
            int mods = event.getModifiers();
            if ((mods & ActionEvent.CTRL_MASK) != 0) {
                // control key was held, flag
                Cell s = (Cell) event.getSource();
                s.flag();
            } else {
                // control key not held, reveal
                Cell s = (Cell) event.getSource();
                s.reveal(true);
            }
        }
    }
}
