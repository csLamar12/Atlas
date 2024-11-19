package org.example.Controller;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VideoBasedPreview extends JPanel {

    private JFXPanel jfxPanel;
    private MediaPlayer mediaPlayer;

    public VideoBasedPreview(String videoPath) {
        setLayout(new BorderLayout());

        // Initialize the JFXPanel
        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        // Run JavaFX initialization on the JavaFX Application Thread
        Platform.runLater(() -> initFX(videoPath));
    }

    private void initFX(String videoPath) {
        Platform.runLater(() -> {
            File videoFile = new File(videoPath);
            Media media = new Media(videoFile.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setOnError(() -> System.err.println("Error: " + mediaPlayer.getError()));

            MediaView mediaView = new MediaView(mediaPlayer);

            mediaView.setFitWidth(320);
//            mediaView.setFitHeight(300);
            mediaView.setPreserveRatio(true);
            mediaView.setOnMouseClicked(event -> toggleVideoPlayback());

            Scene scene = new Scene(new javafx.scene.Group(mediaView));
            jfxPanel.setScene(scene);
            mediaPlayer.play();
        });
    }

    private void toggleVideoPlayback() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
            } else if (mediaPlayer.getStatus() == MediaPlayer.Status.PAUSED || mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED) {
                mediaPlayer.play();
            }
        }
    }

    public boolean isVideoPlaying() {
        if (mediaPlayer != null) {
            System.out.println("Is Playing");
            return mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    public void stopVideo() {

        Platform.runLater(() -> {
            if (this.mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;  // Clear the mediaPlayer reference
            }
        });
    }

}
