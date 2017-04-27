package MagicTilesGame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

public class MagicTilesController implements MouseListener, Runnable, ActionListener{
    
    private MagicTiles parent;
    private MagicTilesView view;
    private MagicTilesModel model;
    
    public MagicTilesController(MagicTiles parent){
        this.parent = parent;
        this.view = this.parent.getView();
        this.model = this.parent.getModel();
        
        // Iniciar el Thread
        this.controllGame();
    }
    
    public void controllGame(){
        Thread hilo = new Thread(this);
        hilo.start();
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        int position = Integer.parseInt(((JButton) e.getSource()).getName());
        if(e.getSource() instanceof JButton){
            if(this.model.getState() == 3){
                if(this.model.getColors()[position] != MagicTilesModel.GREEN && this.model.getColors()[position] != MagicTilesModel.RED){
                    this.model.paintTile(position, 0xd8d8d8);
                    this.view.update();
                }
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        int position = Integer.parseInt(((JButton) e.getSource()).getName());
        if(e.getSource() instanceof JButton){
            if(this.model.getState() == 3){
                if(this.model.getColors()[position] != MagicTilesModel.GREEN && this.model.getColors()[position] != MagicTilesModel.RED){
                    this.model.paintTile(position, MagicTilesModel.WHITE);
                    this.view.update();
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

    @Override
    public void run() {
        try {
            while(this.model.isRunning()){
                if(this.model.getState() == 1){
                    this.model.paintAllBlack();
                    this.model.deactivateTiles();
                    this.model.setInstruction(0);               // Instruccion de esperar siguiente ronda
                    this.model.showTime();                      // Mostrar el tiempo
                    this.view.update();
                    for(int i = this.model.getTime(); i > 0; i--){
                        this.view.update();
                        this.model.subtractTime();
                        Thread.sleep(1000);
                    }
                    this.model.hideTime();
                    this.model.setInstruction(3);
                    this.view.update();
                    this.model.resetTime();
                    this.model.setState(2);
                }else if(this.model.getState() == 2){
                    this.model.generateSequence();
                    
                    int[] sequence = this.model.getSequence();
                    for(int i = 0; i < sequence.length; i++){
                        this.model.paintTile(sequence[i], MagicTilesModel.WHITE);
                        this.view.update();
                        Thread.sleep(1000);
                    }
                    
                    // Dar 5 segundos para que el usuario memorize la secuencia
                    this.model.setInstruction(1);
                    this.model.showTime();
                    for(int i = this.model.getTime(); i > 0; i--){
                        this.view.update();
                        this.model.subtractTime();
                        Thread.sleep(1000);
                    }
                    this.model.hideTime();
                    this.model.setInstruction(2);
                    this.model.activateTiles();
                    this.model.paintAllWhite();
                    this.view.update();
                    this.model.resetTime();
                    this.model.setState(3);
                }else if(this.model.getState() == 4){       // Estado en el que el usuario gano la ronda
                    this.model.deactivateTiles();
                    this.model.setInstruction(4);
                    this.model.emptyInactiveTiles();
                    this.view.update();
                    Thread.sleep(2000);
                    this.model.setState(1);
                }else if(this.model.getState() == 5){       // Estado en el que el usuario perdio
                    this.model.deactivateTiles();
                    this.model.setInstruction(5);
                    this.view.update();
                    Thread.sleep(1500);
                    String[] options = new String[2];
                    options[0] = "Si";
                    options[1] = "No";
                    int choice = JOptionPane.showOptionDialog(this.view,"Quieres volver a intentar?","Perdiste",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE, null, options, null);

                    if (choice == JOptionPane.YES_OPTION)
                    {
                        this.model.initState();
                        this.view.update();
                    }else{
                        this.parent.endGame();      // Terminar el juego
                    }
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        int position = Integer.parseInt(((JButton) e.getSource()).getName());
        if(e.getSource() instanceof JButton){
            if(this.model.getState() == 3){
                if(this.model.getSequence()[this.model.getCurrentSequenceTile()] == position){   // Checar si es el tile correspondiente en la secuencia
                    this.model.paintTile(this.model.getSequence()[this.model.getCurrentSequenceTile()], MagicTilesModel.GREEN);
                    this.model.addScore(1);
                    this.model.addInactiveTile(this.model.getSequence()[this.model.getCurrentSequenceTile()]);      // Prevenir que el usuario vuelva a dar click en ella
                    this.view.update();
                    this.model.nextSequenceTile();
                    if(this.model.getSequence().length == this.model.getCurrentSequenceTile()){
                        this.model.resetSequenceTile();
                        this.model.setState(4);     // Round Won
                    }
                }else{
                    this.model.paintTile(position, MagicTilesModel.RED);
                    this.view.update();
                    this.model.setState(5);     // Game Over
                }
            }
        }
        
    }
    
}
