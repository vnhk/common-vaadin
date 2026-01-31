import { LitElement, html, css } from 'lit';
import { customElement, property, state } from 'lit/decorators.js';
import '@vaadin/icon';
import '@vaadin/icons';

/**
 * Quick filter input for column headers.
 * Provides inline filtering for table columns.
 */
@customElement('bervan-quick-filter')
export class BervanQuickFilter extends LitElement {
    static styles = css`
        :host {
            display: block;
            width: 100%;
        }

        .filter-container {
            position: relative;
            display: flex;
            align-items: center;
        }

        input {
            width: 100%;
            padding: 6px 28px 6px 8px;
            background: rgba(51, 65, 85, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 6px;
            color: rgba(255, 255, 255, 0.95);
            font-size: 0.75rem;
            transition: border-color 150ms, box-shadow 150ms;
        }

        input:focus {
            outline: none;
            border-color: #6366f1;
            box-shadow: 0 0 0 2px rgba(99, 102, 241, 0.2);
        }

        input::placeholder {
            color: rgba(255, 255, 255, 0.4);
        }

        .clear-btn {
            position: absolute;
            right: 4px;
            display: flex;
            align-items: center;
            justify-content: center;
            width: 20px;
            height: 20px;
            padding: 0;
            background: transparent;
            border: none;
            border-radius: 4px;
            color: rgba(255, 255, 255, 0.4);
            cursor: pointer;
            opacity: 0;
            transition: opacity 150ms, color 150ms, background 150ms;
        }

        .clear-btn.visible {
            opacity: 1;
        }

        .clear-btn:hover {
            background: rgba(255, 255, 255, 0.1);
            color: rgba(255, 255, 255, 0.8);
        }

        .clear-btn vaadin-icon {
            width: 12px;
            height: 12px;
        }

        /* Date/number filter with range */
        .range-container {
            display: flex;
            gap: 4px;
        }

        .range-container input {
            flex: 1;
            min-width: 0;
        }

        .range-separator {
            display: flex;
            align-items: center;
            color: rgba(255, 255, 255, 0.4);
            font-size: 0.75rem;
        }

        /* Select filter */
        select {
            width: 100%;
            padding: 6px 24px 6px 8px;
            background: rgba(51, 65, 85, 0.6);
            border: 1px solid rgba(255, 255, 255, 0.1);
            border-radius: 6px;
            color: rgba(255, 255, 255, 0.95);
            font-size: 0.75rem;
            cursor: pointer;
            appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 24 24' fill='none' stroke='%23ffffff' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 6px center;
        }

        select:focus {
            outline: none;
            border-color: #6366f1;
        }

        /* Light theme */
        :host([theme="light"]) input {
            background: rgba(241, 245, 249, 0.9);
            border-color: rgba(0, 0, 0, 0.1);
            color: rgba(15, 23, 42, 0.95);
        }

        :host([theme="light"]) input::placeholder {
            color: rgba(15, 23, 42, 0.4);
        }

        :host([theme="light"]) .clear-btn {
            color: rgba(15, 23, 42, 0.4);
        }

        :host([theme="light"]) .clear-btn:hover {
            background: rgba(0, 0, 0, 0.05);
            color: rgba(15, 23, 42, 0.8);
        }

        :host([theme="light"]) select {
            background: rgba(241, 245, 249, 0.9);
            border-color: rgba(0, 0, 0, 0.1);
            color: rgba(15, 23, 42, 0.95);
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 24 24' fill='none' stroke='%230f172a' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
        }

        :host([theme="light"]) .range-separator {
            color: rgba(15, 23, 42, 0.4);
        }
    `;

    @property({ type: String })
    columnKey: string = '';

    @property({ type: String })
    filterType: 'text' | 'number' | 'date' | 'select' | 'boolean' = 'text';

    @property({ type: String })
    placeholder: string = 'Filter...';

    @property({ type: Array })
    options: { value: string; label: string }[] = [];

    @property({ type: String })
    theme: string = 'dark';

    @property({ type: Number })
    debounceMs: number = 300;

    @state()
    private _value: string = '';

    @state()
    private _minValue: string = '';

    @state()
    private _maxValue: string = '';

    private _debounceTimer: number | null = null;

    private _handleInput(e: Event) {
        const input = e.target as HTMLInputElement;
        this._value = input.value;
        this._debounceDispatch();
    }

    private _handleMinInput(e: Event) {
        const input = e.target as HTMLInputElement;
        this._minValue = input.value;
        this._debounceDispatch();
    }

    private _handleMaxInput(e: Event) {
        const input = e.target as HTMLInputElement;
        this._maxValue = input.value;
        this._debounceDispatch();
    }

    private _handleSelectChange(e: Event) {
        const select = e.target as HTMLSelectElement;
        this._value = select.value;
        this._dispatchFilter();
    }

    private _clear() {
        this._value = '';
        this._minValue = '';
        this._maxValue = '';
        this._dispatchFilter();
    }

    private _debounceDispatch() {
        if (this._debounceTimer) {
            clearTimeout(this._debounceTimer);
        }
        this._debounceTimer = window.setTimeout(() => {
            this._dispatchFilter();
        }, this.debounceMs);
    }

    private _dispatchFilter() {
        const detail: any = {
            columnKey: this.columnKey,
            filterType: this.filterType
        };

        if (this.filterType === 'number' || this.filterType === 'date') {
            detail.minValue = this._minValue || null;
            detail.maxValue = this._maxValue || null;
        } else {
            detail.value = this._value || null;
        }

        this.dispatchEvent(new CustomEvent('filter-change', {
            detail,
            bubbles: true,
            composed: true
        }));
    }

    private _renderTextFilter() {
        return html`
            <div class="filter-container">
                <input
                    type="text"
                    .value=${this._value}
                    placeholder=${this.placeholder}
                    @input=${this._handleInput}>
                <button
                    class="clear-btn ${this._value ? 'visible' : ''}"
                    @click=${this._clear}>
                    <vaadin-icon icon="vaadin:close-small"></vaadin-icon>
                </button>
            </div>
        `;
    }

    private _renderNumberFilter() {
        return html`
            <div class="range-container">
                <input
                    type="number"
                    .value=${this._minValue}
                    placeholder="Min"
                    @input=${this._handleMinInput}>
                <span class="range-separator">-</span>
                <input
                    type="number"
                    .value=${this._maxValue}
                    placeholder="Max"
                    @input=${this._handleMaxInput}>
            </div>
        `;
    }

    private _renderDateFilter() {
        return html`
            <div class="range-container">
                <input
                    type="date"
                    .value=${this._minValue}
                    @input=${this._handleMinInput}>
                <span class="range-separator">-</span>
                <input
                    type="date"
                    .value=${this._maxValue}
                    @input=${this._handleMaxInput}>
            </div>
        `;
    }

    private _renderSelectFilter() {
        return html`
            <select .value=${this._value} @change=${this._handleSelectChange}>
                <option value="">All</option>
                ${this.options.map(opt => html`
                    <option value=${opt.value}>${opt.label}</option>
                `)}
            </select>
        `;
    }

    private _renderBooleanFilter() {
        return html`
            <select .value=${this._value} @change=${this._handleSelectChange}>
                <option value="">All</option>
                <option value="true">Yes</option>
                <option value="false">No</option>
            </select>
        `;
    }

    render() {
        switch (this.filterType) {
            case 'number':
                return this._renderNumberFilter();
            case 'date':
                return this._renderDateFilter();
            case 'select':
                return this._renderSelectFilter();
            case 'boolean':
                return this._renderBooleanFilter();
            default:
                return this._renderTextFilter();
        }
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'bervan-quick-filter': BervanQuickFilter;
    }
}
