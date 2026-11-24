package view.map;

import interface_adapter.map.Camera;
import interface_adapter.map.MapController;
import interface_adapter.map.MapViewModel;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * St. George Street, drawn in 2.5D pixel art. The player walks with WASD or the arrow
 * keys and presses E at a door to enter a building. The game loop runs only while this screen is shown.
 */
public class MapView extends JPanel {
    /** Screen pixels per map pixel. */
    private static final int SCALE = 4;
    /** Art pixels per map pixel in the Java-painted entrance segment. */
    private static final int SEGMENT_ART_SCALE = GroundPainter.PIXELS_PER_TILE / MapViewModel.TILE_SIZE;
    private static final int TILE = MapViewModel.TILE_SIZE * SCALE;
    /**
     * Requested frame delay. On macOS a Swing Timer fires on a ~10 ms grid, so 16 ms actually runs every
     * 20 ms (50 fps, visibly uneven on 60/120 Hz screens) while 8 ms runs every 10 ms (about 100 fps).
     * Movement uses the measured frame time, so walking speed does not depend on this value.
     */
    private static final int FRAME_MILLIS = 8;
    /** Longest frame time passed to the controller, so a stall never moves the player far. */
    private static final double MAX_FRAME_SECONDS = 0.05;

    private static final String CAMPUS_ART = "/maps/campus_art.png";
    private static final String SPRITE_PLACEMENTS = "/maps/campus_sprites.txt";
    private static final String SEGMENT_BUILDINGS = "/maps/segment_buildings.txt";
    private static final String SPRITE_FOLDER = "/maps/sprites/";
    private static final Color SHADOW = new Color(0, 0, 0, 60);
    private static final Color PROMPT_BACKGROUND = new Color(0, 0, 0, 170);

    private final MapViewModel viewModel;
    private final MapController controller;
    private final Timer timer;
    private final BufferedImage campusArt;
    private final SegmentArt segmentArt;
    private final List<SpriteLayer.Placement> sprites;
    private final List<BufferedImage> spriteImages = new ArrayList<>();
    private final CharacterSheet playerSheet = CharacterSheet.of(CharacterLook.player());
    private final WalkAnimation walk = new WalkAnimation();
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private long lastFrameNanos;

    /** Painted once per run: the map file never changes, so every new game reuses the same art. */
    private static BufferedImage cachedBackdrop;
    private static SegmentArt cachedArt;

    /**
     * The campus map screen, used as the game's "street" page.
     * @param toTitle what the "Title Screen" button does
     */
    public MapView(MapViewModel viewModel, MapController controller, Runnable toTitle) {
        this.viewModel = viewModel;
        this.controller = controller;
        paintArtOnce(viewModel);
        campusArt = cachedBackdrop;
        segmentArt = cachedArt;
        sprites = loadSprites();
        for (SpriteLayer.Placement sprite : sprites) {
            spriteImages.add(loadSpriteImage(sprite.getFile()));
        }
        setPreferredSize(new Dimension(960, 640));
        setBackground(Color.BLACK);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton titleButton = new JButton("Title Screen");
        titleButton.setFocusable(false);
        titleButton.addActionListener(e -> toTitle.run());
        add(titleButton);

        timer = new Timer(FRAME_MILLIS, e -> step());
        bindKeys();
        viewModel.addPropertyChangeListener(evt -> repaint());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                lastFrameNanos = System.nanoTime();
                timer.start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                timer.stop();
                up = down = left = right = false;
            }
        });
    }

    private static synchronized void paintArtOnce(MapViewModel viewModel) {
        if (cachedArt != null) {
            return;
        }
        cachedBackdrop = WarmPalette.apply(loadCampusArt(viewModel.getColumns(), viewModel.getRows()));
        TileGrid grid = tileGridOf(viewModel);
        // The whole map is painted in Java; the old backdrop only remains as a fallback under it.
        int rows = viewModel.getRows();
        cachedArt = SegmentArt.paint(grid, rows, cachedBackdrop,
                BuildingPlan.parse(readLines(SEGMENT_BUILDINGS), grid, rows));
    }

    /** Enters the building whose door the player stands at, as the E key does. */
    public void interact() {
        controller.interact();
    }

    /** Walks the player as the movement keys do; directions are -1, 0 or 1. */
    public void walk(int dirX, int dirY, double seconds) {
        walk.update(dirX, dirY, seconds);
        controller.tick(dirX, dirY, seconds);
    }

    private void step() {
        long now = System.nanoTime();
        double seconds = Math.min((now - lastFrameNanos) / 1e9, MAX_FRAME_SECONDS);
        lastFrameNanos = now;
        int dirX = (right ? 1 : 0) - (left ? 1 : 0);
        int dirY = (down ? 1 : 0) - (up ? 1 : 0);
        walk.update(dirX, dirY, seconds);
        controller.tick(dirX, dirY, seconds);
    }

    private void bindKeys() {
        // Window-wide bindings, so the keys work without clicking the map first.
        InputMap input = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actions = getActionMap();
        bindDirection(input, actions, "up", KeyEvent.VK_W, KeyEvent.VK_UP);
        bindDirection(input, actions, "down", KeyEvent.VK_S, KeyEvent.VK_DOWN);
        bindDirection(input, actions, "left", KeyEvent.VK_A, KeyEvent.VK_LEFT);
        bindDirection(input, actions, "right", KeyEvent.VK_D, KeyEvent.VK_RIGHT);
        input.put(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0, false), "interact");
        actions.put("interact", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isRunning()) {
                    controller.interact();
                }
            }
        });
    }

    private void bindDirection(InputMap input, ActionMap actions, String name, int... keys) {
        for (int key : keys) {
            input.put(KeyStroke.getKeyStroke(key, 0, false), name + " pressed");
            input.put(KeyStroke.getKeyStroke(key, 0, true), name + " released");
        }
        actions.put(name + " pressed", new DirectionAction(name, true));
        actions.put(name + " released", new DirectionAction(name, false));
    }

    private final class DirectionAction extends AbstractAction {
        private final String direction;
        private final boolean pressed;

        DirectionAction(String direction, boolean pressed) {
            this.direction = direction;
            this.pressed = pressed;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!timer.isRunning()) {
                return;
            }
            switch (direction) {
                case "up":
                    up = pressed;
                    break;
                case "down":
                    down = pressed;
                    break;
                case "left":
                    left = pressed;
                    break;
                default:
                    right = pressed;
                    break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int cols = viewModel.getColumns();
        int rows = viewModel.getRows();
        int camX = (int) Math.round(Camera.offset(viewModel.getWalkerX() * SCALE, getWidth(), cols * TILE));
        int camY = (int) Math.round(Camera.offset(viewModel.getWalkerY() * SCALE, getHeight(), rows * TILE));

        g.translate(-camX, -camY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        drawVisible(g, campusArt, 1, camX, camY);
        drawVisible(g, segmentArt.getGround(), SEGMENT_ART_SCALE, camX, camY);
        double walkerY = viewModel.getWalkerY();
        drawSprites(g, walkerY, false);
        drawPlayer(g);
        drawSprites(g, walkerY, true);
        drawVisible(g, segmentArt.getCanopy(), SEGMENT_ART_SCALE, camX, camY);
        drawLabels(g);
        g.translate(camX, camY);
        drawPrompt(g);
        g.dispose();
    }

    /**
     * Draws only the part of a map-aligned image that is on screen. The image starts at the map's
     * top-left corner and has {@code artScale} art pixels per map pixel.
     */
    private void drawVisible(Graphics2D g, BufferedImage image, int artScale, int camX, int camY) {
        int sx0 = Math.max(0, Math.floorDiv(camX * artScale, SCALE));
        int sy0 = Math.max(0, Math.floorDiv(camY * artScale, SCALE));
        int sx1 = Math.min(image.getWidth(), Math.floorDiv((camX + getWidth()) * artScale, SCALE) + 1);
        int sy1 = Math.min(image.getHeight(), Math.floorDiv((camY + getHeight()) * artScale, SCALE) + 1);
        if (sx0 >= sx1 || sy0 >= sy1) {
            return;
        }
        int pixel = SCALE / artScale;
        g.drawImage(image, sx0 * pixel, sy0 * pixel, sx1 * pixel, sy1 * pixel, sx0, sy0, sx1, sy1, null);
    }

    /** Lets the map painters read tiles from the view model without knowing about it. */
    private static TileGrid tileGridOf(MapViewModel viewModel) {
        return new TileGrid() {
            @Override
            public int getColumns() {
                return viewModel.getColumns();
            }

            @Override
            public int getRows() {
                return viewModel.getRows();
            }

            @Override
            public char getTile(int col, int row) {
                return viewModel.getTile(col, row);
            }
        };
    }

    private static BufferedImage loadCampusArt(int columns, int rows) {
        try (InputStream stream = MapView.class.getResourceAsStream(CAMPUS_ART)) {
            if (stream == null) {
                throw new IllegalStateException("Missing campus artwork: " + CAMPUS_ART);
            }
            BufferedImage art = ImageIO.read(stream);
            int tileSize = MapViewModel.TILE_SIZE;
            if (art == null || art.getWidth() != columns * tileSize || art.getHeight() != rows * tileSize) {
                throw new IllegalStateException("Campus artwork must match the collision grid: " + CAMPUS_ART);
            }
            return art;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read campus artwork: " + CAMPUS_ART, e);
        }
    }

    /** Draws the building sprites that are in front of (or behind) a player standing at {@code walkerY}. */
    private void drawSprites(Graphics2D g, double walkerY, boolean inFront) {
        // Sprites are saved at 2 pixels per map pixel; at SCALE 4 each becomes a crisp 2x2 block.
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        for (int i = 0; i < sprites.size(); i++) {
            SpriteLayer.Placement sprite = sprites.get(i);
            if (sprite.isInFrontOf(walkerY) == inFront) {
                g.drawImage(spriteImages.get(i), sprite.getX() * SCALE, sprite.getY() * SCALE,
                        sprite.getWidth() * SCALE, sprite.getHeight() * SCALE, null);
            }
        }
    }

    private static List<SpriteLayer.Placement> loadSprites() {
        return SpriteLayer.parse(readLines(SPRITE_PLACEMENTS));
    }

    /** Reads a bundled text file as lines; fails with the resource's name if it is missing. */
    private static List<String> readLines(String path) {
        try (InputStream stream = MapView.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing map resource: " + path);
            }
            String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            return Arrays.asList(text.split("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read map resource: " + path, e);
        }
    }

    private static BufferedImage loadSpriteImage(String file) {
        String path = SPRITE_FOLDER + file;
        try (InputStream stream = MapView.class.getResourceAsStream(path)) {
            BufferedImage image = stream == null ? null : ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Missing building sprite: " + path);
            }
            return image;
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read building sprite: " + path, e);
        }
    }

    private void drawLabels(Graphics2D g) {
        g.setFont(getFont().deriveFont(Font.BOLD, 16f));
        FontMetrics metrics = g.getFontMetrics();
        for (MapViewModel.Label label : viewModel.getLabels()) {
            int centerX = label.getCol() * TILE + TILE / 2;
            int baseline = label.getRow() * TILE + TILE / 2 + metrics.getAscent() / 2;
            int x = centerX - metrics.stringWidth(label.getText()) / 2;
            g.setColor(PROMPT_BACKGROUND);
            g.fillRoundRect(x - 8, baseline - metrics.getAscent() - 2,
                    metrics.stringWidth(label.getText()) + 16, metrics.getHeight() + 4, 6, 6);
            g.setColor(Color.WHITE);
            g.drawString(label.getText(), x, baseline);
        }
    }

    private void drawPlayer(Graphics2D g) {
        int footX = (int) Math.round(viewModel.getWalkerX() * SCALE);
        int footY = (int) Math.round(viewModel.getWalkerY() * SCALE);
        // One sprite pixel is one art pixel of the painted map (half a map pixel), so the player has
        // the same pixel size as the ground, trees and props. The frame's bottom row is where the feet stand.
        int pixel = SCALE / SEGMENT_ART_SCALE;
        int width = 9 * SCALE;
        g.setColor(SHADOW);
        g.fillOval(footX - width / 2, footY - SCALE * 2, width, SCALE * 3);
        BufferedImage frame = walk.isMoving() ? playerSheet.frame(walk.getFacing(), walk.getFrame())
                : playerSheet.standing(walk.getFacing());
        int left = footX - CharacterSheet.WIDTH * pixel / 2;
        int top = footY - (CharacterSheet.HEIGHT - 1) * pixel;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(frame, left, top, CharacterSheet.WIDTH * pixel, CharacterSheet.HEIGHT * pixel, null);
    }

    private void drawPrompt(Graphics2D g) {
        String prompt = viewModel.getPromptText();
        if (prompt.isEmpty()) {
            return;
        }
        g.setFont(getFont().deriveFont(Font.BOLD, 18f));
        FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(prompt) + 32;
        int height = metrics.getHeight() + 16;
        int x = (getWidth() - width) / 2;
        int y = getHeight() - height - 24;
        g.setColor(PROMPT_BACKGROUND);
        g.fillRoundRect(x, y, width, height, 12, 12);
        g.setColor(Color.WHITE);
        g.drawString(prompt, x + 16, y + 8 + metrics.getAscent());
    }
}
