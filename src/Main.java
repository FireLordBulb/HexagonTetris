import HexagonTetris.HexagonTetris;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setSize(600, 800);
            frame.setTitle("Hexagon Tetris");
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new HexagonTetris());
        });
    }
}