package org.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.view.CalendarView;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.application.SourceTrayRowFactory;
import org.dao.GroupDao;
import org.entities.StudentGroup;
import org.service.GroupService;
import org.service.LocalizationService;
import org.service.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the CalendarFX source-tray sidebar.
 */
public class SourceTrayController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceTrayController.class);
    private static final String UNEXPECTED_ERROR = "Unexpected error";
    private static final String SECTION_TYPE_COURSE = "COURSE";
    private static final String SECTION_TYPE_GROUP = "GROUP";

    private CalendarSource calendarSource;
    private ObservableList<StudentGroup> groupsList;
    private Timeline skeletonPulse;

    private final LocalizationService localizationService = new LocalizationService();
    private final SourceTrayRowFactory sourceTrayRowFactory = new SourceTrayRowFactory();
    ResourceBundle selectedBundle = localizationService.getBundle();

    /**
     * Injects shimmer skeleton placeholders into the source-tray scroll pane.
     */
    public void showSkeleton(CalendarView calendarView) {
        ScrollPane sourceScrollPane = calendarView.lookupAll(".source-view-scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .findFirst().orElse(null);
        if (sourceScrollPane == null) {
            return;
        }

        VBox skeleton = new VBox(8);
        skeleton.setPadding(new Insets(12, 16, 12, 16));

        skeleton.getChildren().add(bone(110, 13));
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30));
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30));
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30));
        skeleton.getChildren().add(bone(60, 24));

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(10, 0, 10, 0));
        skeleton.getChildren().add(sep);

        skeleton.getChildren().add(bone(90, 13));
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30));
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30));

        sourceScrollPane.setFitToWidth(true);
        sourceScrollPane.setContent(skeleton);

        skeletonPulse = new Timeline(
                new KeyFrame(Duration.ZERO,          new KeyValue(skeleton.opacityProperty(), 0.45)),
                new KeyFrame(Duration.millis(700),   new KeyValue(skeleton.opacityProperty(), 0.95)),
                new KeyFrame(Duration.millis(1400),  new KeyValue(skeleton.opacityProperty(), 0.45))
        );
        skeletonPulse.setCycleCount(Animation.INDEFINITE);
        skeletonPulse.play();
    }

    private Region bone(double maxWidth, double height) {
        Region r = new Region();
        r.setMaxWidth(maxWidth);
        r.setPrefHeight(height);
        r.setStyle("-fx-background-color: #DEDEDE; -fx-background-radius: 6;");
        return r;
    }

    /**
     * Replaces the CalendarFX source-tray scroll-pane content with the two custom sections.
     */
    public void addSourceSectionsToSourceTray(CalendarView calendarView, CalendarSource courseSource) {
        if (skeletonPulse != null) {
            skeletonPulse.stop();
            skeletonPulse = null;
        }
        this.calendarSource = courseSource;
        this.selectedBundle = localizationService.getBundle();

        Long userId = resolveCurrentUserId();
        List<StudentGroup> groups;
        try {
            groups = new GroupService(new GroupDao()).getGroupsForUser(userId);
        } catch (Exception e) {
            LOGGER.warn("[SourceTrayController] Failed to load groups: {}", e.getMessage());
            groups = Collections.emptyList();
        }

        this.groupsList = FXCollections.observableArrayList(groups);

        ScrollPane sourceScrollPane = calendarView.lookupAll(".source-view-scroll-pane").stream()
                .filter(ScrollPane.class::isInstance)
                .map(ScrollPane.class::cast)
                .findFirst()
                .orElse(null);

        if (sourceScrollPane == null || sourceScrollPane.getContent() == null) {
            return;
        }

        Node coursesSection = createSourceSectionNode(courseSource);
        Node groupsSection = createGroupsSectionNode(groupsList);
        if (coursesSection == null || groupsSection == null) {
            return;
        }

        Separator sectionDivider = new Separator();
        VBox.setMargin(sectionDivider, new Insets(20, 0, 0, 0));
        VBox wrapper = new VBox(12, coursesSection, sectionDivider, groupsSection);
        wrapper.getStyleClass().add("source-tray-content");
        sourceScrollPane.setFitToWidth(true);
        sourceScrollPane.setContent(wrapper);
    }

    private Long resolveCurrentUserId() {
        if (SessionManager.getInstance().isTeacher()) {
            return null;
        }
        return SessionManager.getInstance().getCurrentUser() != null
                ? SessionManager.getInstance().getCurrentUser().getId()
                : null;
    }

    /**
     * Builds the courses section node.
     */
    private Node createSourceSectionNode(CalendarSource source) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/ui/sourcetray-groups.fxml"));
        groupsLoader.setController(this);
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            LOGGER.error(UNEXPECTED_ERROR, e);
            return null;
        }

        Label sectionTitleText = (Label) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");
        Button addButton = (Button) groupsLoader.getNamespace().get("addButton");

        if (sectionTitleText == null || groupsListContainer == null) {
            return null;
        }

        if (addButton != null) {
            addButton.setUserData(SECTION_TYPE_COURSE);
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

        Runnable refreshRows = () -> {
            groupsListContainer.getChildren().clear();
            for (Calendar<?> calendar : source.getCalendars()) {
                Node row = sourceTrayRowFactory.createCalendarRow(
                        calendar,
                        SessionManager.getInstance().isTeacher(),
                        e -> openModal(calendar, SECTION_TYPE_COURSE, e)
                );
                groupsListContainer.getChildren().add(row);
            }
        };

        sectionTitleText.setText(source.getName());
        refreshRows.run();

        source.nameProperty().addListener((obs, oldName, newName) -> sectionTitleText.setText(newName));
        @SuppressWarnings("rawtypes")
        ListChangeListener calendarListener = change -> refreshRows.run();
        source.getCalendars().addListener(calendarListener);

        return sourceSection;
    }

    /**
     * Builds the groups section node.
     */
    private Node createGroupsSectionNode(ObservableList<StudentGroup> items) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/ui/sourcetray-groups.fxml"));
        groupsLoader.setController(this);
        Node sourceSection;
        try {
            sourceSection = groupsLoader.load();
        } catch (IOException e) {
            LOGGER.error(UNEXPECTED_ERROR, e);
            return null;
        }

        Label sectionTitleText = (Label) groupsLoader.getNamespace().get("sectionTitleText");
        VBox groupsListContainer = (VBox) groupsLoader.getNamespace().get("groupsListContainer");
        Button addButton = (Button) groupsLoader.getNamespace().get("addButton");

        if (sectionTitleText == null || groupsListContainer == null) {
            return null;
        }

        if (addButton != null) {
            addButton.setUserData(SECTION_TYPE_GROUP);
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

        sectionTitleText.setText(selectedBundle.getString("sourcetray.groups.section.title"));

        groupsListContainer.getChildren().clear();
        for (StudentGroup group : items) {
            Node row = sourceTrayRowFactory.createGroupRow(
                    group,
                    SessionManager.getInstance().isTeacher(),
                    e -> openModal(group, SECTION_TYPE_GROUP, e)
            );
            groupsListContainer.getChildren().add(row);
        }

        items.addListener(buildGroupChangeListener(groupsListContainer, items));

        return sourceSection;
    }

    private ListChangeListener<StudentGroup> buildGroupChangeListener(VBox groupsListContainer,
                                                                       ObservableList<StudentGroup> items) {
        return change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    handleGroupsAdded(change.getAddedSubList(), groupsListContainer);
                }
                if (change.wasRemoved()) {
                    rebuildGroupRows(items, groupsListContainer);
                }
            }
        };
    }

    private void handleGroupsAdded(List<? extends StudentGroup> addedGroups, VBox container) {
        for (StudentGroup added : addedGroups) {
            Node row = sourceTrayRowFactory.createGroupRow(
                    added,
                    SessionManager.getInstance().isTeacher(),
                    e -> openModal(added, SECTION_TYPE_GROUP, e)
            );
            container.getChildren().add(row);
        }
    }

    private void rebuildGroupRows(ObservableList<StudentGroup> items, VBox container) {
        container.getChildren().clear();
        for (StudentGroup group : items) {
            Node row = sourceTrayRowFactory.createGroupRow(
                    group,
                    SessionManager.getInstance().isTeacher(),
                    e -> openModal(group, SECTION_TYPE_GROUP, e)
            );
            container.getChildren().add(row);
        }
    }

    /**
     * FXML action handler for both "+" add-buttons in the source tray.
     */
    public void handleOpenSidebarModal(ActionEvent event) {
        Button source = (Button) event.getSource();
        String sectionType = source.getUserData() != null ? source.getUserData().toString() : SECTION_TYPE_GROUP;
        if (SECTION_TYPE_COURSE.equalsIgnoreCase(sectionType)) {
            openModal((Calendar<?>) null, SECTION_TYPE_COURSE, event);
        } else {
            openModal((StudentGroup) null, SECTION_TYPE_GROUP, event);
        }
    }

    @SuppressWarnings("unchecked")
    private void openModal(Calendar<?> calendar, String sectionType, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/modals/source-tray-course-modal/course-modal.fxml"),
                    localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateCourseModalController modalController = loader.getController();
            modalController.setCalendar((Calendar<org.entities.Course>) calendar);
            modalController.setCalendarSource(calendarSource);
            modalController.applyProps();
            showModal(root, calendar != null ? calendar.getName() : null, sectionType, event);
        } catch (Exception e) {
            LOGGER.error(UNEXPECTED_ERROR, e);
            throw new IllegalStateException("Failed to open course modal", e);
        }
    }

    private void openModal(StudentGroup group, String sectionType, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/ui/modals/source-tray-group-modal/group-modal.fxml");
            if (fxmlUrl == null) {
                LOGGER.warn("[SourceTrayController] group-modal.fxml not found on classpath");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl, localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateGroupModalController modalController = loader.getController();
            if (modalController == null) {
                LOGGER.warn("[SourceTrayController] CreateGroupModalController is null after load");
                return;
            }
            if (group != null) {
                modalController.setGroup(group);
            }
            modalController.setGroupsList(groupsList);
            modalController.applyProps();
            System.out.println(group.getDisplayFieldOfStudies());
            showModal(root, group != null ? group.getDisplayFieldOfStudies() : null, sectionType, event);
        } catch (Exception e) {
            LOGGER.warn("[SourceTrayController] Failed to open group modal: {}", e.getMessage());
        }
    }

    private void showModal(Parent root, String entityName, String sectionType, ActionEvent event) {
        Stage dialogStage = new Stage();
        boolean isEditMode = entityName != null;
        String titleKey = resolveModalTitleKey(sectionType, isEditMode);
        dialogStage.setTitle(localizationService.getBundle().getString(titleKey));
        dialogStage.setScene(new Scene(root));
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());
        dialogStage.setResizable(false);
        dialogStage.showAndWait();
    }

    private String resolveModalTitleKey(String sectionType, boolean isEditMode) {
        boolean isCourse = SECTION_TYPE_COURSE.equalsIgnoreCase(sectionType);
        if (isCourse) {
            return isEditMode ? "modal.edit.course.title" : "modal.create.course.title";
        }
        return isEditMode ? "modal.edit.group.title" : "modal.create.group.title";
    }
}
