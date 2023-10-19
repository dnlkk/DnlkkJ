class ItcAccordion {
    constructor(target, duration = 350) {
        this._el = target;
        this._duration = duration;

        this._el.querySelector('.accordion__header').addEventListener("click", (e) => {
            this.toggle();
        })
    }

    show() {
        const el = this._el.querySelector('.accordion__body');

        if (el.classList.contains('collapsing') || el.classList.contains('collapse_show')) {
            return;
        }
        el.classList.remove('collapse')
        this._el.classList.add('accordion__item_show')

        const height = el.offsetHeight;

        el.style['max-height'] = 0;
        el.style['overflow'] = 'hidden';
        el.style['transition'] = `max-height ${this._duration}ms ease`;
        el.classList.add('collapsing');
        // получим значение высоты (нам этого необходимо для того, чтобы просто заставить браузер выполнить перерасчет макета, т.к. он не сможет нам вернуть правильное значение высоты, если не сделает это)
        el.offsetHeight;
        el.style['max-height'] = `${height}px`;

        window.setTimeout(() => {
            el.classList.remove('collapsing');
            el.classList.add('collapse_show');
            el.style['max-height'] = '';
            el.style['transition'] = '';
            el.style['overflow'] = '';
        }, this._duration);
    }
    hide() {
        const el = this._el.querySelector('.accordion__body');
        if (el.classList.contains('collapsing') || !el.classList.contains('collapse_show')) {
            return;
        }
        el.style['max-height'] = `${el.offsetHeight}px`;
        el.offsetHeight;
        el.style['max-height'] = 0;
        el.style['overflow'] = 'hidden';
        el.style['transition'] = `max-height ${this._duration}ms ease`;

        el.classList.remove('collapse');
        el.classList.remove('collapse_show');
        this._el.classList.remove('accordion__item_show')

        el.classList.add('collapsing');

        window.setTimeout(() => {
            el.classList.remove('collapsing');
            el.classList.add('collapse');
            el.style['max-height'] = '';
            el.style['transition'] = '';
            el.style['overflow'] = '';
        }, this._duration);
    }
    toggle() {
        this._el.querySelector('.accordion__body').classList.contains('collapse_show') ? this.hide() : this.show();
    }
}

document.querySelectorAll('.accordion')
    .forEach(el => {
        new ItcAccordion(el);
    })
