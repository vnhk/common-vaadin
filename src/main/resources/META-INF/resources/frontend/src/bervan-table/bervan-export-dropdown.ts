import { LitElement, html, css } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import '@vaadin/icon';
import '@vaadin/icons';

export type ExportFormat = 'csv' | 'excel' | 'json';

/**
 * Export format dropdown component.
 * Provides options to export table data in various formats.
 */
@customElement('bervan-export-dropdown')
export class BervanExportDropdown extends LitElement {
    static styles = css`
        :host {
            display: inline-block;
            position: relative;
        }

        .export-btn {
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

        .export-btn:hover {
            background: rgba(16, 185, 129, 0.2);
            color: #10b981;
        }

        .export-btn:disabled {
            opacity: 0.4;
            cursor: not-allowed;
        }

        .export-btn:disabled:hover {
            background: transparent;
            color: rgba(255, 255, 255, 0.7);
        }

        .export-btn vaadin-icon {
            width: 18px;
            height: 18px;
        }

        .dropdown {
            position: absolute;
            top: 100%;
            right: 0;
            min-width: 160px;
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

        .dropdown-item {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px 16px;
            color: rgba(255, 255, 255, 0.7);
            font-size: 0.875rem;
            cursor: pointer;
            transition: all 150ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        .dropdown-item:hover {
            background: rgba(255, 255, 255, 0.05);
            color: rgba(255, 255, 255, 0.95);
        }

        .dropdown-item vaadin-icon {
            width: 18px;
            height: 18px;
            opacity: 0.7;
        }

        .dropdown-item.csv vaadin-icon { color: #10b981; }
        .dropdown-item.excel vaadin-icon { color: #22d3ee; }
        .dropdown-item.json vaadin-icon { color: #f59e0b; }

        .dropdown-item:hover vaadin-icon {
            opacity: 1;
        }

        .dropdown-divider {
            height: 1px;
            margin: 4px 0;
            background: rgba(255, 255, 255, 0.1);
        }

        .format-badge {
            margin-left: auto;
            padding: 2px 6px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 4px;
            font-size: 0.625rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: rgba(255, 255, 255, 0.5);
        }

        /* Light theme */
        :host([theme="light"]) .export-btn {
            color: rgba(15, 23, 42, 0.7);
        }

        :host([theme="light"]) .export-btn:hover {
            background: rgba(16, 185, 129, 0.15);
        }

        :host([theme="light"]) .dropdown {
            background: rgba(255, 255, 255, 0.95);
            border-color: rgba(0, 0, 0, 0.1);
        }

        :host([theme="light"]) .dropdown-item {
            color: rgba(15, 23, 42, 0.7);
        }

        :host([theme="light"]) .dropdown-item:hover {
            background: rgba(0, 0, 0, 0.03);
            color: rgba(15, 23, 42, 0.95);
        }

        :host([theme="light"]) .dropdown-divider {
            background: rgba(0, 0, 0, 0.1);
        }

        :host([theme="light"]) .format-badge {
            background: rgba(0, 0, 0, 0.05);
            color: rgba(15, 23, 42, 0.5);
        }
    `;

    @property({ type: Array })
    formats: ExportFormat[] = ['csv', 'excel', 'json'];

    @property({ type: Boolean })
    disabled: boolean = false;

    @property({ type: String })
    theme: string = 'dark';

    @state()
    private _open: boolean = false;

    connectedCallback() {
        super.connectedCallback();
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

    private _toggleDropdown() {
        if (!this.disabled) {
            this._open = !this._open;
        }
    }

    private _handleExport(format: ExportFormat) {
        this._open = false;
        this.dispatchEvent(new CustomEvent('export-click', {
            detail: { format },
            bubbles: true,
            composed: true
        }));
    }

    private _getFormatIcon(format: ExportFormat): string {
        switch (format) {
            case 'csv': return 'vaadin:file-text-o';
            case 'excel': return 'vaadin:file-table';
            case 'json': return 'vaadin:file-code';
            default: return 'vaadin:file';
        }
    }

    private _getFormatLabel(format: ExportFormat): string {
        switch (format) {
            case 'csv': return 'Export as CSV';
            case 'excel': return 'Export as Excel';
            case 'json': return 'Export as JSON';
            default: return 'Export';
        }
    }

    render() {
        return html`
            <button
                class="export-btn"
                ?disabled=${this.disabled}
                @click=${this._toggleDropdown}>
                <vaadin-icon icon="vaadin:download"></vaadin-icon>
            </button>

            <div class="dropdown ${this._open ? 'open' : ''}">
                ${this.formats.map((format, index) => html`
                    ${index > 0 ? html`<div class="dropdown-divider"></div>` : ''}
                    <div
                        class="dropdown-item ${format}"
                        @click=${() => this._handleExport(format)}>
                        <vaadin-icon icon=${this._getFormatIcon(format)}></vaadin-icon>
                        <span>${this._getFormatLabel(format)}</span>
                        <span class="format-badge">.${format === 'excel' ? 'tsv' : format}</span>
                    </div>
                `)}
            </div>
        `;
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'bervan-export-dropdown': BervanExportDropdown;
    }
}
