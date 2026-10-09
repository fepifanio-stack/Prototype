import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class HotelManagementGame2 extends JFrame {

    

    static final boolean DEBUG = true;      

    static final String[] TYPE_NAMES = {"Single", "Double", "Deluxe"};
    static final int[] TYPE_PRICES = {1000, 1500, 2500};   
    static final int[] UNLOCK_COSTS = {1500, 2500, 4000};  

    static final int START_MONEY = 10000;
    static final int START_REPUTATION = 50;
    static final int MAX_QUEUE = 6;         
    static final int CLEAN_COST = 100;
    static final int ROOM_EXPENSE_PER_DAY = 40;   
    static final int CLEANER_SALARY = 150;        
    static final int BANKRUPT_LIMIT = -1000;

    static final String[] NAMES = {
        "Maria Santos", "Juan Dela Cruz", "Ana Reyes", "Miguel Garcia",
        "Sofia Lim", "Carlos Tan", "Bea Villanueva", "Rico Mendoza"
    };

    

    static class Guest {
        String name;
        int wantedType;  
        int nights;
        int patience;    

       
        @Override
        public String toString() {
            return name + " | wants " + TYPE_NAMES[wantedType] + " | "
                    + nights + " night(s) | patience " + patience;
        }
    }

    enum State { LOCKED, VACANT, OCCUPIED, DIRTY }

    static class Room {
        int number;
        int type;         
        State state;
        Guest guest;      
        int nightsLeft;   
    }

    
    int money = START_MONEY;
    int reputation = START_REPUTATION;   
    int day = 1;
    int guestsServed = 0;
    int cleanerLevel = 0;     
    int marketingLevel = 0;   

    final Random random = new Random();
    final ArrayList<Guest> queue = new ArrayList<>();
    final ArrayList<Room> rooms = new ArrayList<>();

    

    JLabel moneyLabel = new JLabel();
    JLabel dayLabel = new JLabel();
    JLabel reputationLabel = new JLabel();
    JLabel guestsLabel = new JLabel();
    JLabel messageLabel = new JLabel(" ");          

    DefaultListModel<Guest> queueModel = new DefaultListModel<>();
    JList<Guest> queueList = new JList<>(queueModel);

    JButton[] roomButtons = new JButton[12];       
    JButton cleanerButton = new JButton();
    JButton marketingButton = new JButton();
    JTextArea logArea = new JTextArea();

    

    public HotelManagementGame2() {
        setTitle("Sunrise Grand Hotel");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);   
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exitGame();
            }
        });

        createRooms();
        buildScreen();

               for (int i = 0; i < 3; i++) {
            queue.add(makeGuest());
        }

        log("Welcome to Sunrise Grand Hotel!");
        showMessage("Click a guest on the left, then click a green room.");
        updateScreen();
    }

   
    void createRooms() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                Room room = new Room();
                room.number = 101 + row * 4 + col;
                room.type = row;
               
                boolean startsOpen = (col == 0) || (row == 0 && col == 1);
                room.state = startsOpen ? State.VACANT : State.LOCKED;
                rooms.add(room);
            }
        }
    }

    
    void buildScreen() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildQueuePanel(), BorderLayout.WEST);
        add(buildRoomGrid(), BorderLayout.CENTER);
        add(buildRightPanel(), BorderLayout.EAST);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    JPanel buildTopBar() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        Font big = new Font("SansSerif", Font.BOLD, 18);
        JLabel[] labels = {moneyLabel, dayLabel, reputationLabel, guestsLabel};
        for (JLabel label : labels) {
            label.setFont(big);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            panel.add(label);
        }
        return panel;
    }

    JPanel buildQueuePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(300, 100));
        panel.add(new JLabel("Guest Queue (click one)"), BorderLayout.NORTH);

        queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        queueList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        panel.add(new JScrollPane(queueList), BorderLayout.CENTER);
        return panel;
    }

    JPanel buildRoomGrid() {
        JPanel grid = new JPanel(new GridLayout(3, 4, 8, 8));
        for (int i = 0; i < rooms.size(); i++) {
            final Room room = rooms.get(i);
            JButton button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("SansSerif", Font.BOLD, 13));
            button.addActionListener(e -> onRoomClicked(room));
            roomButtons[i] = button;
            grid.add(button);
        }
        return grid;
    }

    JPanel buildRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(280, 100));

        
        JPanel upgrades = new JPanel(new GridLayout(3, 1, 5, 5));
        upgrades.add(new JLabel("Upgrades"));
        cleanerButton.addActionListener(e -> buyCleaner());
        marketingButton.addActionListener(e -> buyMarketing());
        upgrades.add(cleanerButton);
        upgrades.add(marketingButton);
        panel.add(upgrades, BorderLayout.NORTH);

        
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    JPanel buildBottomBar() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(messageLabel, BorderLayout.CENTER);

        JButton endDayButton = new JButton("END DAY");
        endDayButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        endDayButton.addActionListener(e -> endDay());

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> exitGame());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(endDayButton);
        buttons.add(exitButton);
        panel.add(buttons, BorderLayout.EAST);
        return panel;
    }

    

    void updateScreen() {
        moneyLabel.setText("Cash: PHP " + String.format("%,d", money));
        moneyLabel.setForeground(money < 0 ? Color.RED : Color.BLACK);
        dayLabel.setText("Day " + day);
        reputationLabel.setText("Reputation: " + reputation + " / 100");
        guestsLabel.setText("Guests served: " + guestsServed);

        
        queueModel.clear();
        for (Guest guest : queue) {
            queueModel.addElement(guest);
        }

        
        for (int i = 0; i < rooms.size(); i++) {
            updateRoomButton(roomButtons[i], rooms.get(i));
        }

        
        cleanerButton.setText("Hire Cleaner  Lv " + cleanerLevel + "/3  -  " + upgradeText(cleanerLevel, 1500));
        marketingButton.setText("Marketing  Lv " + marketingLevel + "/3  -  " + upgradeText(marketingLevel, 2500));
    }

    String upgradeText(int level, int baseCost) {
        if (level >= 3) {
            return "MAX";
        }
        return "PHP " + baseCost * (level + 1);
    }

    void updateRoomButton(JButton button, Room room) {
        String top = "Room " + room.number + " (" + TYPE_NAMES[room.type] + ")";
        String middle;
        Color color;

        switch (room.state) {
            case VACANT:
                middle = "VACANT";
                color = new Color(170, 230, 170);   
                break;
            case OCCUPIED:
                middle = room.guest.name + " - " + room.nightsLeft + " night(s) left";
                color = new Color(170, 200, 245);   // blue
                break;
            case DIRTY:
                middle = "DIRTY - click to clean (PHP " + CLEAN_COST + ")";
                color = new Color(250, 215, 150);   
                break;
            default:
                middle = "LOCKED - PHP " + UNLOCK_COSTS[room.type];
                color = new Color(210, 210, 210);   
                break;
        }
        button.setText("<html><center>" + top + "<br>" + middle + "</center></html>");
        button.setBackground(color);
    }

    void log(String text) {
        logArea.append("Day " + day + ": " + text + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
        debug(text);
    }

    void showMessage(String text) {
        messageLabel.setText(text);
    }

    
    void debug(String text) {
        if (DEBUG) {
            System.out.println("[DEBUG] day=" + day + " money=" + money
                    + " rep=" + reputation + " queue=" + queue.size() + " | " + text);
        }
    }

    

    void onRoomClicked(Room room) {
        debug("Clicked room " + room.number + " (" + room.state + ")");

        switch (room.state) {
            case VACANT:
                Guest selected = queueList.getSelectedValue();
                if (selected == null) {
                    showMessage("Pick a guest from the queue first!");
                } else {
                    checkIn(room, selected);
                }
                break;

            case DIRTY:
                cleanRoom(room);
                break;

            case LOCKED:
                unlockRoom(room);
                break;

            case OCCUPIED:
                showMessage(room.guest.name + " is staying in Room " + room.number
                        + " for " + room.nightsLeft + " more night(s).");
                break;
        }
    }

    void checkIn(Room room, Guest guest) {
        int pay;
        int repChange;
        String result;

        if (room.type == guest.wantedType) {                 // perfect match
            pay = TYPE_PRICES[room.type] * guest.nights;
            repChange = 3;
            result = "Perfect match!";
        } else if (room.type > guest.wantedType) {           // better room than wanted
            pay = TYPE_PRICES[guest.wantedType] * guest.nights;   // guest pays for what they asked
            repChange = 5;
            result = "Free upgrade - the guest is thrilled!";
        } else {                                             // worse room than wanted
            pay = (int) (TYPE_PRICES[room.type] * 0.7 * guest.nights);
            repChange = -3;
            result = "Guest wanted a better room and paid less.";
        }

        money += pay;
        changeReputation(repChange);
        guestsServed++;

        room.state = State.OCCUPIED;
        room.guest = guest;
        room.nightsLeft = guest.nights;
        queue.remove(guest);

        log(guest.name + " checked into Room " + room.number + " (+PHP " + pay + "). " + result);
        showMessage(result + "  +PHP " + pay);
        updateScreen();
    }

    void cleanRoom(Room room) {
        if (money < CLEAN_COST) {
            showMessage("Not enough money to clean the room!");
            return;
        }
        money -= CLEAN_COST;
        room.state = State.VACANT;
        log("Cleaned Room " + room.number + " (-PHP " + CLEAN_COST + ").");
        showMessage("Room " + room.number + " is clean.");
        updateScreen();
    }

    void unlockRoom(Room room) {
        int cost = UNLOCK_COSTS[room.type];
        if (money < cost) {
            showMessage("You need PHP " + cost + " to unlock this room.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "Unlock Room " + room.number + " for PHP " + cost + "?",
                "Unlock room", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            money -= cost;
            room.state = State.VACANT;
            log("Unlocked Room " + room.number + " (-PHP " + cost + ").");
            updateScreen();
        }
    }

    void buyCleaner() {
        int cost = 1500 * (cleanerLevel + 1);
        if (cleanerLevel >= 3) {
            showMessage("Cleaner is already at max level.");
        } else if (money < cost) {
            showMessage("Not enough money for that upgrade!");
        } else {
            money -= cost;
            cleanerLevel++;
            log("Bought Hire Cleaner (level " + cleanerLevel + "). Cleans " + cleanerLevel * 2 + " rooms a day.");
            updateScreen();
        }
    }

    void buyMarketing() {
        int cost = 2500 * (marketingLevel + 1);
        if (marketingLevel >= 3) {
            showMessage("Marketing is already at max level.");
        } else if (money < cost) {
            showMessage("Not enough money for that upgrade!");
        } else {
            money -= cost;
            marketingLevel++;
            log("Bought Marketing (level " + marketingLevel + "). More guests will arrive.");
            updateScreen();
        }
    }

    void exitGame() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Leave the hotel? Progress is not saved.",
                "Exit Game", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    

    void endDay() {
        
        if (hasVacantRoom() && !queue.isEmpty()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Guests are waiting and you have vacant rooms.\nEnd the day anyway?",
                    "End Day?", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }

        day++;
        debug("---- NEW DAY " + day + " ----");

        processCheckouts();
        runCleaners();
        payExpenses();
        updateGuestPatience();
        addNewGuests();
        randomEvent();

        showMessage("Day " + day + " begins.");
        updateScreen();
        checkGameOver();
    }

    boolean hasVacantRoom() {
        for (Room room : rooms) {
            if (room.state == State.VACANT) {
                return true;
            }
        }
        return false;
    }

    
    void processCheckouts() {
        for (Room room : rooms) {
            if (room.state != State.OCCUPIED) {
                continue;
            }
            room.nightsLeft--;
            if (room.nightsLeft <= 0) {
                int tip = reputation * (1 + random.nextInt(3));   // better reputation = bigger tip
                money += tip;
                log(room.guest.name + " checked out of Room " + room.number + " (tip PHP " + tip + ").");
                room.state = State.DIRTY;
                room.guest = null;
            }
        }
    }

    
    void runCleaners() {
        int canClean = cleanerLevel * 2;
        int cleaned = 0;
        for (Room room : rooms) {
            if (canClean > 0 && room.state == State.DIRTY) {
                room.state = State.VACANT;
                canClean--;
                cleaned++;
            }
        }
        if (cleaned > 0) {
            log("Cleaners prepared " + cleaned + " room(s).");
        }
    }

    void payExpenses() {
        int openRooms = 0;
        for (Room room : rooms) {
            if (room.state != State.LOCKED) {
                openRooms++;
            }
        }
        int expenses = openRooms * ROOM_EXPENSE_PER_DAY + cleanerLevel * CLEANER_SALARY;
        money -= expenses;
        log("Paid daily expenses: PHP " + expenses + ".");
    }

    
    void updateGuestPatience() {
        for (int i = queue.size() - 1; i >= 0; i--) {
            Guest guest = queue.get(i);
            guest.patience--;
            if (guest.patience <= 0) {
                queue.remove(i);
                changeReputation(-4);
                log(guest.name + " got tired of waiting and left! (-4 reputation)");
            }
        }
    }

    void addNewGuests() {
        int count = 1 + random.nextInt(2 + marketingLevel);
        if (reputation >= 70) {
            count++;   // good reputation brings extra guests
        }
        for (int i = 0; i < count; i++) {
            if (queue.size() < MAX_QUEUE) {
                queue.add(makeGuest());
            } else {
                changeReputation(-1);
                log("The queue is full - a guest walked away. (-1 reputation)");
            }
        }
    }

    void randomEvent() {
        int roll = random.nextInt(100);
        if (roll < 10) {
            changeReputation(4);
            log("A guest posted a glowing review! (+4 reputation)");
        } else if (roll < 18) {
            money -= 300;
            log("A pipe burst - repairs cost PHP 300.");
        }
    }

    Guest makeGuest() {
        Guest guest = new Guest();
        guest.name = NAMES[random.nextInt(NAMES.length)];
        int roll = random.nextInt(10);
        guest.wantedType = roll < 5 ? 0 : roll < 9 ? 1 : 2;   // mostly Single, rarely Deluxe
        guest.nights = 1 + random.nextInt(4);
        guest.patience = 3;
        return guest;
    }

    void changeReputation(int amount) {
        reputation = Math.max(0, Math.min(100, reputation + amount));
    }

    void checkGameOver() {
        if (money >= BANKRUPT_LIMIT) {
            if (money < 0) {
                showMessage("You are in debt! Fill your rooms before it is too late.");
            }
            return;
        }
        JOptionPane.showMessageDialog(this,
                "Your hotel went bankrupt on day " + day + "!\nGuests served: " + guestsServed,
                "Game Over", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelManagementGame2().setVisible(true));
    }
}