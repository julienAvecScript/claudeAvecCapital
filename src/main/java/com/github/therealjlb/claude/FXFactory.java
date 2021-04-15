package com.github.therealjlb.claude;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class FXFactory {

    public static Pane newPane(boolean sims) {
        Pane pane = new Pane();
        pane.backgroundProperty().set(newBackground(sims));
        pane.setPrefSize(500, 800);
        Text title = new Text("CLAUDE AVEC CAPITAL");
        title.setFont(Dashboard.ARIALB_TITLE);
        title.setFill(Color.WHITE);
        title.setX(Dashboard.PREFERRED_WIDTH-(Dashboard.CELL_WIDTH*2)-title.getLayoutBounds().getWidth());
        title.setY(Dashboard.CELL_HEIGHT*3);
        pane.getChildren().add(title);
        return pane;
    }


    public static Background newBackground(boolean sims) {
        BackgroundFill fill = new BackgroundFill(newDashColor(sims), CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(fill);
        return background;
    }

    public static Text newLabel(String label, Pane pane) {
        Text text = new Text(label);
        text.setTextAlignment(TextAlignment.LEFT);
        text.setFont(Dashboard.ARIALB_LABEL);
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        return text;
    }

    public static Text newDisplay(Pane pane) {
        Text text = new Text("---");
        text.setTextAlignment(TextAlignment.LEFT);
        text.setFont(Dashboard.ARIALB_VALUE);
        text.setFill(Color.WHITE);
        pane.getChildren().add(text);
        return text;
    }

    public static TextField newTextField(String text, Pane pane) {
        TextField field = new TextField(text);
        pane.getChildren().add(field);
        return field;
    }

    public static CheckBox newCheckBox(Pane pane) {
        CheckBox box = new CheckBox();
        pane.getChildren().add(box);
        return box;
    }

    public static Paint newDashColor(boolean sims) {
        if (sims) {
            return Dashboard.FEDERAL_RED;
        } else {
            return Dashboard.PROVINCIAL_BLUE;
        }
    }
}
