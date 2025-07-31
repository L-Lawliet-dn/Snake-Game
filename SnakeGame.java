package org.example;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends Application {

    private static final int TILE_SIZE = 20;
    private static final int WIDTH = 30;
    private static final int HEIGHT = 20;

    private LinkedList<Point> snake = new LinkedList<>();
    private Direction direction = Direction.RIGHT;
    private boolean running = false;
    private Point apple;

    private Timeline timeline;
    private Random random = new Random();

    private int score = 0;
    private boolean paused = false;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP && direction != Direction.DOWN) direction = Direction.UP;
            else if (e.getCode() == KeyCode.DOWN && direction != Direction.UP) direction = Direction.DOWN;
            else if (e.getCode() == KeyCode.LEFT && direction != Direction.RIGHT) direction = Direction.LEFT;
            else if (e.getCode() == KeyCode.RIGHT && direction != Direction.LEFT) direction = Direction.RIGHT;
            else if (e.getCode() == KeyCode.SPACE) paused = !paused;
            else if (e.getCode() == KeyCode.ENTER && !running) startGame(gc);
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("🐍 Snake Game");
        primaryStage.show();

        startGame(gc);
    }

    private void startGame(GraphicsContext gc) {
        running = true;
        score = 0;
        snake.clear();
        snake.add(new Point(WIDTH / 2, HEIGHT / 2));
        spawnApple();

        timeline = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            if (!running || paused) return;

            moveSnake();
            checkCollisions();
            draw(gc);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void spawnApple() {
        do {
            apple = new Point(random.nextInt(WIDTH), random.nextInt(HEIGHT));
        } while (snake.contains(apple));
    }

    private void moveSnake() {
        Point head = snake.getFirst();
        Point newPoint = head.move(direction);

        snake.addFirst(newPoint);

        if (newPoint.equals(apple)) {
            score++;
            spawnApple();
            if (timeline.getRate() < 2.0) {
                timeline.setRate(timeline.getRate() + 0.05);
            }
        } else {
            snake.removeLast();
        }
    }

    private void checkCollisions() {
        Point head = snake.getFirst();

        // Check wall collision
        if (head.x < 0 || head.x >= WIDTH || head.y < 0 || head.y >= HEIGHT) {
            running = false;
        }

        // Check self collision
        for (int i = 1; i < snake.size(); i++) {
            if (head.equals(snake.get(i))) {
                running = false;
                break;
            }
        }
    }

    private void draw(GraphicsContext gc) {
        // Clear
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);

        // Draw apple
        gc.setFill(Color.RED);
        gc.fillOval(apple.x * TILE_SIZE, apple.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        // Draw snake
        gc.setFill(Color.LIMEGREEN);
        for (Point p : snake) {
            gc.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        gc.setFill(Color.WHITE);
        gc.fillText("Score: " + score, 10, 20);
        if (paused) gc.fillText("Paused", WIDTH * TILE_SIZE / 2 - 20, HEIGHT * TILE_SIZE / 2);

        if (!running) {
            gc.setFill(Color.WHITE);
            gc.fillText("Game Over!", WIDTH * TILE_SIZE / 2 - 30, HEIGHT * TILE_SIZE / 2);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private record Point(int x, int y) {
        Point move(Direction dir) {
            return switch (dir) {
                case UP -> new Point(x, y - 1);
                case DOWN -> new Point(x, y + 1);
                case LEFT -> new Point(x - 1, y);
                case RIGHT -> new Point(x + 1, y);
            };
        }
    }
}