import HexagonTetris.HexagonTetris;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Main {
    private static final HexagonTetris hexagonTetris = new HexagonTetris();
    public static void main(String[] args) throws Exception {

        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setSize(660, 800);
            frame.setTitle("Hexagon Tetris");
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(hexagonTetris);
            frame.addKeyListener(new KeyAdapter(){
                public void keyPressed(KeyEvent event){
                    handleInput(event);
                }
            });
        });
    }
    private static void handleInput(KeyEvent event){
        // Gameplay inputs
        switch(event.getKeyCode()){
            case KeyEvent.VK_R -> hexagonTetris.restartGame();
            case KeyEvent.VK_P -> hexagonTetris.togglePause();
            case KeyEvent.VK_LEFT, KeyEvent.VK_A -> hexagonTetris.movePieceToSide(HexagonTetris.LEFT);
            case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> hexagonTetris.movePieceToSide(HexagonTetris.RIGHT);
            case KeyEvent.VK_Z -> hexagonTetris.rotatePiece(HexagonTetris.LEFT);
            case KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_X -> hexagonTetris.rotatePiece(HexagonTetris.RIGHT);
            case KeyEvent.VK_DOWN, KeyEvent.VK_S -> hexagonTetris.movePieceDownManually();
            case KeyEvent.VK_SPACE -> hexagonTetris.hardDrop();
        }
    }
}