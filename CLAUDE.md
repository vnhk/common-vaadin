# Common Vaadin - Project Notes

> **IMPORTANT**: Keep this file updated when making significant changes to the codebase. This file serves as persistent memory between Claude Code sessions.

## Overview
Shared Vaadin components library used across multiple apps (learning-language, invest-track, etc.)

## Key Architecture

### AbstractBervanTableView
Base class for all table views. Supports:
- Pagination with page size selector
- Checkbox selection with bulk actions
- Filtering (text search, checkbox filters, date range, etc.)
- Sorting
- Export (with password decryption support)
- **Modern features** (opt-in via `BervanTableConfig`)

### BervanTableToolbar
Toolbar with bulk actions (Edit, Delete, Export).
- Uses **icon buttons** when floating toolbar is enabled
- Views can add custom actions (e.g., activate/deactivate in learning-language)
- Always keep toolbar visible for custom actions

### BervanFloatingToolbar
Icon-based floating action bar that appears when items are selected:
- Shows selection count badge
- Standard actions: Edit, Export, Delete (with icons)
- **Custom actions support** - views can add custom icon buttons
- Appears at bottom center of screen with glassmorphism styling

### Modern Table Features (BervanTableConfig)
New opt-in features added via second constructor:
```java
new AbstractBervanTableView(nav, service, config, Entity.class,
    BervanTableConfig.builder()
        .floatingToolbarEnabled(true)      // Icon-based floating toolbar
        .glassmorphismEnabled(true)        // Modern glass styling
        .pageSizeSelectorEnabled(true)     // Page size dropdown
        .keyboardNavigationEnabled(true)   // Escape, Delete, Ctrl+A
        .columnToggleEnabled(true)         // Column visibility toggle
        .build()
);
```

**NOTE**: Constructor always overrides to `enableAllModernFeatures()` - all flags are always ON.

### Column Toggle
- Toggle button (VaadinIcon.GRID_H) added to `topTableActions` toolbar
- Opens Dialog with checkboxes per column from `columnMap`
- `columnMap` populated in `buildGridAutomatically()` for auto-created columns
- Visibility persisted via `BervanTableState` → localStorage per table/user
- Key: `{stateKeyPrefix}-{SimpleClassName}`

## Custom Toolbar Actions

### Adding Custom Actions to Floating Toolbar
Custom toolbars extending `BervanTableToolbar` can add actions to the floating toolbar:

```java
public class MyCustomToolbar extends BervanTableToolbar<UUID, MyEntity> {
    private BervanFloatingToolbar floatingToolbar;

    public MyCustomToolbar withFloatingToolbar(BervanFloatingToolbar floatingToolbar) {
        this.floatingToolbar = floatingToolbar;
        this.useIconButtons = true; // Enable icon buttons
        return this;
    }

    public MyCustomToolbar withMyCustomAction() {
        // Icon button for main toolbar
        BervanButton btn = new BervanButton(new Icon(VaadinIcon.CHECK), e -> handleAction());
        btn.getElement().setAttribute("title", "My Action");
        btn.addClassName("bervan-icon-btn");
        btn.addClassName("success");  // Color class
        actionsToBeAdded.add(btn);

        // Also add to floating toolbar
        if (floatingToolbar != null) {
            floatingToolbar.addCustomAction(
                "my-action-id",
                "vaadin:check",
                "My Action",
                "success",  // Color: primary, success, warning, danger, info, accent
                event -> handleAction()
            );
        }
        return this;
    }
}
```

### View Integration
```java
@Override
protected void buildToolbarActionBar() {
    MyCustomToolbar toolbar = new MyCustomToolbar(...);
    if (floatingToolbar != null) {
        toolbar.withFloatingToolbar(floatingToolbar);
    }
    tableToolbarActions = toolbar
        .withMyCustomAction()
        .withEditButton(service)
        .withDeleteButton()
        .build();
}
```

## File Structure

### Java Classes
- `view/table/BervanTableConfig.java` - Feature flags builder
- `view/table/BervanTableState.java` - localStorage persistence
- `view/table/MultiSortState.java` - Multi-column sort tracking
- `view/table/ExportService.java` - CSV/Excel/JSON export utilities
- `component/table/BervanFloatingToolbar.java` - Floating toolbar with custom action support
- `component/table/BervanPageSizeSelector.java` - Page size dropdown

### Frontend Resources (META-INF/resources/frontend/)
TypeScript Lit components:
- `src/bervan-table/bervan-floating-toolbar.ts` - Renders icon buttons, supports customActions array
- `src/bervan-table/bervan-page-size-selector.ts`

CSS (glassmorphism theme):
- `bervan-variables.css` - Design system variables
- `bervan-glassmorphism.css` - Glass effects
- `bervan-table.css` - Table styles
- `bervan-toolbar.css` - Toolbar styles (icon button colors)
- `bervan-navigation.css` - **Navigation pill tabs with glassmorphism**
- `bervan-project-mgmt.css` - Project Management module styles (relations, badges, inline-edit)

### Custom Toolbars Across Modules
- `learning-language-app`: `LearningLanguageTableToolbar` - Activate/Deactivate (vaadin:check/vaadin:ban)
- `shopping-stats-server-app`: `PricesListToolbar` - Price divide buttons (vaadin:arrow-down)
- `shopping-stats-server-app`: `ProductAlertsToolbar` - Force notification (vaadin:envelope-o)
- `common-vaadin`: `LowCodeGeneratorToolbar` - Run generator (vaadin:cog)

## MenuNavigationComponent

Base class for page navigation with modern pill tabs styling.

### Key Classes
- `MenuNavigationComponent.java` - Abstract base, manages navigation buttons
- `MenuButtonsRow.java` - Custom Vaadin element `<navigation-buttons>`

### CSS Classes
- `.menu-container` - Full-width container with glassmorphism background (covers entire top bar)
  - Uses `position: absolute` to span 100% width of `.view-header`
  - Left-aligned content (`justify-content: flex-start`)
  - `padding-left: 60px` to leave space for drawer toggle
- `.navigation-button` - Pill-shaped button with hover effects
- `.selected-route-button` - Active button with primary color + glow
- `.view-toggle` - Drawer toggle (hamburger icon), has `z-index: 10` to stay above menu-container

### Usage
```java
public class MyPageLayout extends MenuNavigationComponent {
    public MyPageLayout(String routeName) {
        super(routeName);
        addButtonIfVisible(menuButtonsRow, ROUTE, "Label", VaadinIcon.ICON.create());
        add(menuButtonsRow);
    }
}
```

### Navigation Cleanup
Navigation buttons are injected into `.view-header` via JavaScript. They must be cleaned up in `MainLayout.afterNavigation()`:
```java
UI.getCurrent().getPage().executeJs(
    "document.querySelectorAll('navigation-buttons').forEach(el => el.remove());"
);
```

## Important Notes

1. **Export uses BervanTableToolbar** - has password decryption for encrypted fields
2. **Custom toolbar actions** - views override `buildToolbarActionBar()` to add actions
3. **Frontend files location** - must be in `src/main/resources/META-INF/resources/frontend/` for JAR packaging
4. **Backward compatibility** - all new features disabled by default
5. **Icon button colors** - use CSS classes: primary, success, warning, danger, info, accent
6. **Table initial data load** - `refreshData()` is called at end of `renderCommonComponents()` to load data on page load

## Icon Reference (Vaadin Icons)
- Edit: `vaadin:edit`
- Delete: `vaadin:trash`
- Export: `vaadin:download`
- Activate/Check: `vaadin:check`
- Deactivate/Ban: `vaadin:ban`
- Email: `vaadin:envelope-o`
- Settings/Cog: `vaadin:cog`
- Arrow down: `vaadin:arrow-down`
- Close: `vaadin:close-small`

## Glassmorphism CSS Classes

### Static classes (`bervan-glassmorphism.css`)
- `.bervan-glass` - Basic glass panel
- `.bervan-glass-card` - Glass card with hover effect
- `.bervan-filter-panel` - Filter panel with glass effect
- `.bervan-glow-*` - Glow effects (primary, accent, success, danger)

### Themeable classes (`my-tools-vaadin-app/main-layout.css`)
All use `--glass-*` CSS variables defined in each theme file:
- `.glass-btn` - Glass button (variants: primary, success, warning, danger)
- `.glass-container` - Gradient background container
- `.glass-card` - Glass card component
- `.budget-tree-*` - Budget tree glassmorphism styles
- `.logs-toolbar` - Logs toolbar glassmorphism
- `.pocket-tile-in-menu` - Pocket tile glassmorphism

### Theme variable pattern
Each theme (`cyberpunk-theme.css`, `ocean-theme.css`, etc.) defines:
- `--glass-bg`, `--glass-border`, `--glass-shadow` - Core glass appearance
- `--glass-primary-*`, `--glass-success-*`, `--glass-warning-*`, `--glass-danger-*` - Button variants
- `--glass-container-gradient`, `--glass-sidebar-gradient` - Container backgrounds
- `--glass-nav-selected-*` - Navigation button styles
