import { LitElement, html, css, PropertyValues } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import '@vaadin/icon';
import '@vaadin/icons';

interface CustomAction {
    id: string;
    icon: string;
    tooltip: string;
    colorClass: string;
}

/**
 * Floating toolbar component for bulk actions on selected items.
 * Appears when items are selected and provides quick access to common actions.
 * Supports both standard actions (edit, export, delete) and custom actions.
 */
@customElement('bervan-floating-toolbar')
export class BervanFloatingToolbar extends LitElement {
    static styles = css`
        :host {
            display: block;
        }

        .floating-toolbar {
            position: fixed;
            bottom: 32px;
            left: 50%;
            transform: translateX(-50%) translateY(100px);
            display: flex;
            align-items: center;
            gap: 16px;
            padding: 8px 24px;
            background: rgba(17, 25, 40, 0.85);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.18);
            border-radius: 9999px;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1),
                        0 10px 10px -5px rgba(0, 0, 0, 0.04),
                        0 0 20px rgba(99, 102, 241, 0.3);
            z-index: 1000;
            opacity: 0;
            visibility: hidden;
            transition: transform 500ms cubic-bezier(0.34, 1.56, 0.64, 1),
                        opacity 250ms cubic-bezier(0.4, 0, 0.2, 1),
                        visibility 250ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        .floating-toolbar.visible {
            transform: translateX(-50%) translateY(0);
            opacity: 1;
            visibility: visible;
        }

        .selection-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 24px;
            height: 24px;
            padding: 0 8px;
            background: #6366f1;
            border-radius: 9999px;
            color: white;
            font-size: 12px;
            font-weight: 700;
        }

        .divider {
            width: 1px;
            height: 24px;
            background: rgba(255, 255, 255, 0.2);
        }

        .action-group {
            display: flex;
            align-items: center;
            gap: 4px;
        }

        .action-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 40px;
            height: 40px;
            padding: 0;
            background: transparent;
            border: none;
            border-radius: 8px;
            color: rgba(255, 255, 255, 0.7);
            cursor: pointer;
            transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
        }

        .action-btn:hover {
            background: rgba(255, 255, 255, 0.1);
            color: rgba(255, 255, 255, 0.95);
        }

        .action-btn:active {
            transform: scale(0.95);
        }

        .action-btn:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        /* Standard action colors */
        .action-btn.edit:hover {
            color: #6366f1;
            background: rgba(99, 102, 241, 0.2);
        }

        .action-btn.duplicate:hover {
            color: #22d3ee;
            background: rgba(34, 211, 238, 0.2);
        }

        .action-btn.export:hover {
            color: #10b981;
            background: rgba(16, 185, 129, 0.2);
        }

        .action-btn.delete:hover {
            color: #ef4444;
            background: rgba(239, 68, 68, 0.2);
        }

        /* Custom action colors */
        .action-btn.success:hover {
            color: #10b981;
            background: rgba(16, 185, 129, 0.2);
        }

        .action-btn.warning:hover {
            color: #f59e0b;
            background: rgba(245, 158, 11, 0.2);
        }

        .action-btn.info:hover {
            color: #3b82f6;
            background: rgba(59, 130, 246, 0.2);
        }

        .action-btn.accent:hover {
            color: #22d3ee;
            background: rgba(34, 211, 238, 0.2);
        }

        .action-btn.primary:hover {
            color: #6366f1;
            background: rgba(99, 102, 241, 0.2);
        }

        .action-btn.danger:hover {
            color: #ef4444;
            background: rgba(239, 68, 68, 0.2);
        }

        .action-btn vaadin-icon {
            width: 20px;
            height: 20px;
        }

        /* Tooltip */
        .action-btn::after {
            content: attr(data-tooltip);
            position: absolute;
            bottom: 100%;
            left: 50%;
            transform: translateX(-50%) translateY(4px);
            padding: 4px 8px;
            background: #0f172a;
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 4px;
            color: rgba(255, 255, 255, 0.95);
            font-size: 11px;
            white-space: nowrap;
            opacity: 0;
            visibility: hidden;
            pointer-events: none;
            transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        .action-btn:hover::after {
            opacity: 1;
            visibility: visible;
            transform: translateX(-50%) translateY(-4px);
        }

        .close-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 28px;
            height: 28px;
            padding: 0;
            background: rgba(255, 255, 255, 0.1);
            border: none;
            border-radius: 50%;
            color: rgba(255, 255, 255, 0.5);
            cursor: pointer;
            transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
            margin-left: 8px;
        }

        .close-btn:hover {
            background: rgba(255, 255, 255, 0.2);
            color: rgba(255, 255, 255, 0.9);
        }

        .close-btn vaadin-icon {
            width: 14px;
            height: 14px;
        }
    `;

    @property({ type: Number })
    selectedCount: number = 0;

    @property({ type: Boolean })
    editEnabled: boolean = true;

    @property({ type: Boolean })
    duplicateEnabled: boolean = false;

    @property({ type: Boolean })
    exportEnabled: boolean = true;

    @property({ type: Boolean })
    deleteEnabled: boolean = true;

    @property({ type: Boolean })
    singleSelectOnly: boolean = false;

    @property({ type: Array })
    customActions: CustomAction[] = [];

    @state()
    private _visible: boolean = false;

    updated(changedProperties: PropertyValues) {
        if (changedProperties.has('selectedCount')) {
            this._visible = this.selectedCount > 0;
        }
    }

    private _handleEdit() {
        this.dispatchEvent(new CustomEvent('edit-click', { bubbles: true, composed: true }));
    }

    private _handleDuplicate() {
        this.dispatchEvent(new CustomEvent('duplicate-click', { bubbles: true, composed: true }));
    }

    private _handleExport() {
        this.dispatchEvent(new CustomEvent('export-click', { bubbles: true, composed: true }));
    }

    private _handleDelete() {
        this.dispatchEvent(new CustomEvent('delete-click', { bubbles: true, composed: true }));
    }

    private _handleClose() {
        this.dispatchEvent(new CustomEvent('close-click', { bubbles: true, composed: true }));
    }

    private _handleCustomAction(actionId: string) {
        this.dispatchEvent(new CustomEvent('custom-action-click', {
            bubbles: true,
            composed: true,
            detail: { actionId }
        }));
    }

    render() {
        const editDisabled = this.singleSelectOnly && this.selectedCount > 1;

        return html`
            <div class="floating-toolbar ${this._visible ? 'visible' : ''}">
                <span class="selection-badge">${this.selectedCount}</span>

                <div class="divider"></div>

                <div class="action-group">
                    ${this.editEnabled ? html`
                        <button
                            class="action-btn edit"
                            data-tooltip="Edit"
                            ?disabled=${editDisabled}
                            @click=${this._handleEdit}>
                            <vaadin-icon icon="vaadin:edit"></vaadin-icon>
                        </button>
                    ` : ''}

                    ${this.duplicateEnabled ? html`
                        <button
                            class="action-btn duplicate"
                            data-tooltip="Duplicate"
                            @click=${this._handleDuplicate}>
                            <vaadin-icon icon="vaadin:copy-o"></vaadin-icon>
                        </button>
                    ` : ''}

                    ${this.exportEnabled ? html`
                        <button
                            class="action-btn export"
                            data-tooltip="Export"
                            @click=${this._handleExport}>
                            <vaadin-icon icon="vaadin:download"></vaadin-icon>
                        </button>
                    ` : ''}

                    ${this.deleteEnabled ? html`
                        <button
                            class="action-btn delete"
                            data-tooltip="Delete"
                            @click=${this._handleDelete}>
                            <vaadin-icon icon="vaadin:trash"></vaadin-icon>
                        </button>
                    ` : ''}

                    ${this.customActions.map(action => html`
                        <button
                            class="action-btn ${action.colorClass}"
                            data-tooltip="${action.tooltip}"
                            @click=${() => this._handleCustomAction(action.id)}>
                            <vaadin-icon icon="${action.icon}"></vaadin-icon>
                        </button>
                    `)}
                </div>

                <button class="close-btn" @click=${this._handleClose}>
                    <vaadin-icon icon="vaadin:close-small"></vaadin-icon>
                </button>
            </div>
        `;
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'bervan-floating-toolbar': BervanFloatingToolbar;
    }
}
