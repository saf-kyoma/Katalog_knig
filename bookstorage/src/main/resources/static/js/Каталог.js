/**
   * Глобальная функция для выбора/снятия всех чекбоксов.
   * Сделана глобальной, чтобы была доступна из HTML.
   */
  function toggleAllCheckboxes(source) {
      const checkboxes = document.querySelectorAll('.u-table-body-1 input[type="checkbox"]');
      checkboxes.forEach(checkbox => checkbox.checked = source.checked);
  }

  document.addEventListener('DOMContentLoaded', function() {
    let currentSortColumn = 'name';
    let currentSortOrder = 'asc';
    let currentSearch = '';

    /**
     * Получает текущее значение поля поиска.
     * @return {string} Обрезанная строка поиска.
     */
    function getSearchValue() {
      const searchInput = document.querySelector('.u-search-input');
      return searchInput.value.trim();
    }

    /**
     * Обрабатывает отправку формы поиска.
     * @param {Event} event - Событие отправки формы.
     */
    function handleSearch(event) {
      event.preventDefault(); // Предотвращаем стандартную отправку формы
      currentSearch = getSearchValue();
      // Сбрасываем сортировку к значениям по умолчанию при поиске
      currentSortColumn = 'name';
      currentSortOrder = 'asc';
      resetSortButtons();
      fetchBooks({
        search: currentSearch,
        sort_column: currentSortColumn,
        sort_order: currentSortOrder
      });
    }

    /**
     * Обрабатывает нажатие на кнопку "Сбросить поиск".
     */
    function handleResetButton() {
      currentSearch = '';
      const searchInput = document.querySelector('.u-search-input');
      searchInput.value = '';
      // Сбрасываем сортировку к значениям по умолчанию
      currentSortColumn = 'name';
      currentSortOrder = 'asc';
      resetSortButtons();
      fetchBooks({
        search: currentSearch,
        sort_column: currentSortColumn,
        sort_order: currentSortOrder
      });
    }

    /**
     * Сбрасывает все кнопки сортировки к их исходному состоянию.
     */
    function resetSortButtons() {
      const sortButtons = document.querySelectorAll('.sort-button');
      sortButtons.forEach(button => {
        button.setAttribute('data-order', 'asc');
        button.textContent = '▲';
        button.classList.remove('btn-secondary');
        button.classList.add('btn-light');
      });
    }

    /**
     * Получает книги из API на основе предоставленных параметров.
     * @param {Object} params - Параметры запроса для получения книг.
     */
    function fetchBooks(params = {}) {
      const queryParams = new URLSearchParams(params).toString();
      const apiUrl = `/api/books${queryParams ? '?' + queryParams : ''}`;

      fetch(apiUrl)
        .then(response => {
          if (!response.ok) {
            throw new Error('Сетевая ошибка: ' + response.statusText);
          }
          return response.json();
        })
        .then(data => {
          populateTable(data);
        })
        .catch(error => {
          console.error('Ошибка при выполнении запроса:', error);
        });
    }

    /**
     * Заполняет таблицу полученными данными о книгах.
     * @param {Array} books - Список книг для отображения.
     */
    function populateTable(books) {
      const tableBody = document.querySelector('.u-table-body-1');
      tableBody.innerHTML = '';

      if (books.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 7; // Увеличено на 1 из-за добавленной колонки с чекбоксами
        cell.className = 'text-center';
        cell.textContent = 'Книги не найдены.';
        row.appendChild(cell);
        tableBody.appendChild(row);
        return;
      }

      books.forEach(book => {
        const row = document.createElement('tr');

        // Чекбокс
        const checkboxTd = document.createElement('td');
        checkboxTd.classList.add('checkbox-column');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = book.isbn; // Или другой уникальный идентификатор
        checkboxTd.appendChild(checkbox);
        row.appendChild(checkboxTd);

        // Название книги (гиперссылка)
        const nameCell = document.createElement('td');
        nameCell.className = 'u-border-1 u-border-grey-30 u-palette-4-light-3 u-table-cell';
        const link = document.createElement('a');
        link.href = `Информация-о-книге.html?isbn=${book.isbn}`;
        link.textContent = book.name || 'Без названия';

        // Визуальные стили ссылки
        link.className = 'text-decoration-none';
        link.style.color = '#0d6efd';

        nameCell.appendChild(link);
        row.appendChild(nameCell);

        // Автор(-ы)
        const authorsCell = document.createElement('td');
        authorsCell.className = 'u-border-1 u-border-grey-30 u-table-cell';
        if (book.authors && Array.isArray(book.authors) && book.authors.length > 0) {
          const authors = book.authors.map(author => author.fio).join(', ');
          authorsCell.textContent = authors;
        } else {
          authorsCell.textContent = 'Неизвестно';
        }
        row.appendChild(authorsCell);

        // Дата издания
        const yearCell = document.createElement('td');
        yearCell.className = 'u-border-1 u-border-grey-30 u-table-cell';
        yearCell.textContent = book.publicationYear || 'Не указано';
        row.appendChild(yearCell);

        // Издательство
        const publishingCell = document.createElement('td');
        publishingCell.className = 'u-border-1 u-border-grey-30 u-table-cell';
        publishingCell.textContent = book.publishingCompany || 'Не указано';
        row.appendChild(publishingCell);

        // Количество экземпляров
        const countCell = document.createElement('td');
        countCell.className = 'u-border-1 u-border-grey-30 u-table-cell';
        countCell.textContent = book.countOfBooks !== undefined ? book.countOfBooks : 'Не указано';
        row.appendChild(countCell);

        // ISBN
        const isbnCell = document.createElement('td');
        isbnCell.className = 'u-border-1 u-border-grey-30 u-table-cell';
        isbnCell.textContent = book.isbn || 'Не указано';
        row.appendChild(isbnCell);

        tableBody.appendChild(row);
      });
    }

    /**
     * Обрабатывает логику сортировки при клике на кнопку сортировки.
     * @param {string} column - Столбец для сортировки.
     * @param {HTMLElement} button - Элемент кнопки сортировки.
     */
    function handleSort(column, button) {
      const currentOrder = button.getAttribute('data-order') || 'asc';
      const newOrder = currentOrder === 'asc' ? 'desc' : 'asc';
      button.setAttribute('data-order', newOrder);
      button.textContent = newOrder === 'asc' ? '▲' : '▼';

      // Сбрасываем состояние других кнопок
      const sortButtons = document.querySelectorAll('.sort-button');
      sortButtons.forEach(btn => {
        if (btn !== button) {
          btn.setAttribute('data-order', 'asc');
          btn.textContent = '▲';
          btn.classList.remove('btn-secondary');
          btn.classList.add('btn-light');
        }
      });

      // Меняем стили текущей кнопки
      if (newOrder === 'asc') {
        button.classList.remove('btn-secondary');
        button.classList.add('btn-light');
      } else {
        button.classList.remove('btn-light');
        button.classList.add('btn-secondary');
      }

      // Обновляем текущую сортировку и делаем запрос
      currentSortColumn = column;
      currentSortOrder = newOrder;
      fetchBooks({
        search: currentSearch,
        sort_column: currentSortColumn,
        sort_order: currentSortOrder
      });
    }

    /**
     * Обработчик нажатия на кнопку "Удалить выбранные".
     */
    function handleDeleteSelected(event) {
        event.preventDefault(); // Предотвращаем переход по ссылке

        // Собираем все выбранные чекбоксы
        const selectedCheckboxes = document.querySelectorAll('.u-table-body-1 input[type="checkbox"]:checked');
        if (selectedCheckboxes.length === 0) {
            alert('Пожалуйста, выберите хотя бы одну книгу для удаления.');
            return;
        }

        // Подтверждаем удаление
        const confirmation = confirm(`Вы уверены, что хотите удалить ${selectedCheckboxes.length} книгу(и)?`);
        if (!confirmation) {
            return;
        }

        // Собираем ISBN выбранных книг
        const isbns = Array.from(selectedCheckboxes).map(checkbox => checkbox.value);

        // Отправляем запрос на массовое удаление
        fetch('/api/books/bulk-delete', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(isbns)
        })
        .then(response => {
            if (response.status === 204) {
                // Успешное удаление
                alert('Выбранные книги успешно удалены.');
                // Обновляем таблицу
                fetchBooks({
                    search: currentSearch,
                    sort_column: currentSortColumn,
                    sort_order: currentSortOrder
                });
            } else {
                // Ошибка при удалении
                alert('Произошла ошибка при удалении книг.');
            }
        })
        .catch(error => {
            console.error('Ошибка при выполнении запроса:', error);
            alert('Произошла ошибка при удалении книг.');
        });
    }

    /**
     * Инициализирует обработчики событий для формы поиска и кнопок сортировки.
     */
    function initializeEventListeners() {
      // Обработчик отправки формы поиска
      const searchForm = document.querySelector('#searchForm');
      if (searchForm) {
        searchForm.addEventListener('submit', handleSearch);
      }

      // Обработчик кнопки "Искать"
      const searchButton = document.querySelector('#searchButton');
      if (searchButton) {
        searchButton.addEventListener('click', handleSearch);
      }

      // Обработчик кнопки "Сбросить поиск"
      const resetButton = document.querySelector('#resetButton');
      if (resetButton) {
        resetButton.addEventListener('click', handleResetButton);
      }

      // Обработчики сортировки
      const sortButtons = document.querySelectorAll('.sort-button');
      sortButtons.forEach(button => {
        const columnName = button.getAttribute('data-column');
        button.addEventListener('click', () => handleSort(columnName, button));
      });

      // Обработчик кнопки "Удалить выбранные"
      const deleteButton = document.getElementById('deleteSelectedButton');
      if (deleteButton) {
          deleteButton.addEventListener('click', handleDeleteSelected);
      }
    }

    // Запуск и первичное наполнение таблицы
    initializeEventListeners();
    fetchBooks({
      search: currentSearch,
      sort_column: currentSortColumn,
      sort_order: currentSortOrder
    });
  });

  // *** ДОБАВЛЕННЫЙ КОД ДЛЯ КНОПОК ИМПОРТА/ЭКСПОРТА ***
      document.addEventListener('DOMContentLoaded', function() {
        const importBtn = document.getElementById('DBadd');
        const exportBtn = document.getElementById('DBout');

        // При нажатии на "Загрузить базу данных" (import)
        importBtn.addEventListener('click', function() {
          fetch('/api/csv/import', {
            method: 'POST'
          })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при импорте базы данных из CSV');
            }
            return response.text();
          })
          .then(text => {
            alert('Импорт успешно завершён: ' + text);
          })
          .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выполнить импорт: ' + error.message);
          });
        });

        // При нажатии на "Выгрузить базу данных" (export)
        exportBtn.addEventListener('click', function() {
          fetch('/api/csv/export', {
            method: 'POST'
          })
          .then(response => {
            if (!response.ok) {
              throw new Error('Ошибка при экспорте базы данных в CSV');
            }
            return response.text();
          })
          .then(text => {
            alert('Экспорт успешно завершён: ' + text);
          })
          .catch(error => {
            console.error('Ошибка:', error);
            alert('Не удалось выполнить экспорт: ' + error.message);
          });
        });
      });