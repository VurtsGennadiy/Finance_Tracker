// Универсальная функция форматирования даты
function getTodayFormatted() {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// Инициализация даты при открытии модальных окон
document.addEventListener('DOMContentLoaded', function () {
    // Income Modal
    const incomeModal = document.getElementById('incomeModal');
    if (incomeModal) {
        incomeModal.addEventListener('shown.bs.modal', function () {
            const incomeDateInput = document.getElementById('incomeDate');
            if (incomeDateInput) {
                incomeDateInput.value = getTodayFormatted();
                incomeDateInput.max = getTodayFormatted();
            }
        });
    }

    // Expense Modal
    const expenseModal = document.getElementById('expenseModal');
    if (expenseModal) {
        expenseModal.addEventListener('shown.bs.modal', function () {
            const expenseDateInput = document.getElementById('expenseDate');
            if (expenseDateInput) {
                expenseDateInput.value = getTodayFormatted();
                expenseDateInput.max = getTodayFormatted();
            }
        });
    }

    // Transfer Modal
    const transferModal = document.getElementById('transferModal');
    if (transferModal) {
        transferModal.addEventListener('shown.bs.modal', function () {
            const transferDateInput = document.getElementById('transferDate');
            if (transferDateInput) {
                transferDateInput.value = getTodayFormatted();
                transferDateInput.max = getTodayFormatted();
            }
        });
    }
});
