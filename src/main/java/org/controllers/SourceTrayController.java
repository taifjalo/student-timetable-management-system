package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.dao.GroupDao;
import org.entities.StudentGroup;
import org.service.GroupService;
import org.service.LocalizationService;
import org.service.SessionManager;

public class SourceTrayController {

    private CalendarSource calendarSource;
    private ObservableList<StudentGroup> groupsList;

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();


    public void addSourceSectionsToSourceTray(CalendarView calendarView, CalendarSource courseSource) {
        this.calendarSource = courseSource;

        // Fetch groups
        Long userId = SessionManager.getInstance().isTeacher() ? null
                : (SessionManager.getInstance().getCurrentUser() != null
                        ? SessionManager.getInstance().getCurrentUser().getId() : null);
        List<StudentGroup> groups;
        try {
            groups = new GroupService(new GroupDao()).getGroupsForUser(userId);
        } catch (Exception e) {
            System.err.println("[SourceTrayController] Failed to load groups: " + e.getMessage());
            groups = java.util.Collections.emptyList();
        }

        this.groupsList = FXCollections.observableArrayList(groups);
        ScrollPane sourceScrollPane = calendarView.lookupAll(".source-view-scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .findFirst()
                .orElse(null);

        if (sourceScrollPane == null || sourceScrollPane.getContent() == null) return;

        Node coursesSection = createSourceSectionNode(courseSource);
        Node groupsSection = createGroupsSectionNode("Groups", groupsList);
        if (coursesSection == null || groupsSection == null) return;

        Separator sectionDivider = new Separator();
        VBox.setMargin(sectionDivider, new javafx.geometry.Insets(20, 0, 0, 0));
        VBox wrapper = new VBox(12, coursesSection, sectionDivider, groupsSection);
        wrapper.getStyleClass().add("source-tray-content");
        sourceScrollPane.setFitToWidth(true);
        sourceScrollPane.setContent(wrapper);
    }

    private Node createSourceSectionNode(CalendarSource source) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/sourcetray-groups.fxml"));
        groupsLoader.setController(this);
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Label sectionTitleText = (Label) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");
        Button addButton = (Button) groupsLoader.getNamespace().get("addButton");

        if (sectionTitleText == null || groupsListContainer == null) return null;

        if (addButton != null) {
            addButton.setUserData(source.getName());
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

        Runnable refreshRows = () -> {
            groupsListContainer.getChildren().clear();
            for (Calendar calendar : source.getCalendars()) {
                groupsListContainer.getChildren().add(createCalendarRow(calendar, source.getName()));
            }
        };

        sectionTitleText.setText(source.getName());

        refreshRows.run();

        source.nameProperty().addListener((obs, oldName, newName) -> sectionTitleText.setText(newName));
        source.getCalendars().addListener((ListChangeListener<Calendar>) change -> refreshRows.run());

        return sourceSection;
    }

    private Node createGroupsSectionNode(String sectionTitle, ObservableList<StudentGroup> items) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/sourcetray-groups.fxml"));
        groupsLoader.setController(this);
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Label sectionTitleText = (Label) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");
        Button addButton = (Button) groupsLoader.getNamespace().get("addButton");

        if (sectionTitleText == null || groupsListContainer == null) return null;

        if (addButton != null) {
            addButton.setUserData(sectionTitle);
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

//        sectionTitleText.setText(sectionTitle);
        sectionTitleText.setText(selectedBundle.getString("sourcetray.groups.section.title"));


        // Render initial rows
        groupsListContainer.getChildren().clear();
        for (StudentGroup group : items) {
            groupsListContainer.getChildren().add(createGroupRow(group, sectionTitle));
        }

        // Listen for new groups added/removed so the tray updates live
        items.addListener((javafx.collections.ListChangeListener<StudentGroup>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (StudentGroup added : change.getAddedSubList()) {
                        groupsListContainer.getChildren().add(createGroupRow(added, sectionTitle));
                    }
                }
                if (change.wasRemoved()) {
                    // Rebuild on removal — simplest safe approach
                    groupsListContainer.getChildren().clear();
                    for (StudentGroup group : items) {
                        groupsListContainer.getChildren().add(createGroupRow(group, sectionTitle));
                    }
                }
            }
        });

        return sourceSection;
    }

    public void handleOpenCreateGroupModal(ActionEvent event) {
        Button source = (Button) event.getSource();
        String sectionName = source.getUserData() != null ? source.getUserData().toString() : "Group";
        String singular = toSingular(sectionName);
        if ("Course".equalsIgnoreCase(singular)) {
            openGroupModal((Calendar) null, singular, event);
        } else {
            openGroupModal((StudentGroup) null, singular, event);
        }
    }

    private void openGroupModal(Calendar calendar, String sectionName, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/source-tray-course-modal/course-modal.fxml"), localizationService.getBundle());
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/source-tray-course-modal/course-modal.fxml"));
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateCourseModalController modalController = loader.getController();
            modalController.setCalendar(calendar);
            modalController.setCalendarSource(calendarSource);
            modalController.applyProps();
            showModal(root, calendar != null ? calendar.getName() : null, sectionName, event);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to open course modal", e);
        }
    }

    private void openGroupModal(StudentGroup group, String sectionName, ActionEvent event) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/source-tray-create-modal/create-modal.fxml");
            if (fxmlUrl == null) {
                System.err.println("[SourceTrayController] create-modal.fxml not found on classpath");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            CreateGroupModalController modalController = loader.getController();
            if (modalController == null) {
                System.err.println("[SourceTrayController] CreateGroupModalController is null after load");
                return;
            }
            if (group != null) {
                modalController.setGroup(group);
            }
            modalController.setSectionName(sectionName);
            modalController.setGroupsList(groupsList);
            modalController.applyProps();
            showModal(root, group != null ? group.getFieldOfStudies() : null, sectionName, event);
        } catch (Exception e) {
            System.err.println("[SourceTrayController] Failed to open group modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showModal(Parent root, String groupName, String sectionName, ActionEvent event) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(groupName != null ? "Edit " + sectionName : "Create " + sectionName);
        dialogStage.setScene(new Scene(root));
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private HBox createCalendarRow(Calendar calendar, String sectionName) {
        Region colorDot = new Region();
        colorDot.setMinSize(12, 12);
        colorDot.setMaxSize(12, 12);
        colorDot.setStyle("-fx-background-color: " + styleToHex(calendar.getStyle()) + ";");
        HBox.setMargin(colorDot, new javafx.geometry.Insets(0, 6, 0, 0));

        Text nameText = new Text(calendar.getName());
        nameText.setStrokeWidth(0.0);
        calendar.nameProperty().addListener((obs, oldName, newName) -> nameText.setText(newName));

        calendar.styleProperty().addListener((obs, oldStyle, newStyle) ->
                colorDot.setStyle("-fx-background-color: " + styleToHex(newStyle) + ";"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        ImageView editIcon = new ImageView(new Image(getClass().getResource("/images/edit_dots_icon.png").toExternalForm()));
        editIcon.setFitWidth(15.0);
        editIcon.setFitHeight(15.0);
        editIcon.setPickOnBounds(true);
        editIcon.setPreserveRatio(true);

        Button actionButton = new Button();
        actionButton.setMnemonicParsing(false);
        actionButton.setGraphic(editIcon);
        actionButton.setCursor(Cursor.HAND);
        actionButton.setStyle("-fx-background-color: transparent;");
        actionButton.setOnAction(e -> openGroupModal(calendar, toSingular(sectionName), e));
        actionButton.setVisible(SessionManager.getInstance().isTeacher());
        actionButton.setManaged(SessionManager.getInstance().isTeacher());

        HBox row = new HBox(colorDot, nameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private HBox createGroupRow(StudentGroup group, String sectionName) {

        Text groupNameText = new Text(group.getFieldOfStudies());
        groupNameText.setStrokeWidth(0.0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        ImageView editIcon = new ImageView(new Image(getClass().getResource("/images/edit_dots_icon.png").toExternalForm()));
        editIcon.setFitWidth(15.0);
        editIcon.setFitHeight(15.0);
        editIcon.setPickOnBounds(true);
        editIcon.setPreserveRatio(true);

        Button actionButton = new Button();
        actionButton.setMnemonicParsing(false);
        actionButton.setGraphic(editIcon);
        actionButton.setCursor(Cursor.HAND);
        actionButton.setStyle("-fx-background-color: transparent;");
        actionButton.setOnAction(e -> openGroupModal(group, toSingular(sectionName), e));
        actionButton.setVisible(SessionManager.getInstance().isTeacher());
        actionButton.setManaged(SessionManager.getInstance().isTeacher());

        HBox row = new HBox(groupNameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private String toSingular(String word) {
        if (word == null || word.isEmpty()) return word;
        if (word.endsWith("s") || word.endsWith("S")) return word.substring(0, word.length() - 1);
        return word;
    }

    private static String styleToHex(String style) {
        if (style == null) return "#888888";
        switch (style.toLowerCase()) {
            case "style1": return "#77C04B";
            case "style2": return "#418FCB";
            case "style3": return "#F7D15B";
            case "style4": return "#9D5B9F";
            case "style5": return "#D0525F";
            case "style6": return "#F9844B";
            case "style7": return "#AE663E";
            default:       return "#888888";
        }
    }
}

