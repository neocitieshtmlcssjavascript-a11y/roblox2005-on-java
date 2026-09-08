import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class java extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    // --- НАСТРОЙКИ МИРА ---
    private int blockWidth = 60;
    private int blockHeight = 30; // Высота для изометрии (псевдо-3D)
    private int blockDepth = 20;  // Высота подъема блока вверх
    
    // Смещение камеры (для перетаскивания)
    private int camX = 400;
    private int camY = 200;
    private int angle = 0; // Вращение камеры (0, 90, 180, 270)

    // --- СУЩНОСТИ ---
    private int playerX = 2, playerY = 2; // Координаты плоского Стива
    private int zombieX = 5, zombieY = 4; // Координаты плоского Зомби
    private boolean isMoving = false;    // Состояние кнопки "Go"

    // --- ИНТЕРФЕЙС И EXPLORER ---
    private String selectedObject = "World";
    private Point mouseClickPos = null;

    public java() {
        JFrame frame = new JFrame("Roblox 2005 2.5D Engine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        
        // Панель управления (Кнопки + Explorer)
        JPanel uiPanel = new JPanel();
        uiPanel.setLayout(new BoxLayout(uiPanel, BoxLayout.Y_AXIS));
        uiPanel.setBackground(new Color(50, 50, 50));
        uiPanel.setPreferredSize(new Dimension(250, 700));

        // Кнопка GO
        JButton goBtn = new JButton("▶ GO / MOVE");
        goBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        goBtn.addActionListener(e -> {
            isMoving = !isMoving;
            selectedObject = "Player (Moving)";
        });

        // Кнопка Поворота Камеры
        JButton rotateBtn = new JButton("🔄 Rotate Camera");
        rotateBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        rotateBtn.addActionListener(e -> {
            angle = (angle + 90) % 360;
            repaint();
        });

        // Кастомный Explorer (Проводник объектов)
        JLabel expLabel = new JLabel("--- EXPLORER ---");
        expLabel.setForeground(Color.WHITE);
        expLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement(" Workspace");
        listModel.addElement("  ├── Camera");
        listModel.addElement("  ├── BasePlate (3D Blocks)");
        listModel.addElement("  ├── Player (Flat Steve)");
        listModel.addElement("  └── Zombie (Flat Enemy)");
        listModel.addElement(" StarterGui");
        listModel.addElement("  └── GoButton");

        JList<String> explorerList = new JList<>(listModel);
        explorerList.setBackground(new Color(40, 40, 40));
        explorerList.setForeground(Color.GREEN);
        explorerList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selectedObject = explorerList.getSelectedValue().trim();
        });

        uiPanel.add(Box.createVerticalStrut(10));
        uiPanel.add(goBtn);
        uiPanel.add(Box.createVerticalStrut(10));
        uiPanel.add(rotateBtn);
        uiPanel.add(Box.createVerticalStrut(20));
        uiPanel.add(expLabel);
        uiPanel.add(Box.createVerticalStrut(10));
        uiPanel.add(new JScrollPane(explorerList));

        // Сборка окна
        frame.setLayout(new BorderLayout());
        frame.add(this, BorderLayout.CENTER);
        frame.add(uiPanel, BorderLayout.EAST);

        // Слушатели мыши для перетаскивания мира
        addMouseListener(this);
        addMouseMotionListener(this);

        frame.setVisible(true);

        // Игровой таймер (апдейты)
        Timer timer = new Timer(100, this);
        timer.start();
    }

    // Перевод сетки в псевдо-3D (Изометрию)
    private Point toIso(int x, int y, int z) {
        int isoX = camX + (x - y) * (blockWidth / 2);
        int isoY = camY + (x + y) * (blockHeight / 2) - (z * blockDepth);
        return new Point(isoX, isoY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Задний фон (Небо старого Роблокса)
        g2.setColor(new Color(135, 206, 235));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 1. РИСУЕМ 3D ПЛАТФОРМУ (Сетка 8х8 блоков)
        g2.setColor(new Color(34, 139, 34)); // Зеленая трава
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                draw3DBlock(g2, x, y, 0);
            }
        }

        // 2. РИСУЕМ ПЛОСКОГО ЗОМБИ (Спрайт)
        Point zombiePos = toIso(zombieX, zombieY, 1);
        drawFlatCharacter(g2, zombiePos.x, zombiePos.y, Color.GREEN, "ZOMBIE");

        // 3. РИСУЕМ ПЛОСКОГО ИГРОКА (Стив из Майнкрафт)
        Point playerPos = toIso(playerX, playerY, 1);
        drawFlatCharacter(g2, playerPos.x, playerPos.y, new Color(0, 150, 255), "STEVE");

        // Оверлей с инфой
        g2.setColor(Color.BLACK);
        g2.drawString("Selected in Explorer: " + selectedObject, 10, 20);
        g2.drawString("Зажмите ЛКМ для перетаскивания камеры", 10, 40);
        g2.drawString("Угол камеры: " + angle + "°", 10, 60);
    }

    // Метод отрисовки псевдо-3D куба
    private void draw3DBlock(Graphics2D g, int x, int y, int z) {
        // В зависимости от угла меняем проекцию отрисовки
        int rx = x, ry = y;
        if (angle == 90) { rx = y; ry = 7 - x; }
        else if (angle == 180) { rx = 7 - x; ry = 7 - y; }
        else if (angle == 270) { rx = 7 - y; ry = x; }

        Point p = toIso(rx, ry, z);

        // Верхняя грань (Ромб)
        Polygon top = new Polygon();
        top.addPoint(p.x, p.y);
        top.addPoint(p.x + blockWidth / 2, p.y + blockHeight / 2);
        top.addPoint(p.x, p.y + blockHeight);
        top.addPoint(p.x - blockWidth / 2, p.y + blockHeight / 2);
        g.setColor(new Color(100, 200, 100)); // Светло-зеленый
        g.fillPolygon(top);
        g.setColor(new Color(80, 160, 80));
        g.drawPolygon(top);

        // Левая грань
        Polygon left = new Polygon();
        left.addPoint(p.x - blockWidth / 2, p.y + blockHeight / 2);
        left.addPoint(p.x, p.y + blockHeight);
        left.addPoint(p.x, p.y + blockHeight + blockDepth);
        left.addPoint(p.x - blockWidth / 2, p.y + blockHeight / 2 + blockDepth);
        g.setColor(new Color(139, 69, 19)); // Коричневая земля
        g.fillPolygon(left);
        g.drawPolygon(left);

        // Правая грань
        Polygon right = new Polygon();
        right.addPoint(p.x, p.y + blockHeight);
        right.addPoint(p.x + blockWidth / 2, p.y + blockHeight / 2);
        right.addPoint(p.x + blockWidth / 2, p.y + blockHeight / 2 + blockDepth);
        right.addPoint(p.x, p.y + blockHeight + blockDepth);
        g.setColor(new Color(110, 50, 10)); // Темно-коричневая
        g.fillPolygon(right);
        g.drawPolygon(right);
    }

    // Отрисовка плоского человечка, который выглядит как "картонка" в 3D мире
    private void drawFlatCharacter(Graphics2D g, int cx, int cy, Color shirtColor, String name) {
        int h = 50; // Высота
        int w = 20; // Ширина

        // Корпус/Рубашка (Плоский прямоугольник)
        g.setColor(shirtColor);
        g.fillRect(cx - w/2, cy - h, w, h - 15);
        
        // Штаны
        g.setColor(Color.BLUE);
        g.fillRect(cx - w/2, cy - 15, w, 15);

        // Квадратная голова в стиле Майнкрафт
        g.setColor(new Color(255, 218, 185)); // Телесный
        g.fillRect(cx - 10, cy - h - 15, 20, 15);

        // Имя над головой
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(name, cx - 15, cy - h - 20);
    }

    // Логика движения по кнопке GO
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMoving) {
            // Игрок идет к зомби
            if (playerX < zombieX) playerX++;
            else if (playerX > zombieX) playerX--;
            
            if (playerY < zombieY) playerY++;
            else if (playerY > zombieY) playerY--;

            // Если подошел вплотную — стоп
            if (playerX == zombieX && playerY == zombieY) {
                isMoving = false;
                selectedObject = "Collision! (Steve met Zombie)";
            }
        }
        repaint();
    }

    // --- УПРАВЛЕНИЕ КАМЕРОЙ (ПЕРЕТАСКИВАНИЕ МЫШЬЮ) ---
    @Override
    public void mousePressed(MouseEvent e) { mouseClickPos = e.getPoint(); }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseClickPos != null) {
            int dx = e.getX() - mouseClickPos.x;
            int dy = e.getY() - mouseClickPos.y;
            camX += dx;
            camY += dy;
            mouseClickPos = e.getPoint();
            repaint();
        }
    }

    @Override public void mouseReleased(MouseEvent e) { mouseClickPos = null; }
    @Override public void mouseMoved(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}

    // Точка входа
    public static void main(String[] args) {
        SwingUtilities.invokeLater(java::new);
    }
}
