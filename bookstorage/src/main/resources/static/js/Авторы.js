/**
   * Глобальная функция для выбора/снятия всех чекбоксов.
   * Сделана глобальной, чтобы была доступна из HTML.
   */
  function toggleAllCheckboxes(source) {
      const checkboxes = document.querySelectorAll('#authors-table-body input[type="checkbox"]');
      checkboxes.forEach(checkbox => checkbox.checked = source.checked);
  }
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

  document.addEventListener('DOMContentLoaded', function() {
    let currentSortColumn = 'fio';
    let currentSortOrder = 'asc';
    let currentSearch = '';

    /**
     * Функция для отображения сообщений
     * @param {string} message - Сообщение для отображения.
     * @param {string} type - Тип сообщения ('success' или 'error').
     */
    function showMessage(message, type) {
        const messageContainer = document.getElementById('message-container');
        messageContainer.innerHTML = `<div class="message ${type}">${message}</div>`;
        setTimeout(() => {
            messageContainer.innerHTML = '';
        }, 5000);
    }

    /**
     * Функция для получения и отображения авторов
     * @param {Object} params - Параметры запроса для получения авторов.
     */
    async function fetchAuthors(params = {}) {
        try {
            const queryParams = new URLSearchParams(params).toString();
            // Используем эндпоинт /api/authors/search?q=... для поиска, иначе /api/authors
            const apiUrl = params.q ? `/api/authors/search?q=${encodeURIComponent(params.q)}&sort_column=${params.sort_column}&sort_order=${params.sort_order}` :
                                      `/api/authors?sort_column=${params.sort_column}&sort_order=${params.sort_order}`;
            const response = await fetch(apiUrl);
            if (!response.ok) {
                throw new Error(`Ошибка: ${response.status} ${response.statusText}`);
            }
            const authors = await response.json();
            populateAuthorsTable(authors);
        } catch (error) {
            console.error('Ошибка при получении авторов:', error);
            showMessage('Не удалось загрузить список авторов.', 'error');
        }
    }

    /**
     * Функция для заполнения таблицы авторами
     * @param {Array} authors - Список авторов для отображения.
     */
    function populateAuthorsTable(authors) {
        const tbody = document.getElementById('authors-table-body');
        // Очищаем существующие строки
        tbody.innerHTML = '';

        if (authors.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">Авторы не найдены.</td></tr>';
            return;
        }

        authors.forEach(author => {
            const tr = document.createElement('tr');

            // Чекбокс
            const checkboxTd = document.createElement('td');
            checkboxTd.classList.add('checkbox-column');
            const checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.value = author.id;
            checkboxTd.appendChild(checkbox);
            tr.appendChild(checkboxTd);

            // ФИО (гиперссылка)
            const fioTd = document.createElement('td');
            fioTd.classList.add('u-table-cell');
            const link = document.createElement('a');
            link.href = `Информация-об-авторе.html?id=${author.id}`;
            link.textContent = author.fio || 'Без ФИО';
            link.classList.add('text-decoration-none', 'text-primary');
            fioTd.appendChild(link);
            tr.appendChild(fioTd);

            // Дата рождения
            const birthDateTd = document.createElement('td');
            birthDateTd.classList.add('u-table-cell');
            birthDateTd.textContent = author.birthDate || 'Не указано';
            tr.appendChild(birthDateTd);

            // Страна
            const countryTd = document.createElement('td');
            countryTd.classList.add('u-table-cell');
            countryTd.textContent = author.country || 'Не указано';
            tr.appendChild(countryTd);

            // Псевдоним
            const nicknameTd = document.createElement('td');
            nicknameTd.classList.add('u-table-cell');
            nicknameTd.textContent = author.nickname || 'Не указано';
            tr.appendChild(nicknameTd);

            tbody.appendChild(tr);
        });
    }

    /**
     * Обрабатывает отправку формы поиска.
     * @param {Event} event - Событие отправки формы.
     */
    function handleSearch(event) {
        event.preventDefault(); // Предотвращаем стандартную отправку формы
        currentSearch = getSearchValue();
        // Сбрасываем сортировку к значениям по умолчанию при поиске
        currentSortColumn = 'fio';
        currentSortOrder = 'asc';
        resetSortButtons();
        fetchAuthors({
            q: currentSearch,
            sort_column: currentSortColumn,
            sort_order: currentSortOrder
        });
    }

    /**
     * Обрабатывает нажатие на кнопку "Сбросить поиск".
     */
    function handleResetButton() {
        currentSearch = '';
        const searchInput = document.querySelector('.input-group input[name="search"]');
        searchInput.value = '';
        // Сбрасываем сортировку к значениям по умолчанию
        currentSortColumn = 'fio';
        currentSortOrder = 'asc';
        resetSortButtons();
        fetchAuthors({
            sort_column: currentSortColumn,
            sort_order: currentSortOrder
        });
    }

    /**
     * Получает текущее значение поля поиска.
     * @return {string} Обрезанная строка поиска.
     */
    function getSearchValue() {
        const searchInput = document.querySelector('.input-group input[name="search"]');
        return searchInput.value.trim();
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
        fetchAuthors({
            q: currentSearch,
            sort_column: currentSortColumn,
            sort_order: currentSortOrder
        });
    }

    function handleDeleteSelectedAuthors(event) {
          event.preventDefault();

          // Собираем все выбранные чекбоксы
          const selectedCheckboxes = document.querySelectorAll('#authors-table-body input[type="checkbox"]:checked');
          if (selectedCheckboxes.length === 0) {
              showMessage('Пожалуйста, выберите хотя бы одного автора для удаления.', 'error');
              return;
          }

          // Спросим у пользователя, удалять ли всё (true) или не удалять ничего (false)
          const removeEverything = confirm(
            'Удалить выбранных авторов вместе со всеми их книгами (если у книги нет других соавторов)?\n\nНажмите ОК, чтобы удалить ВСЁ\nНажмите Отмена, чтобы НЕ удалять вообще никого.'
          );

          // Если пользователь нажал «Отмена», removeEverything = false => значит вообще ничего не делаем
          // Но всё равно отправим запрос, чтобы сервис понял, что removeEverything=false
          // и просто вышел без удаления.
          const authorIds = Array.from(selectedCheckboxes).map(ch => parseInt(ch.value, 10));

          fetch(`/api/authors/bulk-delete?removeEverything=${removeEverything}`, {
              method: 'DELETE',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(authorIds)
          })
          .then(response => {
              if (response.status === 204) {
                  if (removeEverything) {
                      // Только если removeEverything=true, значит что-то действительно удалилось
                      showMessage('Выбранные авторы и их книги (если у книги не осталось соавторов) успешно удалены.', 'success');
                      // Перезапрашиваем список авторов
                      fetchAuthors({
                        q: currentSearch,
                        sort_column: currentSortColumn,
                        sort_order: currentSortOrder
                      });
                  } else {
                      // removeEverything=false => "Отмена"
                      showMessage('Удаление отменено пользователем. Ничего не произошло.', 'success');
                  }
              } else {
                  showMessage('Произошла ошибка при удалении авторов.', 'error');
              }
          })
          .catch(error => {
              console.error('Ошибка при удалении авторов:', error);
              showMessage('Не удалось удалить авторов.', 'error');
          });
    }

    /**
     * Инициализирует обработчики событий для формы поиска и кнопок сортировки.
     */
    function initializeEventListeners() {
        // Обработчик отправки формы поиска
        const searchForm = document.getElementById('searchForm');
        if (searchForm) {
            searchForm.addEventListener('submit', handleSearch);
        }

        // Обработчик кнопки "Искать"
        const searchButton = document.getElementById('searchButton');
        if (searchButton) {
            searchButton.addEventListener('click', handleSearch);
        }

        // Обработчик кнопки "Сбросить поиск"
        const resetButton = document.getElementById('resetButton');
        if (resetButton) {
            resetButton.addEventListener('click', handleResetButton);
        }

        // Обработчик кнопки "Удалить выбранные"
         const deleteButton = document.getElementById('deleteSelectedButton');
      if (deleteButton) {
          deleteButton.addEventListener('click', handleDeleteSelectedAuthors);
      }

        // Обработчики сортировки
        const sortButtons = document.querySelectorAll('.sort-button');
        sortButtons.forEach(button => {
            const columnName = button.getAttribute('data-column');
            button.addEventListener('click', () => handleSort(columnName, button));
        });
    }




    // Запуск и первичное наполнение таблицы
    initializeEventListeners();
    fetchAuthors();





  });