import { LitElement, html, css } from 'lit';
import { customElement, property } from 'lit/decorators.js';
import '@vaadin/icon';
import '@vaadin/icons';

/**
 * Sort indicator component for multi-column sorting.
 * Shows sort direction and priority number for multi-sort.
 */
@customElement('bervan-sort-indicator')
export class BervanSortIndicator extends LitElement {
    static styles = css`
        :host {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            margin-left: 4px;
        }

        .sort-arrow {
            display: flex;
            align-items: center;
            justify-content: center;
            color: #6366f1;
            transition: transform 200ms cubic-bezier(0.4, 0, 0.2, 1);
        }

        .sort-arrow.desc {
            transform: rotate(180deg);
        }

        .sort-arrow vaadin-icon {
            width: 14px;
            height: 14px;
        }

        .sort-priority {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 16px;
            height: 16px;
            padding: 0 4px;
            background: #6366f1;
            border-radius: 9999px;
            color: white;
            font-size: 10px;
            font-weight: 700;
        }

        /* Hidden state */
        :host([hidden]) {
            display: none;
        }

        /* Light theme */
        :host([theme="light"]) .sort-arrow {
            color: #4f46e5;
        }

        :host([theme="light"]) .sort-priority {
            background: #4f46e5;
        }
    `;

    @property({ type: String })
    direction: 'asc' | 'desc' | 'none' = 'none';

    @property({ type: Number })
    priority: number = -1;

    @property({ type: Boolean })
    showPriority: boolean = false;

    @property({ type: String })
    theme: string = 'dark';

    render() {
        if (this.direction === 'none') {
            return html``;
        }

        return html`
            <span class="sort-arrow ${this.direction === 'desc' ? 'desc' : ''}">
                <vaadin-icon icon="vaadin:arrow-up"></vaadin-icon>
            </span>
            ${this.showPriority && this.priority >= 0 ? html`
                <span class="sort-priority">${this.priority + 1}</span>
            ` : ''}
        `;
    }
}

declare global {
    interface HTMLElementTagNameMap {
        'bervan-sort-indicator': BervanSortIndicator;
    }
}
