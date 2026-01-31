import { LitElement, html, css } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import '@vaadin/icon';
import '@vaadin/icons';

interface ColumnInfo {
    key: string;
    header: string;
    visible: boolean;
}

/**
 * Column visibility toggle dropdown component.
 * Allows users to show/hide table columns.
 */
@customElement('bervan-column-toggle')
export class BervanColumnToggle extends LitElement {
    static styles = css`
        :host {
            display: inline-block;
            position: relative;
        }

        .toggle-btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            padding: 0;
            background: transparent;
            border: none;
            border-radius: 8px;
            color: rgba(255, 255, 255, 0.7);
            cursor: pointer;
            transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        .toggle-btn:hover {
            background: rgba(255, 255, 255, 0.1);
            color: rgba(255, 255, 255, 0.95);
        }

        .toggle-btn vaadin-icon {
            width: 18px;
            height: 18px;
        }

        .dropdown {
            position: absolute;
            top: 100%;
            right: 0;
            min-width: 200px;
            max-height: 320px;
            margin-top: 8px;
            background: rgba(17, 25, 40, 0.95);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.18);
            border-radius: 12px;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1),
                        0 4px 6px -2px rgba(0, 0, 0, 0.05);
            overflow: hidden;
            opacity: 0;
            visibility: hidden;
            transform: translateY(-8px);
            transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
            z-index: 100;
        }

        .dropdown.open {
            opacity: 1;
            visibility: visible;
            transform: translateY(0);
        }

        .dropdown-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 16px;
            border-bottom: 1px solid rgba(255, 255, 255, 0.1);
        }

        .dropdown-title {
            color: rgba(255, 255, 255, 0.95);
            font-size: 0.875rem;
            font-weight: 600;
        }

        .toggle-all {
            color: #6366f1;
            font-size: 0.75rem;
            background: none;
            border: none;
            cursor: pointer;
            padding: 4px 8px;
            border-radius: 4px;
            transition: background 150ms;
        }

        .toggle-all:hover {
            background: rgba(99, 102, 241, 0.2);
        }

        .column-list {
            max-height: 250px;
            overflow-y: auto;
            padding: 8px 0;
        }

        .column-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 8px 16px;
            cursor: pointer;
            transition: background 150ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        .column-item:hover {
            background: rgba(255, 255, 255, 0.05);
        }

        .column-item input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: #6366f1;
            cursor: pointer;
        }

        .column-item label {
            flex: 1;
            color: rgba(255, 255, 255, 0.7);
            font-size: 0.875rem;
            cursor: pointer;
            user-select: none;
        }

        .column-item:hover label {
            color: rgba(255, 255, 255, 0.95);
        }

        /* Scrollbar styling */
        .column-list::-webkit-scrollbar {
            width: 6px;
        }

        .column-list::-webkit-scrollbar-track {
            background: rgba(255, 255, 255, 0.05);
            border-radius: 3px;
        }

        .column-list::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.2);
            border-radius: 3px;
        }

        .column-list::-webkit-scrollbar-thumb:hover {
            background: rgba(255, 255, 255, 0.3);
        }

        /* Light theme */
        :host([theme="light"]) .toggle-btn {
            color: rgba(15, 23, 42, 0.7);
        }

        :host([theme="light"]) .toggle-btn:hover {
            background: rgba(0, 0, 0, 0.05);
            color: rgba(15, 23, 42, 0.95);
        }

        :host([theme="light"]) .dropdown {
            background: rgba(255, 255, 255, 0.95);
            border-color: rgba(0, 0, 0, 0.1);
        }

        :host([theme="light"]) .dropdown-header {
            border-bottom-color: rgba(0, 0, 0, 0.1);
        }

        :host([theme="light"]) .dropdown-title {
            color: rgba(15, 23, 42, 0.95);
        }

        :host([theme="light"]) .column-item:hover {
            background: rgba(0, 0, 0, 0.03);
        }

        :host([theme="light"]) .column-item label {
            color: rgba(15, 23, 42, 0.7);
        }

        :host([theme="light"]) .column-item:hover label {
            color: rgba(15, 23, 42, 0.95);
        }
    `;

    @property({ type: Array })
    columns: ColumnInfo[] = [];

    @property({ type: String })
    storageKey: string = '';

    @property({ type: String })
    theme: string = 'dark';

    @state()
    private _open: boolean = false;

    connectedCallback() {
        super.connectedCallback();
        this._loadFromStorage();
        document.addEventListener('click', this._handleOutsideClick);
    }

    disconnectedCallback() {
        super.disconnectedCallback();
        document.removeEventListener('click', this._handleOutsideClick);
    }

    private _handleOutsideClick = (e: Event) => {
        if (!this.contains(e.target as Node)) {
            this._open = false;
        }
    };

    private _loadFromStorage() {
        if (this.storageKey && this.columns.length > 0) {
            const stored = localStorage.getItem(this.storageKey);
            if (stored) {
                try {
                    const visibility = JSON.parse(stored) as Record<string, boolean>;
                    this.columns = this.columns.map(col => ({
                        ...col,
                        visible: visibility[col.key] ?? col.visible
                    }));
                } catch (e) {
                    // Invalid stored data, ignore
                }
            }
        }
    }

    private _saveToStorage() {
        if (this.storageKey) {
            const visibility: Record<string, boolean> = {};
            this.columns.forEach(col => {
                visibility[col.key] = col.visible;
            });
            localStorage.setItem(this.storageKey, JSON.stringify(visibility));
        }
    }

    private _toggleDropdown() {
        this._open = !this._open;
    }

    private _toggleColumn(key: string) {
        this.columns = this.columns.map(col =>
            col.key === key ? { ...col, visible: !col.visible } : col
        );
        this._saveToStorage();
        this._dispatchChange();
    }

    private _toggleAll(visible: boolean) {
        this.columns = this.columns.map(col => ({ ...col, visible }));
        this._saveToStorage();
        this._dispatchChange();
    }

    private _dispatchChange() {
        this.dispatchEvent(new CustomEvent('column-visibility-change', {
            detail: { columns: this.columns },
            bubbles: true,
            composed: true
        }));
    }

    render() {
        const allVisible = this.columns.every(col => col.visible);
        const noneVisible = this.columns.every(col => !col.visible);

        return html`
            <button class="toggle-btn" @click=${this._toggleDropdown}>
                <vaadin-icon icon="vaadin:eye"></vaadin-icon>
            </button>

            <div class="dropdown ${this._open ? 'open' : ''}">
                <div class="dropdown-header">
                    <span class="dropdown-title">Columns</span>
                    <button
                        class="toggle-all"
                        @click=${() => this._toggleAll(!allVisible)}>
                        ${allVisible ? 'Hide All' : 'Show All'}
                    </button>
                </div>

                <div class="column-list">
                    ${this.columns.map(col => html`
                        <div class="column-item" @click=${() => this._toggleColumn(col.key)}>
                            <input
                                type="checkbox"
                                .checked=${col.visible}
                                @click=${(e: Event) => e.stopPropagation()}>
                            <label>${col.header}</label>
                        </div>
                    `)}
                </div>
            </div>
        `;
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'bervan-column-toggle': BervanColumnToggle;
    }
}
