package view;
import model.Photo;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainFrame extends JFrame {

    private JTable table;
    private JLabel lblImage;
    private JComboBox<String> cbCameraName;
    private JLabel lblSequentialTime; // Etiqueta para mostrar tiempo secuencial
    private JLabel lblParallelTime;   // Etiqueta para mostrar tiempo paralelo
    private JButton btnSequential;
    private JButton btnParallel;
    private JButton btnShowImage;
    private JButton btnClearSelection;
    private boolean isParallelProcessing = false;

    public MainFrame(List<Photo> photos) {
        super("NASA Mars Rover Photos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        initUI(photos);
        setVisible(true);
    }

    private void initUI(List<Photo> photos) {
        setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Sol", "Earth Date", "Camera Name", "Image URL", "Rover"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        for (Photo photo : photos) {
            model.addRow(new Object[]{
                    photo.getId(),
                    photo.getSol(),
                    photo.getEarth_date(),
                    photo.getCamera().getName(),
                    photo.getImg_src(),
                    photo.getRover().getName()
            });
        }

        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        lblImage = new JLabel();
        add(lblImage, BorderLayout.EAST);

        // Panel superior para filtros (cámara) y botones de imagen y limpieza
        JPanel topPanel = new JPanel(new BorderLayout());

        // Panel para filtro de cámara
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        Set<String> cameraNames = photos.stream().map(photo -> photo.getCamera().getName()).collect(Collectors.toSet());
        cbCameraName = new JComboBox<>(cameraNames.toArray(new String[0]));
        filterPanel.add(new JLabel("Camera Name:"));
        filterPanel.add(cbCameraName);
        topPanel.add(filterPanel, BorderLayout.WEST);

        // Panel para mostrar imagen y limpiar selección
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnShowImage = new JButton("Mostrar Imagen");
        btnShowImage.addActionListener(this::showImage);
        imagePanel.add(btnShowImage);

        btnClearSelection = new JButton("Limpiar Selección");
        btnClearSelection.addActionListener(this::clearSelection);
        imagePanel.add(btnClearSelection);

        topPanel.add(imagePanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Panel para botones de procesamiento secuencial y paralelo
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnSequential = new JButton("Procesamiento Secuencial");
        btnSequential.addActionListener(e -> {
            isParallelProcessing = false;
            long startTime = System.nanoTime();
            filterPhotos(photos);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Convertir a milisegundos
            lblSequentialTime.setText("Tiempo secuencial: " + duration + " ms");
        });
        buttonPanel.add(btnSequential);

        lblSequentialTime = new JLabel("Tiempo secuencial: ");
        buttonPanel.add(lblSequentialTime);

        btnParallel = new JButton("Procesamiento Paralelo");
        btnParallel.addActionListener(e -> {
            isParallelProcessing = true;
            long startTime = System.nanoTime();
            filterPhotos(photos);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // Convertir a milisegundos
            lblParallelTime.setText("Tiempo paralelo: " + duration + " ms");
        });
        buttonPanel.add(btnParallel);

        lblParallelTime = new JLabel("Tiempo paralelo: ");
        buttonPanel.add(lblParallelTime);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void filterPhotos(List<Photo> photos) {
        String cameraName = (String) cbCameraName.getSelectedItem();

        List<Photo> filteredPhotos = photos.stream()
                .filter(photo -> cameraName == null || photo.getCamera().getName().equals(cameraName))
                .collect(Collectors.toList());

        if (isParallelProcessing) {
            filteredPhotos = filteredPhotos.parallelStream().collect(Collectors.toList());
        }

        updateTable(filteredPhotos);
    }

    private void updateTable(List<Photo> photos) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        for (Photo photo : photos) {
            model.addRow(new Object[]{
                    photo.getId(),
                    photo.getSol(),
                    photo.getEarth_date(),
                    photo.getCamera().getName(),
                    photo.getImg_src(),
                    photo.getRover().getName()
            });
        }
    }

    private void showImage(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String imageUrl = (String) table.getValueAt(selectedRow, 4); // Obtener la URL de la imagen desde la tabla
            if (imageUrl != null && !imageUrl.isEmpty()) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        URL url = new URL(imageUrl);

                        // Cargar la imagen usando ImageIO para manejo robusto
                        BufferedImage image = ImageIO.read(url);

                        // Verificar que la imagen se haya cargado correctamente
                        if (image != null) {
                            ImageIcon imageIcon = new ImageIcon(image);
                            lblImage.setIcon(imageIcon);
                            lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                        } else {
                            JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error al cargar la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        }
    }

    private void clearSelection(ActionEvent e) {
        table.clearSelection(); // Limpiar selección en la tabla
        lblImage.setIcon(null); // Limpiar imagen en el JLabel
    }
}