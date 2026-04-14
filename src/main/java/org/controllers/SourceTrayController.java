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

/**
 * Controller for the CalendarFX source-tray sidebar.
 * Replaces the default CalendarFX source-tray content with two custom sections:
 * <ul>
 *   <li><b>Courses</b> — one colored row per {@link com.calendarfx.model.Calendar},
 *       with an edit button visible only to teachers.</li>
 *   <li><b>Groups</b> — one row per {@link org.entities.StudentGroup} from the database,
 *       also teacher-editable. Backed by an {@link ObservableList} so the tray
 *       updates live when groups are added or deleted.</li>
 * </ul>
 * Both sections are loaded from {@code sourcetray-groups.fxml} using this controller
 * as their FXML controller, allowing the FXML action handler
 * ({@link #handleOpenSidebarModal(ActionEvent)}) to serve both add-buttons.
 */
public class SourceTrayController {

    private static final String SECTION_TYPE_COURSE = "COURSE";
    private static final String SECTION_TYPE_GROUP = "GROUP";
    private CalendarSource calendarSource;
    private ObservableList<StudentGroup> groupsList;
    private Timeline skeletonPulse;

    private final LocalizationService localizationService = new LocalizationService();
    private final SourceTrayRowFactory sourceTrayRowFactory = new SourceTrayRowFactory();
    ResourceBundle selectedBundle = localizationService.getBundle();


    /**
     * Injects shimmer skeleton placeholders into the source-tray scroll pane so the
     * sidebar has content while the real lesson/group data loads in the background.
     * The skeleton pulses between 45 % and 95 % opacity on a 1.4-second loop.
     * It is automatically replaced (and the animation stopped) when
     * {@link #addSourceSectionsToSourceTray} is called.
     *
     * @param calendarView the CalendarFX view whose source tray to populate
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

        // — Courses section —
        skeleton.getChildren().add(bone(110, 13));  // section title
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30)); // row
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30)); // row
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30)); // row
        skeleton.getChildren().add(bone(60, 24));               // add button

        Separator sep = new Separator();
        VBox.setMargin(sep, new Insets(10, 0, 10, 0));
        skeleton.getChildren().add(sep);

        // — Groups section —
        skeleton.getChildren().add(bone(90, 13));               // section title
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30)); // row
        skeleton.getChildren().add(bone(Double.MAX_VALUE, 30)); // row

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

    /** Returns a rounded grey placeholder rectangle used inside the skeleton loader. */
    private Region bone(double maxWidth, double height) {
        Region r = new Region();
        r.setMaxWidth(maxWidth);
        r.setPrefHeight(height);
        r.setStyle("-fx-background-color: #DEDEDE; -fx-background-radius: 6;");
        return r;
    }

    /**
     * Replaces the CalendarFX source-tray scroll-pane content with the two custom
     * sections (courses + groups). Fetches groups from the database using the current
     * user's ID (students see only their own groups; teachers see all groups).
     * Must be called after the {@link CalendarView} skin has been applied so the
     * scroll-pane lookup succeeds.
     *
     * @param calendarView the CalendarFX view whose source tray to populate
     * @param courseSource the {@link CalendarSource} backing the courses section
     */
    public void addSourceSectionsToSourceTray(CalendarView calendarView, CalendarSource courseSource) {
        if (skeletonPulse != null) {
            skeletonPulse.stop();
            skeletonPulse = null;
        }
        this.calendarSource = courseSource;
        this.selectedBundle = localizationService.getBundle();

        // Fetch groups
        Long userId = SessionManager.getInstance().isTeacher() ? null
                : (SessionManager.getInstance().getCurrentUser() != null
                        ? SessionManager.getInstance().getCurrentUser().getId() : null);
        List<StudentGroup> groups;

        try {
            groups = new GroupService(new GroupDao()).getGroupsForUser(userId);
        } catch (Exception e) {
            System.out.println("[SourceTrayController] Failed to load groups: " + e.getMessage());
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
        Node groupsSection = createGroupsSectionNode("Groups", groupsList);
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

    /**
     * Builds the courses section node from {@code sourcetray-groups.fxml}.
     * Wires up reactive listeners so the row list refreshes whenever a calendar
     * is added to or removed from {@code source}.
     *
     * @param source the {@link CalendarSource} whose calendars to display
     * @return the root {@link Node} for the section, or {@code null} on load failure
     */
    private Node createSourceSectionNode(CalendarSource source) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/ui/sourcetray-groups.fxml"));
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
            for (Calendar calendar : source.getCalendars()) {
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
        source.getCalendars().addListener((ListChangeListener<Calendar>) change -> refreshRows.run());

        return sourceSection;
    }

    /**
     * Builds the groups section node from {@code sourcetray-groups.fxml}.
     * Listens on {@code items} for additions (appended individually) and removals
     * (full rebuild for safety).
     *
     * @param sectionTitle the section header label (currently overridden by the bundle key)
     * @param items        the live observable list of groups to display
     * @return the root {@link Node} for the section, or {@code null} on load failure
     */
    private Node createGroupsSectionNode(String sectionTitle, ObservableList<StudentGroup> items) {
        FXMLLoader groupsLoader = new FXMLLoader(getClass().getResource("/ui/sourcetray-groups.fxml"));
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

        if (sectionTitleText == null || groupsListContainer == null) {
            return null;
        }

        if (addButton != null) {
            addButton.setUserData(SECTION_TYPE_GROUP);
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

        sectionTitleText.setText(selectedBundle.getString("sourcetray.groups.section.title"));


        // Render initial rows
        groupsListContainer.getChildren().clear();
        for (StudentGroup group : items) {
            Node row = sourceTrayRowFactory.createGroupRow(
                    group,
                    SessionManager.getInstance().isTeacher(),
                    e -> openModal(group, SECTION_TYPE_GROUP, e)
            );
            groupsListContainer.getChildren().add(row);
        }

        // Listen for new groups added/removed so the tray updates live
        items.addListener((ListChangeListener<StudentGroup>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (StudentGroup added : change.getAddedSubList()) {
                        Node row = sourceTrayRowFactory.createGroupRow(
                                added,
                                SessionManager.getInstance().isTeacher(),
                                e -> openModal(added, SECTION_TYPE_GROUP, e)
                        );
                        groupsListContainer.getChildren().add(row);
                    }
                }
                if (change.wasRemoved()) {
                    // Rebuild on removal — simplest safe approach
                    groupsListContainer.getChildren().clear();
                    for (StudentGroup group : items) {
                        Node row = sourceTrayRowFactory.createGroupRow(
                                group,
                                SessionManager.getInstance().isTeacher(),
                                e -> openModal(group, SECTION_TYPE_GROUP, e)
                        );
                        groupsListContainer.getChildren().add(row);
                    }
                }
            }
        });

        return sourceSection;
    }

    /**
     * FXML action handler for both "+" add-buttons in the source tray.
     * Reads the button's {@code userData} to decide whether to open the course modal
     * ({@link #openModal(Calendar, String, ActionEvent)}) or the group modal
     * ({@link #openModal(StudentGroup, String, ActionEvent)}).
     *
     * @param event the action event from the clicked add-button
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

    /**
     * Opens the course create/edit modal ({@code course-modal.fxml}).
     *
     * @param calendar    the calendar to edit, or {@code null} to create a new course
     * @param sectionType the section type constant ({@value #SECTION_TYPE_COURSE})
     * @param event       the originating action event (used to resolve the owner window)
     */
    private void openModal(Calendar<?> calendar, String sectionType, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ui/modals/source-tray-course-modal/course-modal.fxml"),
                    localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateCourseModalController modalController = loader.getController();
            modalController.setCalendar(calendar);
            modalController.setCalendarSource(calendarSource);
            modalController.applyProps();
            showModal(root, calendar != null ? calendar.getName() : null, sectionType, event);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to open course modal", e);
        }
    }

    /**
     * Opens the group create/edit modal ({@code group-modal.fxml}).
     *
     * @param group       the group to edit, or {@code null} to create a new group
     * @param sectionType the section type constant ({@value #SECTION_TYPE_GROUP})
     * @param event       the originating action event (used to resolve the owner window)
     */
    private void openModal(StudentGroup group, String sectionType, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/ui/modals/source-tray-group-modal/group-modal.fxml");
            if (fxmlUrl == null) {
                System.out.println("[SourceTrayController] group-modal.fxml not found on classpath");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl, localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateGroupModalController modalController = loader.getController();
            if (modalController == null) {
                System.out.println("[SourceTrayController] CreateGroupModalController is null after load");
                return;
            }
            if (group != null) {
                modalController.setGroup(group);
            }
            modalController.setGroupsList(groupsList);
            modalController.applyProps();
            showModal(root, group != null ? group.getFieldOfStudies() : null, sectionType, event);
        } catch (Exception e) {
            System.out.println("[SourceTrayController] Failed to open group modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Displays a loaded FXML root as an application-modal, non-resizable dialog.
     * Resolves the window title from the bundle using the section type and edit/create mode.
     *
     * @param root        the loaded FXML scene root
     * @param entityName  the existing entity's name ({@code null} = create mode)
     * @param sectionType the section type constant
     * @param event       the originating action event (used to resolve the owner window)
     */
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

    /**
     * Returns the bundle key for the modal window title based on section type and mode.
     *
     * @param sectionType the section type constant
     * @param isEditMode  {@code true} for edit, {@code false} for create
     * @return the i18n bundle key string
     */
    private String resolveModalTitleKey(String sectionType, boolean isEditMode) {
        boolean isCourse = SECTION_TYPE_COURSE.equalsIgnoreCase(sectionType);
        if (isCourse) {
            return isEditMode ? "modal.edit.course.title" : "modal.create.course.title";
        }
        return isEditMode ? "modal.edit.group.title" : "modal.create.group.title";
    }

}

