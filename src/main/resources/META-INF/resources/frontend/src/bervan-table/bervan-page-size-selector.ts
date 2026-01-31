import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';

/**
 * Page size selector dropdown component.
 * Allows users to select how many items to display per page.
 */
@customElement('bervan-page-size-selector')
export class BervanPageSizeSelector extends LitElement {
    static styles = css`
        :host {
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        label {
            color: rgba(255, 255, 255, 0.7);
            font-size: 0.875rem;
            white-space: nowrap;
        }

        select {
            padding: 6px 28px 6px 12px;
            background: rgba(51, 65, 85, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 8px;
            color: rgba(255, 255, 255, 0.95);
            font-size: 0.875rem;
            cursor: pointer;
            appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%23ffffff' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 8px center;
            transition: border-color 150ms cubic-bezier(0.4, 0, 0.2, 1),
                        box-shadow 150ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        select:hover {
            border-color: rgba(255, 255, 255, 0.2);
        }

        select:focus {
            outline: none;
            border-color: #6366f1;
            box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.2);
        }

        option {
            background: #1e293b;
            color: rgba(255, 255, 255, 0.95);
        }

        /* Light theme support */
        :host([theme="light"]) label {
            color: rgba(15, 23, 42, 0.7);
        }

        :host([theme="light"]) select {
            background: rgba(241, 245, 249, 0.9);
            border-color: rgba(0, 0, 0, 0.1);
            color: rgba(15, 23, 42, 0.95);
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 24 24' fill='none' stroke='%230f172a' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
        }

        :host([theme="light"]) select:hover {
            border-color: rgba(0, 0, 0, 0.2);
        }

        :host([theme="light"]) option {
            background: #f8fafc;
            color: rgba(15, 23, 42, 0.95);
        }
    `;

    @property({ type: Array })
    options: number[] = [10, 25, 50, 100, -1];

    @property({ type: Number })
    value: number = 50;

    @property({ type: String })
    label: string = 'Show:';

    @property({ type: String })
    allLabel: string = 'All';

    @property({ type: String })
    storageKey: string = '';

    @property({ type: String })
    theme: string = 'dark';

    connectedCallback() {
        super.connectedCallback();
        this._loadFromStorage();
    }

    private _loadFromStorage() {
        if (this.storageKey) {
            const stored = localStorage.getItem(this.storageKey);
            if (stored) {
                const parsedValue = parseInt(stored, 10);
                if (this.options.includes(parsedValue)) {
                    this.value = parsedValue;
                    this._dispatchChange();
                }
            }
        }
    }

    private _saveToStorage(value: number) {
        if (this.storageKey) {
            localStorage.setItem(this.storageKey, value.toString());
        }
    }

    private _handleChange(e: Event) {
        const select = e.target as HTMLSelectElement;
        this.value = parseInt(select.value, 10);
        this._saveToStorage(this.value);
        this._dispatchChange();
    }

    private _dispatchChange() {
        this.dispatchEvent(new CustomEvent('page-size-change', {
            detail: { value: this.value },
            bubbles: true,
            composed: true
        }));
    }

    private _getOptionLabel(option: number): string {
        return option === -1 ? this.allLabel : option.toString();
    }

    render() {
        return html`
            <label>${this.label}</label>
            <select .value=${this.value.toString()} @change=${this._handleChange}>
                ${this.options.map(option => html`
                    <option value=${option} ?selected=${option === this.value}>
                        ${this._getOptionLabel(option)}
                    </option>
                `)}
            </select>
        `;
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'bervan-page-size-selector': BervanPageSizeSelector;
    }
}
