/**
 * Локализация времени на клиенте
 * Конвертирует UNIX timestamp (UTC) в локальное время браузера
 */
document.addEventListener('DOMContentLoaded', function() {
    localizeAllTimestamps();
});

/**
 * Находит все элементы с data-timestamp и конвертирует в локальное время
 */
function localizeAllTimestamps() {
    const timestampElements = document.querySelectorAll('[data-timestamp]');
    
    timestampElements.forEach(function(el) {
        const timestamp = parseInt(el.getAttribute('data-timestamp'));
        
        if (isNaN(timestamp)) {
            el.textContent = '—';
            return;
        }
        
        const date = new Date(timestamp);
        const formatted = formatLocalDate(date);
        
        el.textContent = formatted;
    });
}

/**
 * Форматирует дату в локальном времени пользователя
 * @param {Date} date - объект Date
 * @returns {string} отформатированная строка в формате ДД.ММ.ГГГГ ЧЧ:ММ
 */
function formatLocalDate(date) {
    return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}
