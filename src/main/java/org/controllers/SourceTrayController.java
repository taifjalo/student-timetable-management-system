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
 * ({@link #handleOpenCreateGroupModal(ActionEvent)}) to serve both add-buttons.
 */
public class SourceTrayController {

    private static final String SECTION_TYPE_COURSE = "COURSE";
    private static final String SECTION_TYPE_GROUP = "GROUP";
    private CalendarSource calendarSource;
    private ObservableList<StudentGroup> groupsList;

    private final LocalizationService localizationService = new LocalizationService();
    ResourceBundle selectedBundle = localizationService.getBundle();


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

    /**
     * Builds the courses section node from {@code sourcetray-groups.fxml}.
     * Wires up reactive listeners so the row list refreshes whenever a calendar
     * is added to or removed from {@code source}.
     *
     * @param source the {@link CalendarSource} whose calendars to display
     * @return the root {@link Node} for the section, or {@code null} on load failure
     */
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
            addButton.setUserData(SECTION_TYPE_COURSE);
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

        Runnable refreshRows = () -> {
            groupsListContainer.getChildren().clear();
            for (Calendar calendar : source.getCalendars()) {
                groupsListContainer.getChildren().add(createCalendarRow(calendar, SECTION_TYPE_COURSE));
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
            addButton.setUserData(SECTION_TYPE_GROUP);
            addButton.setVisible(SessionManager.getInstance().isTeacher());
            addButton.setManaged(SessionManager.getInstance().isTeacher());
        }

//        sectionTitleText.setText(sectionTitle);
        sectionTitleText.setText(selectedBundle.getString("sourcetray.groups.section.title"));


        // Render initial rows
        groupsListContainer.getChildren().clear();
        for (StudentGroup group : items) {
            groupsListContainer.getChildren().add(createGroupRow(group, SECTION_TYPE_GROUP));
        }

        // Listen for new groups added/removed so the tray updates live
        items.addListener((javafx.collections.ListChangeListener<StudentGroup>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (StudentGroup added : change.getAddedSubList()) {
                        groupsListContainer.getChildren().add(createGroupRow(added, SECTION_TYPE_GROUP));
                    }
                }
                if (change.wasRemoved()) {
                    // Rebuild on removal — simplest safe approach
                    groupsListContainer.getChildren().clear();
                    for (StudentGroup group : items) {
                        groupsListContainer.getChildren().add(createGroupRow(group, SECTION_TYPE_GROUP));
                    }
                }
            }
        });

        return sourceSection;
    }

    /**
     * FXML action handler for both "+" add-buttons in the source tray.
     * Reads the button's {@code userData} to decide whether to open the course modal
     * ({@link #openGroupModal(Calendar, String, ActionEvent)}) or the group modal
     * ({@link #openGroupModal(StudentGroup, String, ActionEvent)}).
     *
     * @param event the action event from the clicked add-button
     */
    public void handleOpenCreateGroupModal(ActionEvent event) {
        Button source = (Button) event.getSource();
        String sectionType = source.getUserData() != null ? source.getUserData().toString() : SECTION_TYPE_GROUP;
        if (SECTION_TYPE_COURSE.equalsIgnoreCase(sectionType)) {
            openGroupModal((Calendar) null, SECTION_TYPE_COURSE, event);
        } else {
            openGroupModal((StudentGroup) null, SECTION_TYPE_GROUP, event);
        }
    }

    /**
     * Opens the course create/edit modal ({@code course-modal.fxml}).
     *
     * @param calendar    the calendar to edit, or {@code null} to create a new course
     * @param sectionType the section type constant ({@value #SECTION_TYPE_COURSE})
     * @param event       the originating action event (used to resolve the owner window)
     */
    private void openGroupModal(Calendar calendar, String sectionType, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/source-tray-course-modal/course-modal.fxml"),localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateCourseModalController modalController = loader.getController();
            modalController.setCalendar(calendar);
            modalController.setCalendarSource(calendarSource);
            modalController.applyProps();
            showModal(root, calendar != null ? calendar.getName() : null, sectionType, event);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to open course modal", e);
        }
    }

    /**
     * Opens the group create/edit modal ({@code create-modal.fxml}).
     *
     * @param group       the group to edit, or {@code null} to create a new group
     * @param sectionType the section type constant ({@value #SECTION_TYPE_GROUP})
     * @param event       the originating action event (used to resolve the owner window)
     */
    private void openGroupModal(StudentGroup group, String sectionType, ActionEvent event) {
        try {
            java.net.URL fxmlUrl = getClass().getResource("/source-tray-create-modal/create-modal.fxml");
            if (fxmlUrl == null) {
                System.err.println("[SourceTrayController] create-modal.fxml not found on classpath");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl, localizationService.getBundle());
            Parent root = loader.load();
            localizationService.swapSides(root);
            CreateGroupModalController modalController = loader.getController();
            if (modalController == null) {
                System.err.println("[SourceTrayController] CreateGroupModalController is null after load");
                return;
            }
            if (group != null) {
                modalController.setGroup(group);
            }
            modalController.setGroupsList(groupsList);
            modalController.applyProps();
            showModal(root, group != null ? group.getFieldOfStudies() : null, sectionType, event);
        } catch (Exception e) {
            System.err.println("[SourceTrayController] Failed to open group modal: " + e.getMessage());
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

    /**
     * Builds a single row in the courses section: a colored dot, the calendar name,
     * a spacer, and an edit-icon button (teacher-only). The dot and name are kept
     * in sync via property listeners.
     *
     * @param calendar    the CalendarFX calendar to represent
     * @param sectionType the section type constant (passed to the edit modal)
     * @return the constructed {@link HBox} row node
     */
    private HBox createCalendarRow(Calendar calendar, String sectionType) {
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
        actionButton.setOnAction(e -> openGroupModal(calendar, sectionType, e));
        actionButton.setVisible(SessionManager.getInstance().isTeacher());
        actionButton.setManaged(SessionManager.getInstance().isTeacher());

        HBox row = new HBox(colorDot, nameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    /**
     * Builds a single row in the groups section: the group name, a spacer, and an
     * edit-icon button (teacher-only).
     *
     * @param group       the group to represent
     * @param sectionType the section type constant (passed to the edit modal)
     * @return the constructed {@link HBox} row node
     */
    private HBox createGroupRow(StudentGroup group, String sectionType) {

        Text groupNameText = new Text(group.getDisplayFieldOfStudies());
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
        actionButton.setOnAction(e -> openGroupModal(group, sectionType, e));
        actionButton.setVisible(SessionManager.getInstance().isTeacher());
        actionButton.setManaged(SessionManager.getInstance().isTeacher());

        HBox row = new HBox(groupNameText, spacer, actionButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }


    /**
     * Maps a CalendarFX style name string (e.g. {@code "style1"}) to its corresponding
     * hex color string. Case-insensitive.
     *
     * @param style the CalendarFX style name
     * @return hex color string, or {@code "#888888"} for unknown styles
     */
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

