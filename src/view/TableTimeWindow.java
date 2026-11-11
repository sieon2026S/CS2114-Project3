package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import Controller.HostController;
import model.Party;
import model.Restaurant;
import model.RestaurantListener;
import model.Table;

/**
 * The thin view for Table Time: the window the host sees.
 *
 * It only draws. Every button press is handed to the HostController, which
 * decides what to do and calls the model. The window never changes the
 * Restaurant itself; it reads getWaitingParties() and getTables() and
 * redraws whenever the model calls modelChanged().
 *
 * @author Sathvik Verma (covering the view for Charlie)
 * @version 2026.11.11
 */
public class TableTimeWindow
    extends JFrame
    implements RestaurantListener
{
    private static final int TABLE_COUNT = 8;
    private static final Color OPEN = new Color(46, 139, 87);
    private static final Color TAKEN = new Color(178, 34, 34);

    private final Restaurant restaurant;
    private HostController controller;

    private final JTextField nameField;
    private final JTextField sizeField;
    private final JButton addButton;
    private final JButton removeButton;
    private final JButton undoButton;
    private final JButton[] tableButtons;
    private final JTextArea waitlistArea;
    private final JLabel messageLabel;

    /**
     * Builds the window, registers it with the model, and draws the
     * starting state. Call setController() before the host can click.
     *
     * @param restaurant
     *            the model to display (read-only use)
     */
    public TableTimeWindow(Restaurant restaurant)
    {
        super("Table Time");
        if (restaurant == null)
        {
            throw new IllegalArgumentException("Restaurant must not be null.");
        }
        this.restaurant = restaurant;

        // ----- top: name + size + Add / Remove / Undo -----
        nameField = new JTextField(12);
        sizeField = new JTextField(3);
        addButton = new JButton("Add");
        removeButton = new JButton("Remove");
        undoButton = new JButton("Undo");

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Size:"));
        inputPanel.add(sizeField);
        inputPanel.add(addButton);
        inputPanel.add(removeButton);
        inputPanel.add(undoButton);

        // ----- center: one button per table -----
        JPanel tablePanel = new JPanel(new GridLayout(2, 4, 8, 8));
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            "Tables (click an open table to seat, a taken table to free)"));
        tableButtons = new JButton[TABLE_COUNT];
        for (int i = 0; i < TABLE_COUNT; i++)
        {
            JButton button = new JButton();
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setForeground(Color.WHITE);
            button.setFont(button.getFont().deriveFont(Font.BOLD));
            button.setPreferredSize(new Dimension(150, 60));
            tableButtons[i] = button;
            tablePanel.add(button);
        }

        // ----- right: the waitlist -----
        waitlistArea = new JTextArea(12, 18);
        waitlistArea.setEditable(false);
        waitlistArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane waitlistScroll = new JScrollPane(waitlistArea);
        waitlistScroll.setBorder(
            BorderFactory.createTitledBorder("Waitlist (arrival order)"));

        // ----- bottom: one message line -----
        messageLabel = new JLabel(" ");
        messageLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        setLayout(new BorderLayout(8, 8));
        add(inputPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(waitlistScroll, BorderLayout.EAST);
        add(messageLabel, BorderLayout.SOUTH);

        wireButtons();
        restaurant.addListener(this);
        modelChanged(); // draw the starting state

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Connects the controller. Kept separate from the constructor because
     * HostController needs the window to exist before it can be built.
     *
     * @param hostController
     *            the controller that handles every click
     */
    public void setController(HostController hostController)
    {
        if (hostController == null)
        {
            throw new IllegalArgumentException("Controller must not be null.");
        }
        this.controller = hostController;
    }

    /**
     * Hands every button press to the controller. The view itself never
     * checks input or calls a model method that changes anything.
     */
    private void wireButtons()
    {
        addButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (ready())
                {
                    controller.handleAdd(nameField.getText(),
                        sizeField.getText());
                }
            }
        });
        removeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (ready())
                {
                    controller.handleRemove(nameField.getText());
                }
            }
        });
        undoButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if (ready())
                {
                    controller.handleUndo();
                }
            }
        });
        for (int i = 0; i < TABLE_COUNT; i++)
        {
            final int tableNumber = i + 1;
            tableButtons[i].addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    if (ready())
                    {
                        controller.handleTableClick(tableNumber);
                    }
                }
            });
        }
    }

    /**
     * True once a controller is connected; otherwise says so on screen
     * instead of crashing.
     */
    private boolean ready()
    {
        if (controller == null)
        {
            showMessage("Not connected to a controller yet.");
            return false;
        }
        return true;
    }

    /**
     * Called by the Restaurant after every real change. Re-reads the
     * waitlist and tables and redraws both, so the screen never shows a
     * seated party as still waiting.
     */
    @Override
    public void modelChanged()
    {
        StringBuilder text = new StringBuilder();
        int position = 1;
        for (Party party : restaurant.getWaitingParties())
        {
            text.append(position).append(". ").append(party.getName())
                .append(" (").append(party.getSize()).append(")\n");
            position++;
        }
        if (position == 1)
        {
            text.append("Nobody waiting");
        }
        waitlistArea.setText(text.toString());

        int index = 0;
        for (Table table : restaurant.getTables())
        {
            if (index >= TABLE_COUNT)
            {
                break;
            }
            JButton button = tableButtons[index];
            String label = "<html><center>Table " + table.getNumber()
                + " (" + table.getCapacity() + " seats)<br>";
            if (table.isFree())
            {
                label += "Open";
                button.setBackground(OPEN);
            }
            else
            {
                label += table.getSeatedParty().getName();
                button.setBackground(TAKEN);
            }
            button.setText(label + "</center></html>");
            index++;
        }
    }

    /**
     * Shows one line of feedback (errors, who was seated, and so on).
     *
     * @param text
     *            the message; null shows nothing
     */
    public void showMessage(String text)
    {
        messageLabel.setText(text == null ? " " : text);
    }

    /**
     * Empties the name and size fields after a successful action.
     */
    public void clearInputs()
    {
        nameField.setText("");
        sizeField.setText("");
    }

    /**
     * Raw, unchecked text in the name field (the controller validates it).
     *
     * @return the name text
     */
    public String getNameText()
    {
        return nameField.getText();
    }

    /**
     * Raw, unchecked text in the size field (the controller parses it).
     *
     * @return the size text
     */
    public String getSizeText()
    {
        return sizeField.getText();
    }
}
