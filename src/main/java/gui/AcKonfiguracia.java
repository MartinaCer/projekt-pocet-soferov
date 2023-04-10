package gui;

import dataObjekty.Data;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import konfiguracia.Konfiguracia;
import konfiguracia.Konstanty.Prestavka;

/**
 *
 * @author Martina Cernekova
 */
public class AcKonfiguracia extends AbstractAction {

    private final JFrame frame;
    private final Data data;
    private final Konfiguracia konfiguracia;

    public AcKonfiguracia(JFrame frame, Data data, Konfiguracia konfiguracia) {
        super("Zmeň konfiguráciu");
        this.frame = frame;
        this.data = data;
        this.konfiguracia = konfiguracia;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable runnable = new Runnable() {
            public void run() {
                frame.getContentPane().removeAll();
                JPanel panel = new JPanel();
                panel.setBounds(40, 80, 200, 30);
                JPanel panelPolia = new JPanel();
                panelPolia.setLayout(new BoxLayout(panelPolia, BoxLayout.PAGE_AXIS));
                JPanel panelTlacitka = new JPanel();
                panelTlacitka.setLayout(new FlowLayout());
                JLabel garL = new JLabel("depo");
                JTextField gar = new JTextField(5);
                gar.setText(konfiguracia.getGaraz().toString());
                JLabel rezL = new JLabel("rezerva [min]");
                JTextField rez = new JTextField(5);
                rez.setText(String.valueOf(konfiguracia.getRezerva() / 60));
                JLabel jazdaL = new JLabel("maximálna doba jazdy [h]");
                JTextField jazda = new JTextField(5);
                jazda.setText(String.valueOf(konfiguracia.getMaxDobaJazdy() / 3600));
                JLabel zmenaL = new JLabel("maximálna doba zmeny [h]");
                JTextField zmena = new JTextField(5);
                zmena.setBounds(50, 50, 150, 20);
                zmena.setText(String.valueOf(konfiguracia.getMaxDobaSmeny() / 3600));
                JLabel presL = new JLabel("prestávka v dobe jazdy [min]");
                JTextField pres = new JTextField(5);
                pres.setText(String.valueOf(konfiguracia.getPrestavkaVdobeJazdy() / 60));
                JLabel soferL = new JLabel("cena šoféra [€/deň]");
                JTextField sofer = new JTextField(5);
                sofer.setText(konfiguracia.getCenaSofera().toString());
                JLabel kmL = new JLabel("cena za kilometer [€]");
                JTextField km = new JTextField(5);
                km.setText(konfiguracia.getCenaKilometer().toString());

                StringBuilder pMinS = new StringBuilder();
                StringBuilder pSucetS = new StringBuilder();
                StringBuilder pCasS = new StringBuilder();
                for (Prestavka prestavka : konfiguracia.getPrestavky()) {
                    pMinS.append((prestavka.getMinPrestavka() / 60) + ";");
                    pSucetS.append((prestavka.getMinSucetPrestavok() / 60) + ";");
                    pCasS.append((prestavka.getBezPrestavky() / 60) + ";");
                }
                pMinS.deleteCharAt(pMinS.length() - 1);
                pSucetS.deleteCharAt(pSucetS.length() - 1);
                pCasS.deleteCharAt(pCasS.length() - 1);

                JLabel pMinL = new JLabel("prestávka - minimálne trvanie [min]");
                JTextField pMin = new JTextField(5);
                pMin.setText(pMinS.toString());
                JLabel pSucetL = new JLabel("prestávka - minimálny súčet [min]");
                JTextField pSucet = new JTextField(5);
                pSucet.setText(pSucetS.toString());
                JLabel pCasL = new JLabel("prestávka - časový interval [min]");
                JTextField pCas = new JTextField(5);
                pCas.setText(pCasS.toString());

                JButton demo = new JButton("demo hodnoty");
                JButton b = new JButton("ulož");
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        konfiguracia.setGaraz(Integer.valueOf(gar.getText()));
                        konfiguracia.setRezerva(Integer.valueOf(rez.getText()) * 60);
                        konfiguracia.setMaxDobaJazdy(Integer.valueOf(jazda.getText()) * 3600);
                        konfiguracia.setMaxDobaSmeny(Integer.valueOf(zmena.getText()) * 3600);
                        konfiguracia.setPrestavkaVdobeJazdy(Integer.valueOf(pres.getText()) * 60);
                        konfiguracia.setCenaSofera(Integer.valueOf(sofer.getText()));
                        konfiguracia.setCenaKilometer(Integer.valueOf(km.getText()));

                        String[] pMinP = pMin.getText().split(";");
                        String[] pSucetP = pSucet.getText().split(";");
                        String[] pCasP = pCas.getText().split(";");
                        List<Prestavka> prestavky = new ArrayList<>();
                        for (int i = 0; i < pMinP.length; i++) {
                            prestavky.add(new Prestavka(Integer.valueOf(pMinP[i]) * 60, Integer.valueOf(pSucetP[i]) * 60, Integer.valueOf(pCasP[i]) * 60));
                        }
                        konfiguracia.setPrestavky(prestavky);

                        data.zmenKonfiguraciu();
                        JOptionPane.showMessageDialog(frame, "Hotovo.", "Zmena konfigurácie", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                demo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        konfiguracia.vynuluj();
                        data.zmenKonfiguraciu();
                        JOptionPane.showMessageDialog(frame, "Hotovo.", "Zmena konfigurácie", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
                panelPolia.add(garL);
                panelPolia.add(gar);
                panelPolia.add(rezL);
                panelPolia.add(rez);
                panelPolia.add(jazdaL);
                panelPolia.add(jazda);
                panelPolia.add(zmenaL);
                panelPolia.add(zmena);
                panelPolia.add(presL);
                panelPolia.add(pres);
                panelPolia.add(soferL);
                panelPolia.add(sofer);
                panelPolia.add(kmL);
                panelPolia.add(km);
                panelPolia.add(pMinL);
                panelPolia.add(pMin);
                panelPolia.add(pSucetL);
                panelPolia.add(pSucet);
                panelPolia.add(pCasL);
                panelPolia.add(pCas);
                panelTlacitka.add(demo);
                panelTlacitka.add(b);
                panel.add(panelPolia, BorderLayout.PAGE_START);
                frame.add(panelTlacitka, BorderLayout.PAGE_END);
                frame.add(panel);
                frame.revalidate();
                frame.repaint();
            }
        };
        EventQueue.invokeLater(runnable);
    }

}
